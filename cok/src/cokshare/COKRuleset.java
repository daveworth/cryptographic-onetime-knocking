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

import java.util.*;
import java.io.*;

/**
 * A COKRuleset is a vector of strings which can later be parsed by a
 * <code>KnockAction</code> when appropriate.
 *
 * @see cokshare.KnockAction
 */
public class COKRuleset implements Serializable {
	/** Vector of Strings which are to be executed for a knock*/
	protected Vector rules;

	/**
	 * Default constructor, builds empty lists of rules.
	 */
	public COKRuleset() { rules = new Vector(); }

	/** 
	 * Constructor: Builds a ruleset!
	 * 
	 * @param rules an array of rules (Strings)
	 */
	public COKRuleset(String[] rules) {
		this.rules = new Vector(rules.length);
		for (int x = 0; x < rules.length; x++)
			this.rules.add(rules[x]);
	}

	/** Rules Accessor */
	public Vector getRules() { return rules; }

	/** Clear this ruleset */
	public void clearRules() { rules.clear(); }

	/** 
	 * Append a new rule to this ruleset
	 *
	 * @param rule A String to add to the ruleset
	 */
	public void addRule(String rule) { rules.addElement(rule); }

	/** Compare two COKRulesets for equality */
	public boolean equals(Object obj) {
		boolean equals = false;
		if (obj != null && obj.getClass().isAssignableFrom(getClass())) {
				COKRuleset ruleset = (COKRuleset)obj;
				if (this.rules.equals(ruleset.getRules()))
					equals = true;
			}
		return equals;
	}

}
