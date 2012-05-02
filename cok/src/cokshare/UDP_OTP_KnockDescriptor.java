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

public class UDP_OTP_KnockDescriptor extends KnockDescriptor {

	protected byte[] firstOTP;
	protected byte[] nextOTP;

	protected int port;

	protected COKRuleset replayRules;

	protected String rulename;
	protected String algorithm;

	protected HashSet oldKeys;

	public UDP_OTP_KnockDescriptor() {
		super();

		this.replayRules    = new COKRuleset();
		this.rulename       = new String();
		this.algorithm      = new String();
	}

	public UDP_OTP_KnockDescriptor(byte[] firstOTP, int port,
								   COKRuleset successRules, 
								   COKRuleset badSourceRules,
								   COKRuleset replayRules,
								   String rulename, String algorithm) {

		this.firstOTP = new byte[firstOTP.length];
		System.arraycopy(firstOTP, 0, this.firstOTP, 0, firstOTP.length);
		this.nextOTP  = new byte[firstOTP.length];
		System.arraycopy(nextOTP, 0, this.nextOTP, 0, nextOTP.length);

		this.port = port;
		this.successRules   = successRules;
		this.badSourceRules = badSourceRules;
		this.replayRules    = replayRules;
		this.rulename       = rulename;
		this.algorithm      = algorithm;
	}	

	public UDP_OTP_KnockDescriptor(byte[] firstOTP, byte[] nextOTP, int port,
								   COKRuleset successRules, 
								   COKRuleset badSourceRules,
								   COKRuleset replayRules,
								   String rulename, String algorithm, 
								   HashSet oldKeys) {
		this(firstOTP,port,successRules,badSourceRules,replayRules,
			 rulename,algorithm);

		this.nextOTP = new byte[nextOTP.length];
		System.arraycopy(nextOTP, 0, this.nextOTP, 0, nextOTP.length);
		this.oldKeys = new HashSet(oldKeys);
	}

	public byte[] getFirstOTP() { return firstOTP; }
	public void setFirstOTP(byte[] firstOTP) {
		this.firstOTP = new byte[firstOTP.length];
		System.arraycopy(firstOTP, 0, this.firstOTP, 0, firstOTP.length);
	}

	public byte[] getNextOTP() { return nextOTP; }
	public void setNextOTP(byte[] nextOTP) {
		this.nextOTP = new byte[nextOTP.length];
		System.arraycopy(nextOTP, 0, this.nextOTP, 0, nextOTP.length);
	}

	public int getPort() { return port; }
	public void setPort(int port) { this.port = port; }

	public COKRuleset getReplayRules() { return replayRules; }
	public void setReplayRules(COKRuleset replayRules) { 
		this.replayRules = replayRules;
	}

	public String getRulename() { return rulename; }
	public void setRulename(String rulename) { this.rulename = rulename; }

	public String getAlgorithm() { return algorithm; }
	public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

	public HashSet getOldKeys() { return oldKeys; }

	public String getKnockType() { return "UDP OTP Knock"; }
	public String getKnockDesc() { return "UDP_OTP_"+rulename + "_" + algorithm + "_" + port; }

	public void update(KnockDescriptor desc) {
		if (desc instanceof UDP_OTP_KnockDescriptor) {
			UDP_OTP_KnockDescriptor udesc = (UDP_OTP_KnockDescriptor)desc;
			
			setPort(udesc.getPort());
			setSuccessRules(udesc.getSuccessRules());
			setBadSourceRules(udesc.getBadSourceRules());
			setReplayRules(udesc.getReplayRules());
		}
	}

	public boolean equals(Object obj) {
		boolean equals = false;
		if ( obj != null && obj instanceof UDP_OTP_KnockDescriptor) {
			UDP_OTP_KnockDescriptor knockdesc = (UDP_OTP_KnockDescriptor)obj;
			if (this.firstOTP.length == knockdesc.firstOTP.length &&
				this.port            == knockdesc.port            &&
				this.rulename.equals(knockdesc.rulename)          &&
				this.algorithm.equals(knockdesc.algorithm)) {
				
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

	protected synchronized void writeObject(ObjectOutputStream stream) 
		throws IOException {
		stream.defaultWriteObject();
		stream.writeInt(nextOTP.length);
		for (int i=0; i<nextOTP.length; i++)
			stream.writeObject(new Byte(nextOTP[i]));

		stream.writeInt(firstOTP.length);
		for (int i=0; i<firstOTP.length; i++)
			stream.writeObject(new Byte(firstOTP[i]));
	}

	protected synchronized void readObject(ObjectInputStream stream) 
		throws IOException, ClassNotFoundException {

		stream.defaultReadObject();

		int nextOTPlen = stream.readInt();
		nextOTP = new byte[nextOTPlen];
		for (int i = 0; i < nextOTPlen; i++)
			nextOTP[i] = ((Byte)stream.readObject()).byteValue();

		int firstOTPlen = stream.readInt();
		firstOTP = new byte[firstOTPlen];
		for (int i = 0; i < firstOTPlen; i++)
			firstOTP[i] = ((Byte)stream.readObject()).byteValue();

	}

}
