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
import java.net.*;

import net.sourceforge.jpcap.net.*;

import cokshare.SimpleSyslogger;
import cokshare.COKRuleset;

/**
 * Abstract class allowing for actions to be taken upon reciept of knocks
 *  or other important events.
 */
public class KnockAction {

	/** Ruleset to execute */
	protected COKRuleset rules;

	/** Constructor */
	public KnockAction(COKRuleset rules) {
		this.rules = rules;
	}

	/** Executes the "appropriate" action on being called. 
	 * <p>
	 *<pre>
	 * Parse rules:
	 * replace __SRC_IP__ with the source IP, __SRC_PORT__ with the 
	 *    source port
	 * replace __DEST_IP__ with destination IP, __DEST_PORT__ with the 
	 *    destination port 
	 * replace __KNOCKDESC__ with the knock description of the knock 
	 *    calling this action
	 *
	 * __LOG__   starting a rule means to log it via syslog 
	 * __PRINT__ starting a rule means to print the string which 
	 *    follows to STDOUT (Deperecated)
	 *
	 * then call equiv of system() on the line if we are not 
	 *    Logging or Printing
	 * </pre>
	 */
	public void execute(IPPacket packet, String knockdesc) {
		Vector curRules = rules.getRules();
		Runtime runtime = null;

		for (int x = 0; x < curRules.size(); x++) {

			String rule = (String)curRules.get(x);
			rule = rule.trim();

			if (rule.length() > 0) {
			
				if (packet != null) {					
					rule = rule.replaceAll("__SRC_IP__",    packet.getSourceAddress());
					rule = rule.replaceAll("__DEST_IP__",   packet.getDestinationAddress());

					if (packet instanceof UDPPacket)
						rule = rule.replaceAll("__SRC_PORT__",  ""+((UDPPacket)packet).getSourcePort());
					else if (packet instanceof TCPPacket)
						rule = rule.replaceAll("__SRC_PORT__",  ""+((TCPPacket)packet).getSourcePort());

					if (packet instanceof UDPPacket)
						rule = rule.replaceAll("__DEST_PORT__", ""+((UDPPacket)packet).getDestinationPort());
					else if (packet instanceof TCPPacket)
						rule = rule.replaceAll("__DEST_PORT__", ""+((TCPPacket)packet).getDestinationPort());
				}

				if (knockdesc != null) {
					rule = rule.replaceAll("__KNOCKDESC__", knockdesc);
				}

				String logPrefix = "__LOG__";
				String printPrefix = "__PRINT__";

				if (rule.startsWith(logPrefix)) {
					SimpleSyslogger.syslog(rule.substring(logPrefix.length()).trim());
				} else if (rule.startsWith(printPrefix)) {
					System.out.println(rule.substring(printPrefix.length()).trim());
				} else {
					if (runtime == null)
						runtime = Runtime.getRuntime();
					try {
						runtime.exec(rule);
					} catch (java.io.IOException e) {
						System.out.println("DEBUG (KnockAction:execute) : " + e.getMessage());
					}
				}
			}
		}
	}

	/** ruleset accessor */
	public COKRuleset getRuleset() { return rules; }
}
