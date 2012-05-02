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

public class PortSequenceKnockDescriptor extends KnockDescriptor {
	protected Vector portSeq;
	protected long timeout;

	public PortSequenceKnockDescriptor() {
		super();

		portSeq        = new Vector();
	}

	public PortSequenceKnockDescriptor(Vector portSeq, COKRuleset successRules,
									   COKRuleset badSourceRules, long timeout) {
		this.portSeq        = portSeq;
		this.successRules   = successRules;
		this.badSourceRules = badSourceRules;
		this.timeout        = timeout;
	}

	public Vector getPortSeq() { return portSeq; }
	public void setPortSeq(Vector portSeq) { this.portSeq = new Vector(portSeq); }

	public long getTimeout() { return timeout; }
	public void setTimeout(long timeout) { this.timeout = timeout; }

	public String getKnockType() { return "Port Sequence Knock"; }
	public String getKnockDesc() {
		String retval = "PortSeq_";
		for (int x = 0; x < portSeq.size(); x++)
			retval += portSeq.get(x) + "_";
		retval += timeout;
		
		return retval;
	}

	public void update(KnockDescriptor desc) {
		if (desc instanceof PortSequenceKnockDescriptor) {
			PortSequenceKnockDescriptor psdesc 
				= (PortSequenceKnockDescriptor)desc;

			setPortSeq(psdesc.getPortSeq());
			setTimeout(psdesc.getTimeout());
		}
	}

	public boolean equals(Object obj) {
		boolean equals = false;
		if (obj != null && obj instanceof PortSequenceKnockDescriptor) {
			PortSequenceKnockDescriptor knockdesc = 
				(PortSequenceKnockDescriptor)obj;

			if (this.portSeq.equals(knockdesc.portSeq)            &&
				this.successRules.equals(knockdesc.successRules)  &&
				this.timeout == knockdesc.timeout)
				equals = true;
			
		}
		return equals;
	}
}
