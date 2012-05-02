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

/**
 * Class defining a "DNS Knock" Descriptor which is a specialization
 * of <code>UDP_OTP_KnockDescriptor</code> which simply adds a knock domain
 * as described in <code>cokd.DNSKnock</code>
 *
 * @see cokshare.UDP_OTP_KnockDescriptor
 * @see cokd.DNSKnock
 */
public class DNSKnockDescriptor extends UDP_OTP_KnockDescriptor {

	/** Host suffix specifying the knock domain */
	protected String knockDomain;

	/** 
	 * Default constructor which sets the port to 53 permanently.
	 */
	public DNSKnockDescriptor() {
		super();
		
		this.port = 53;
	}

	/**
	 * Explicit constructor which takes all the necessary bits and
	 * builds the knock descriptor appropriately.  All of the
	 * parameters except <code>knockDomain</code> are exactly as they
	 * are defined in <code>UDP_OTP_KnockDescriptor</code>
	 *
	 * @param firstOTP     First OTP bytes
	 * @param successRules rules to execute on successful knock
	 * @param replayRules  rules to execute on replay attack
	 * @param rulename     rulename associated with knock
	 * @param algorithm    algorithm with which knock is encoded
	 * @param knockDomain  Domain name to associate with DNS knock
	 *
	 * @see cokshare.UDP_OTP_KnockDescriptor
	 */
	public DNSKnockDescriptor(byte[] firstOTP, 
							  COKRuleset successRules,
							  COKRuleset badSourceRules, 
							  COKRuleset replayRules, 
							  String rulename,
							  String algorithm, 
							  String knockDomain) {
		super(firstOTP, 53, successRules, badSourceRules, replayRules, 
			  rulename, algorithm);
		this.knockDomain = knockDomain;
	}

	/**
	 * Explicit constructor which takes all the necessary bits and
	 * builds the knock descriptor appropriately.  All of the
	 * parameters except <code>knockDomain</code> are exactly as they
	 * are defined in <code>UDP_OTP_KnockDescriptor</code>
	 *
	 * @param firstOTP     First OTP bytes
	 * @param nextOTP      nextOTP hash bytes
	 * @param successRules rules to execute on successful knock
	 * @param replayRules  rules to execute on replay attack
	 * @param rulename     rulename associated with knock
	 * @param algorithm    algorithm with which knock is encoded
	 * @param knockDomain  Domain name to associate with DNS knock
	 * @param oldKeys      set to track replay attacks
	 *
	 * @see cokshare.UDP_OTP_KnockDescriptor
	 */
	public DNSKnockDescriptor(byte[] firstOTP, byte[] nextOTP, 
							  COKRuleset successRules, 
							  COKRuleset badSourceRules,
							  COKRuleset replayRules, 
							  String rulename, String algorithm, 
							  String knockDomain, HashSet oldKeys) {
		super(firstOTP, nextOTP, 53, successRules, badSourceRules,
			  replayRules, rulename, algorithm, oldKeys);
		this.knockDomain = knockDomain;
	}

	/** knocktype accessor */
	public String getKnockType() { return "DNS Knock"; }
	
	/** knock description accessor */
	public String getKnockDesc() { return "DNS_"+knockDomain+"_"+rulename + "_" + algorithm; }

	/** knock domain accessor */
	public String getKnockDomain() { return knockDomain; }

	/** knock domain setter */
	public void setKnockDomain(String knockDomain) { this.knockDomain = knockDomain; }

	public void update(KnockDescriptor desc) {
		if (desc instanceof DNSKnockDescriptor) {
			DNSKnockDescriptor dnsdesc = (DNSKnockDescriptor)desc;

			setSuccessRules(dnsdesc.getSuccessRules());
			setBadSourceRules(dnsdesc.getBadSourceRules());
			setReplayRules(dnsdesc.getReplayRules());
		}
	}

	/**
	 * verify equality
	 */
	public boolean equals(Object obj) {
		boolean equals = false;

		if (obj != null && obj instanceof DNSKnockDescriptor) {

			DNSKnockDescriptor knockdesc = (DNSKnockDescriptor)obj;

			if (this.firstOTP.length == knockdesc.firstOTP.length &&
				this.port            == knockdesc.port            &&
				this.rulename.equals(knockdesc.rulename)          &&
				this.algorithm.equals(knockdesc.algorithm)        &&
				this.knockDomain.equals(knockdesc.getKnockDomain())) {

				int nonMatches = 0;
				for (int i = 0; i < firstOTP.length; i++)
					if (this.firstOTP[i] != knockdesc.firstOTP[i])
						nonMatches++;
				
				if (nonMatches == 0)
					equals = true;
			}
		}

		return equals;
	}
}
