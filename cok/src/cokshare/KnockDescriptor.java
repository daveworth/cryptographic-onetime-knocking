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

package cokshare;

import java.io.*;
import java.util.*;

/**
 * Abstract base class for all KnockDescriptors, defining basic
 * interface for all descriptors.
 */
public abstract class KnockDescriptor implements Serializable {

	/** Rules to execute on successful knock */
	protected COKRuleset successRules;

	/** Rules to executeon successful knock from bad source address */
	protected COKRuleset badSourceRules;

	/** Vector (of CIDRBlocks) which are valid sources for this knock */
	HashSet validSourceAddrs;

	protected KnockDescriptor() {
		successRules   = new COKRuleset();
		badSourceRules = new COKRuleset();

		validSourceAddrs = new HashSet();
	}

	/**
	 * A string describing the type of knock being passed about
	 *
	 * @return a String containing a descriptive name!
	 */
	public abstract String getKnockType();

	/**
	 * Return a string describing the knock!
	 *
	 * @return a String containing a description of the knock
	 */
	public abstract String getKnockDesc();

	/**
	 * Update a knock appropriately.
	 *
	 * @param desc Knock descriptor from which to update.
	 */
	public abstract void update(KnockDescriptor desc);

	/**
	* comparison operation for serialization
	*
	* @param obj Object to compare descriptor to
	*/
	public abstract boolean equals(Object obj);

	/**
	 * Accessor for Success Ruleset
	 */
	public COKRuleset getSuccessRules() { return successRules; }
	
	/**
	 * Setter for success Ruleset
	 */
	public void setSuccessRules(COKRuleset successRules) { 
		this.successRules = successRules; 
	}

	/**
	 * Accessor for bad source address Ruleset
	 */
	public COKRuleset getBadSourceRules() { return badSourceRules; }
	
	/**
	 * Setter for bad source address Ruleset
	 */
	public void setBadSourceRules(COKRuleset badSourceRules) {
		this.badSourceRules = badSourceRules;
	}

	/**
	 * Add a new valid source address
	 *
	 * @param cidr a CIDRBlock of valid addresses
	 */
	public void addValidSourceAddress(CIDRBlock cidr) {
		validSourceAddrs.add(cidr);
	}
	
	/**
	 * Remove a previously valid source address
	 *
	 * @param cidr a CIDRBlock to remove from the set of valid addresses
	 */
	public void remoteValidSourceAddress(CIDRBlock cidr) {
		validSourceAddrs.remove(cidr);
	}

	/** getter for valid Source Address list */
	public HashSet getValidSourceAddrs() { return validSourceAddrs; }
}
