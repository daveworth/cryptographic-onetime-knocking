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
import cokshare.COKRuleset;
import cokshare.CIDRBlock;

/**
 * Class defining a knock which is meant to then trigger an event
 */
public abstract class Knock {

	/** Action to exec on success */
	protected KnockAction successAction;

	/** Action to execute when the source for a knock is invalid */
	protected KnockAction badSourceAction;

	/** Vector (of CIDRBlocks) which are valid sources for this knock */
	HashSet validSourceAddrs;

	protected Knock(COKRuleset successRules, COKRuleset badSourceRules,
					HashSet validSourceAddrs) {
		this.successAction = new KnockAction(successRules);
		this.badSourceAction = new KnockAction(badSourceRules);
		if (validSourceAddrs != null)
			this.validSourceAddrs = new HashSet(validSourceAddrs);
		else
			this.validSourceAddrs = new HashSet();
	}

	/** method called to investigate the usefulness of a packet */
	public abstract void checkPacket(Packet packet);

	/** produce a hashcode to make things work right! */
	public abstract int hashCode();

	/** Get a descriptor for this knock */
	public abstract KnockDescriptor getKnockDescriptor();

	/** Get the associated success action */
	public KnockAction getSuccessAction() { return successAction; }

	/** Get the associated bad source action */
	public KnockAction getBadSourceAction() { return badSourceAction; }

	/**
	 * Checks if a given IP address is valid for this knock
	 *
	 * @param addr String containing IP address to check for validity
	 *
	 * @throws IllegalArgumentException if addr is not a valid IP address
	 */
	protected boolean validSource(String addr) 
		throws IllegalArgumentException {
		
		if (!CIDRBlock.isIP(addr))
			throw new IllegalArgumentException("Invalid IP address: " + addr);
		
		boolean retval = false;
		if (validSourceAddrs != null && validSourceAddrs.size() > 0) {
			for (Iterator it = validSourceAddrs.iterator(); it.hasNext(); ) {
				if (((CIDRBlock)it.next()).contains(addr)) {
					retval = true;
					break;
				}
			}
		} else
			retval = true;

		return retval;
	}
}
