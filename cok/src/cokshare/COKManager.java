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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI Definition interface for the COKd project.
 */
public interface COKManager extends Remote {

	/** An error has occurred in manipulating a rule */
	public static final int RULE_ERROR    = 0;
	/** The rule being added was new */
	public static final int RULE_NEW      = 1;
	/** The rule being added was not new, so it was updated correctly */
	public static final int RULE_OVERRIDE = 2;
	/** The rule being removed existed, and was removed */
	public static final int RULE_REMOVED  = 3;

	/**
	 * Shutdown the COKd
	 *
	 * @throws RemoteException
	 */
	void HaltCOKd() throws RemoteException;

	/**
	 * Get an array of knocks currently being listened to by COKd's
	 * various rules
	 *
	 * @return an array of knocks which are bound to rulesets
	 *
	 * @throws RemoteException
	 */
	KnockDescriptor[] getBoundKnocks() throws RemoteException;

	/**
	 * Sets a rule in COKd's listening set.
	 *
	 * @param knockdesc A knock descriptor describing the knock to be added
	 *
	 * @return An int specifying the status of the new ruleset.
	 * <code>RULE_ERROR</code> indicates that there was an error
	 * inserting the ruleset, <code>RULE_NEW</code> indicates that the
	 * rule was inserted and no ruleset previously was associated with
	 * the port, <code>RULE_OVERRIDE</code> indicates that ruleset was
	 * previously associated with the port, and it has been updated
	 *
	 * @throws RemoteException
	 */
	int setKnock(KnockDescriptor knockdesc) throws RemoteException;

	/**
	 * Remove a rule in COKd's listening set.
	 *
	 * @param knockdesc A knock descriptor describe the knock to be removed!
	 *
	 * @return an int specifying if the knock existed or not:
	 * <code>RULE_ERROR</code> means no, <code>RULE_REMOVED</code>
	 * means yes!
	 */
	int removeKnock(KnockDescriptor knockdesc) throws RemoteException;
}
