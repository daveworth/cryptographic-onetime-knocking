/*
 *  Copyright (c) 2004, David Worth <cesium@hexi-dump.org>
 *  All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *  
 *  Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 * 
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  
 *  Neither the name of the Hexi-Dump.org nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cokd;

import java.lang.*;
import java.util.*;
import java.security.*;

import net.sourceforge.jpcap.net.*;

import cokshare.KnockDescriptor;
import cokshare.UDP_OTP_KnockDescriptor;
import cokshare.MDC;
import cokshare.SimpleSyslogger;

/**
 * Implementation of Knock to handle standard OTP passwords embedded
 * in UDP packets destined for an appropriate port.
 */
public class UDP_OTP_Knock extends Knock {

	/** initial destination hash NOTE: this must be protected! */
	protected byte[] firstOTP;
	/** next value to hash to */
	protected byte[] nextOTP;
	/** table to track replay attacks */
	protected HashSet oldKeys;
	/** current one way hash algorithm */
	protected String algo;
	/** "rulename" replaces seed in OTP or S/Key */
	protected String rulename;
	/** actual hashing object */
	protected MessageDigest digester;
	/** action to execute on replay */
	protected KnockAction replayAction;
	/** port this knock is bound to */
	protected int port;

	/**
	 * Constructor...
	 *
	 * @param desc UDP_OTP_KnockDescriptor providing the info about the knock
	 */
	UDP_OTP_Knock(UDP_OTP_KnockDescriptor desc) {
		super(desc.getSuccessRules(), desc.getBadSourceRules(),
			  desc.getValidSourceAddrs());

		this.firstOTP = new byte[desc.getFirstOTP().length];
		System.arraycopy(desc.getFirstOTP(), 0,
						 this.firstOTP, 0,
						 desc.getFirstOTP().length);

		this.nextOTP = new byte[desc.getNextOTP().length];
		System.arraycopy(desc.getNextOTP(), 0,
						 this.nextOTP, 0,
						 desc.getNextOTP().length);

		this.replayAction = new KnockAction(desc.getReplayRules());
		this.algo = desc.getAlgorithm();
		this.rulename = desc.getRulename();
		this.port = desc.getPort();

		oldKeys = desc.getOldKeys();
		if (oldKeys == null)
			oldKeys = new HashSet();

		try {
			digester = MessageDigest.getInstance(algo);
		} catch (NoSuchAlgorithmException e) {
			SimpleSyslogger.syslog("DEBUG (UDP_OTP_Knock): " + e.getMessage());
		}
	}

	/**
	 * Check an incoming packet for a valid knock by extracting the contained data
	 *  and calling <code>checkReadable()</code>
	 *
	 * @param inPacket should be a UDPPacket from COKd
	 */
	public void checkPacket(Packet inPacket) {

		if (inPacket instanceof UDPPacket) {
			String otpString = new String(((UDPPacket)inPacket).getData());
			checkReadable(otpString, inPacket);
		}
	}

	/**
	 * The meat of verifying a one-time-passwords lies here.  A string
	 * containing the password and a packet are taken in.  The string
	 * is converted <code>fromReadable()</code> and hashed, then
	 * folded and compared with the stored folded one-time-password.
	 * If the key matches then the success action is called, if the
	 * key is a replay the replay action is called, and if the match
	 * blatantly fails it is ignored (for now)
	 *
	 * @param otpString a string containing a one-time-password
	 *
	 * @param inPacket a UDPPacket which can be passed to an action if necessary
	 *
	 * @see cokshare.MDC#fromReadable
	 * @see cokshare.MDC#foldHash
	 */
	protected void checkReadable(String otpString, Packet inPacket) {
		if (inPacket instanceof UDPPacket) {
			UDPPacket packet = (UDPPacket)inPacket;
			int destPort = packet.getDestinationPort();
			
			try {
				byte[] incomingPotentialOTP = MDC.fromReadable(otpString);
				byte[] foldedPotentialOTP = null;

				try {
					foldedPotentialOTP = MDC.foldHash(algo, digester.digest(incomingPotentialOTP));
				} catch (NoSuchAlgorithmException e) {
					SimpleSyslogger.syslog("DEBUG (UDP_OTP_Knock): " + e.getMessage());
				}

				boolean match = false;
				if (destPort == port && foldedPotentialOTP != null &&
					foldedPotentialOTP.length == nextOTP.length) {
					int mismatches = 0;
					for (int x = 0; x < foldedPotentialOTP.length; x++) {
						if (foldedPotentialOTP[x] != nextOTP[x]) {
							mismatches++;
							break;
						}					
					}
					if (mismatches == 0)
						match = true;
				}

				if (match) {
					if (validSource(packet.getSourceAddress())) {
						oldKeys.add(otpString);
					
						nextOTP = new byte[incomingPotentialOTP.length];
						for (int x = 0; x < incomingPotentialOTP.length; x++)
							nextOTP[x] = incomingPotentialOTP[x];

						successAction.execute((IPPacket)packet,getKnockDescriptor().getKnockDesc());
					} else {
						badSourceAction.execute((IPPacket)packet,getKnockDescriptor().getKnockDesc());
					}
				} else {
					if (oldKeys.contains(otpString)) {
						replayAction.execute((IPPacket)packet,getKnockDescriptor().getKnockDesc());
					}
				}
			} catch (IllegalArgumentException e) {
				// COK saw a UDP packet which isn't a knock and flipped out!
				//   so we ignore it for now!
				//SimpleSyslogger.syslog("Illegal Argument: " + e.getMessage());
			}
		}
	}

	/**
	 * Overridden hashCode method provides a hash of relevant
	 * information but... NOTE: This may suck, ALOT!
	 *
	 * @return an integer hashCode for this instance
	 */
	public int hashCode() {
		int retval = 0;
		for (int x = 0; x < 4; x++) {
			retval |= firstOTP[x] ^ firstOTP[x+4];
		}

		retval ^= ((port & 0xff) << 16) | (port & 0xffff);

		return retval;
	}

	/**
	 * Get a knock descriptor back from the current state of this knock.
	 *
	 * @return return a UDP_OTP_KnockDescriptor based on this knock.
	 */
	public KnockDescriptor getKnockDescriptor() {
		return new UDP_OTP_KnockDescriptor(firstOTP, nextOTP, port, 
										   successAction.getRuleset(),
										   badSourceAction.getRuleset(),
										   replayAction.getRuleset(),
										   rulename,
										   algo, oldKeys);
	}
}
