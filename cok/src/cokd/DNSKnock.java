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

import net.sourceforge.jpcap.net.*;
import net.sourceforge.jpcap.util.*;

import cokshare.KnockDescriptor;
import cokshare.UDP_OTP_KnockDescriptor;
import cokshare.DNSKnockDescriptor;

/**
 * Class defining "DNS Knocks" which are <code>UDP_OTP_Knock</code>s
 * specialized to operate only on DNS name lookups which are directed
 * at a given server.  To operate they arequire a <code>[knockDomain]</code> 
 * such that given a <code>[One Time Password]</code> (which is 
 * <code>_</code> separated) name lookups are for domains in the form 
 * <code>[One Time Password].[knockDomain]</code>.  For example a DNS Knock 
 * query could be for <code>INCH_SEA_ANNE_LONG_AHEM_TOUR.knocking.org</code>.
 *
 * @see cokd.UDP_OTP_Knock
 */
public class DNSKnock extends UDP_OTP_Knock {

	/** Host suffix specifying the knock domain */
	protected String knockDomain;

	/**
	 * Constructor
	 *
	 * @param desc a DNSKnockDescriptor describing this knock!
	 */
	public DNSKnock(DNSKnockDescriptor desc) {
		super((UDP_OTP_KnockDescriptor)desc);

		this.knockDomain = desc.getKnockDomain();
	}

	/**
	 * Check an incoming DNS packet by extracting the appropriate
	 * data, and if it is, then checking it is a query for the
	 * "correct" domain, after which the one time password is extract,
	 * _'s are removed, and it is checked via the facilities defined
	 * by <code>UDP_OTP_Knock</code>
	 *
	 * @param inPacket an incoming packet to check... it should have
	 * been forced to be a UDPPacket by COKd...
	 */
	public void checkPacket(Packet inPacket) {

		if (inPacket instanceof UDPPacket) {
			byte[] dnsdata = ((UDPPacket)inPacket).getData();

			int transactionID = ArrayHelper.extractInteger(dnsdata, 0, 2);
			int flags         = ArrayHelper.extractInteger(dnsdata, 2, 2);
			int questions     = ArrayHelper.extractInteger(dnsdata, 4, 2);
			int answers       = ArrayHelper.extractInteger(dnsdata, 6, 2);
			int authorities   = ArrayHelper.extractInteger(dnsdata, 8, 2);
			int additionalrrs = ArrayHelper.extractInteger(dnsdata, 10, 2);
		
			int querytype = -1;
			int queryclass = -1;

			int pos = 12;
			int chunks = 0;
			String domainname = "";
			for (int x = 0; x < questions; x++) {
				int cnt = ArrayHelper.extractInteger(dnsdata, pos++, 1);
			
				while(cnt != 0) {
					byte[] namechunk = new byte[cnt];
					for (int y = 0; y < cnt; y++) {
						namechunk[y] = dnsdata[y+pos];
					}
					pos+=cnt;

					if (chunks == 0) {
						domainname = new String(namechunk);
					} else {
						domainname = domainname.concat("."+new String(namechunk));
					}

					chunks++;

					cnt = ArrayHelper.extractInteger(dnsdata, pos++, 1);
				}
				
				querytype =  ArrayHelper.extractInteger(dnsdata, pos, 2);
				pos += 2;
				queryclass = ArrayHelper.extractInteger(dnsdata, pos, 2);
				pos+=2;

				/* XXX do some better checking here... we should
				 * verify that this is a proper query in some way! XXX
				 */
				if (domainname.endsWith(knockDomain)) {
					String otpString = (domainname.split("\\."))[0];
					otpString = otpString.replaceAll("_", " ");
					checkReadable(otpString, inPacket);
				}
			}
		}
	}

	/**
	 * Get a knock descriptor back from the current state of this knock.
	 *
	 * @return return a DNSKnockDescriptor based on this knock.
	 */
	public KnockDescriptor getKnockDescriptor() {
		return new DNSKnockDescriptor(firstOTP, nextOTP, 
									  successAction.getRuleset(),
									  badSourceAction.getRuleset(),
									  replayAction.getRuleset(), rulename,
									  algo, knockDomain, oldKeys);
	}
}
