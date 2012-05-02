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

import java.util.*;
import net.sourceforge.jpcap.net.*;

import cokshare.KnockDescriptor;
import cokshare.PortSequenceKnockDescriptor;

/**
 * Class defining a knock which is meant to then trigger an event
 */
public class PortSequenceKnock extends Knock {

	/** time when the first knock occurred */
	protected long firstKnock = 0;
	/** how long the knock is allowed to take */
	protected long timeout;
	/** number of ports in knock sequence */
	protected int knockLength;
	/** actual sequence of ports to be knocked */
	protected Vector portSeq;
	/** where in the knock sequence we are*/      
	protected int seqIdx = 0;

	/**
	 * Constructor
	 * 
	 * @param portSeq Vector of ports in the knock sequence
	 * @param timeout Amount of time a port is valid
	 * @param action  Default action on success of knock
	 */
	PortSequenceKnock(PortSequenceKnockDescriptor desc) {
		super(desc.getSuccessRules(), desc.getBadSourceRules(),
			  desc.getValidSourceAddrs());
		this.knockLength = desc.getPortSeq().size();
		this.portSeq = desc.getPortSeq();
		this.timeout = desc.getTimeout();
	}

	/**
	 * Check an incoming packet to see if it is part of the current knock
	 */
	public void checkPacket(Packet packet) {
		if (isAlive() && (packet instanceof TCPPacket)) {
			if (((Integer)portSeq.get(seqIdx)).intValue() == ((TCPPacket)packet).getDestinationPort()) {
				if (seqIdx == 0)
					firstKnock = (new Date()).getTime();
				seqIdx++;
				//System.out.println("Got next knock sequence port ("+((TCPPacket)packet).getDestinationPort()+") Seq. Idx: " + seqIdx + "  Knock Len: " + knockLength);
				if (isComplete()) {
					if (validSource(((IPPacket)packet).getSourceAddress())) {
						successAction.execute((IPPacket)packet,getKnockDescriptor().getKnockDesc());
					} else {
						badSourceAction.execute((IPPacket)packet,getKnockDescriptor().getKnockDesc());
					}

					reset();
				}
			}
		}
		else if (!isAlive()) {
			//System.out.println("resetting in checkPacket (not alive!)");
			reset();
		}
	}

	/**
	 * We are done only if all the knocks have been found!
	 */
	public boolean isComplete() {
		return seqIdx == knockLength;
	}

	/**
	 * Start over!
	 */
	public void reset() {
		//System.out.println("sequence knock is reset!");
		seqIdx = 0;
		firstKnock = 0;
	}

	/**
	 * Check to make sure that we've gotten at least the first packet
	 * of the knock, and that we haven't timed out,  if we haven't gotten
	 * the first packet then we can't have timed out, and we are alive,
	 * if we have started, make sure we haven't timed out!
	 */
	public boolean isAlive() {
		if (firstKnock != 0)
			return ((new Date()).getTime() - firstKnock) < timeout;
		else return true;
	}

	public int hashCode() {
		int hashcode = 0;
		
		for (int x = 0; x < portSeq.size() - (portSeq.size()%2); x+=2)
			hashcode ^= (((Integer)portSeq.get(x)).intValue() & 0xffff)<<16 | (((Integer)portSeq.get(x+1)).intValue() & 0xffff);
		if (portSeq.size()%2 == 1)
			hashcode ^= ((Integer)(portSeq.get(portSeq.size()-1))).intValue() & 0xffff;
		
		return hashcode;
	}

	public KnockDescriptor getKnockDescriptor() {
		return new PortSequenceKnockDescriptor(portSeq, 
											   successAction.getRuleset(), 
											   badSourceAction.getRuleset(), 
											   timeout);
	}
}
