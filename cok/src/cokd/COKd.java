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
import java.util.prefs.*;
import java.security.*;
import java.net.*;
import java.io.*;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;

import net.sourceforge.jpcap.capture.*;
import net.sourceforge.jpcap.net.*;

import cokshare.*;

/**
 * Daemon for Port-Knocking in general, with a focus on one-time
 * knocking
 */
public class COKd extends UnicastRemoteObject 
	implements COKManager, Unreferenced {

	/** Set of knock descriptors used to build packet listener later */
	protected HashSet knockdescs;

	/** My own private RMI Registry */
	protected Registry myRMIRegistry;

	/** RMI's Default Port */
	protected static final int REGISTRY_PORT = 1099;

	/** Thread which wraps actual packet capture */
	protected PacketCaptureThread pcapThread;

	/** Stored preferences object */
	protected COKPrefs cokPrefs;
	
	// Here lie the implementations for a COKManager

	/**
	 * (Re)Initialize the daemon by creating or restarting the capture
	 * thread (which in turn rebuilds the current knock list
	 */
	private void InitializeCOKd() {
		//System.out.println("COKdaemon re-initialized!");
		SimpleSyslogger.syslog("COKdaemon re-initialized!");
		if (pcapThread == null) {
			pcapThread = new PacketCaptureThread(knockdescs,
												 cokPrefs.captureDevice);
			pcapThread.start();
		} else 
			pcapThread.restartCapture(knockdescs);	
	}

	/**
	 * Shutdown the COKd by writing knocks to preferences (NYI),
	 * unbinding the daemon, logging important info out, and leaving
	 *
	 * @throws RemoteException
	 */
	public void HaltCOKd() 
		throws RemoteException {

		if (pcapThread != null) {
			try {
				knockdescs = pcapThread.getKnockDescriptors();
				Preferences sysPrefs = Preferences.systemNodeForPackage(this.getClass());
				sysPrefs.clear();
				sysPrefs.putInt("KnockCount", knockdescs.size());
				SimpleSyslogger.syslog("Writing " + knockdescs.size() + " knock(s) to Preferences!");
				int cnt = 0;
				for (Iterator it = knockdescs.iterator(); it.hasNext(); ) {
					PrefChunker.putObjectInPreferences((KnockDescriptor)it.next(),
													   sysPrefs,
													   "Knock_"+cnt);
					cnt++;
				}			
			} catch (BackingStoreException e) {
				SimpleSyslogger.syslog("Error storing knocks to Preferences!  " + e.getMessage());
			} catch (IOException e) {
				SimpleSyslogger.syslog("Error storing knocks to Preferences!  " + e.getMessage());
			}

			// kill capture
			pcapThread.stopCapture();
			pcapThread = null;
		}

		try {
			String cokdURL = "//"+InetAddress.getLocalHost().getHostAddress()+"/COKManager";
			Naming.unbind(cokdURL);

			SimpleSyslogger.syslog("Exiting via call to HaltCOKd");

		} catch (java.net.UnknownHostException e) {
			SimpleSyslogger.syslog("Localhost is not found: " + e.getMessage());
		} catch (NotBoundException e) {
			SimpleSyslogger.syslog("COKd is not bound, exiting anyway - " + e.getMessage());
		} catch (MalformedURLException e) {
			// Should never happen!
			SimpleSyslogger.syslog(e.getMessage());
		}

		SimpleSyslogger.close();
		// Finally leave for real!
		System.exit(0);
	}

	/**
	 * Get an array of knocks currently being listened to by COKd's
	 * various rules
	 *
	 * @return all knocks currently being listened for
	 */	
	public KnockDescriptor[] getBoundKnocks() {
		KnockDescriptor[] retKnockDescs = new KnockDescriptor[0];
		if (pcapThread != null) {
			knockdescs = pcapThread.getKnockDescriptors();
			if (knockdescs != null) {
				retKnockDescs = new KnockDescriptor[knockdescs.size()];
				int x = 0;
				for (Iterator it = knockdescs.iterator(); it.hasNext(); ) {
					retKnockDescs[x++]=(KnockDescriptor)it.next();
				}
			}
		}

		//System.out.println("A client requested the BoundKnock list!");
		SimpleSyslogger.syslog("A client requested the BoundKnock list!");

		return retKnockDescs;
	}

	/**
	 * Sets a rule in COKd's listening set.
	 *
	 * @param knockdesc The knock to set actions associated with it
	 *
	 * @return An int specifying the status of the new ruleset.  0
	 * indicates that there was an error inserting the ruleset, 1
	 * indicates that the rule was inserted and no ruleset previously
	 * was associated with the port, 2 indicates that ruleset was
	 * previously associated with the port, and it has been updated, 3
	 * indicates that the passed ruleset was contained no rules, and
	 * the port has been unassociated!
	 */
	public int setKnock(KnockDescriptor knockdesc) {

		int retval = COKManager.RULE_ERROR;

		if (pcapThread != null) 
			knockdescs = pcapThread.getKnockDescriptors();

		for (Iterator it = knockdescs.iterator(); it.hasNext(); ) {
			KnockDescriptor checkdesc = (KnockDescriptor)it.next();
			if (checkdesc.equals(knockdesc)) {
				checkdesc.update(knockdesc);
				retval = COKManager.RULE_OVERRIDE;
				break;
			}
		}
		
		if (retval == COKManager.RULE_ERROR) {
			knockdescs.add(knockdesc);
			retval = COKManager.RULE_NEW;
		}

		InitializeCOKd();

		return retval;
	}

	/**
	 * Remove a rule in COKd's listening set.
	 *
	 * @param knockdesc A knock descriptor describe the knock to be removed!
	 *
	 * @return an int specifying if the knock existed or not: 0 means
	 * no, 3 means yes!
	 */
	public int removeKnock(KnockDescriptor knockdesc) {
		
		int retval = COKManager.RULE_ERROR;		

		if (pcapThread != null) 
			knockdescs = pcapThread.getKnockDescriptors();

		for (Iterator it = knockdescs.iterator(); it.hasNext(); ) {
			KnockDescriptor checkdesc = (KnockDescriptor)it.next();
			if (checkdesc.equals(knockdesc)) {
				knockdescs.remove(checkdesc);
				retval = COKManager.RULE_REMOVED;
				break;
			}
		}

		InitializeCOKd();

		return retval;
	}

	/**
	 * Called when no clients a referencing the object!
	 */
	public void unreferenced() {
		//System.out.println("All clients have disconnected!");
	}

	/**
	 * Run the COK-daemon from the commandline
	 */
	public static void main(String[] args) {

		// Create and install a security manager
		if (System.getSecurityManager() == null) {
		    System.setSecurityManager(new RMISecurityManager());
		}

		try {
			InetAddress localHost = InetAddress.getLocalHost();

			SimpleSyslogger.init();
			SimpleSyslogger.addListener(localHost);

			COKd daemon = new COKd(parseCmdLine(args));

			String cokdURL = "//"+localHost.getHostAddress()+"/COKManager";
			Naming.rebind(cokdURL, daemon);

			SimpleSyslogger.syslog("COKdaemon bound in registry by constructor! ("+cokdURL+")");

		} catch (Exception e) {
			System.out.println("DEBUG (COKd:main): " + e.getMessage());
			e.printStackTrace();
		}		
	}

	/** default constructor, uses default params */
	private COKd() 
		throws RemoteException, IOException, ClassNotFoundException {
		this(new COKPrefs());
	}

	/**
	 * Constructor (builds a pcap engine and does its thing!)
	 */
	private COKd(COKPrefs cokPrefs)
		throws RemoteException, IOException, ClassNotFoundException {
		super();
		
		this.cokPrefs = cokPrefs;
		if (cokPrefs.verbose)
			SimpleSyslogger.mirrorSTDOUT();

		SimpleSyslogger.syslog("Starting RMI registry on port " + REGISTRY_PORT + " ...");
		myRMIRegistry = LocateRegistry.createRegistry(REGISTRY_PORT);

		knockdescs = new HashSet();

		try {
			Preferences sysPrefs = Preferences.systemNodeForPackage(this.getClass());
			if (this.cokPrefs.clearStoredKnocks) {
				sysPrefs.clear();
				SimpleSyslogger.syslog("Clearing stored knocks!");
			} else if (this.cokPrefs.readStoredKnocks) {
				int knockcount = sysPrefs.getInt("KnockCount", 0);
				SimpleSyslogger.syslog("Loading " + knockcount + " knocks from Preferences!");
				for (int x = 0; x < knockcount; x++)
					knockdescs.add((KnockDescriptor)PrefChunker.getObjectFromPreferences(sysPrefs, "Knock_"+x));		
						
				if (knockcount > 0)
					InitializeCOKd();
			} else if (!this.cokPrefs.readStoredKnocks) {
				SimpleSyslogger.syslog("Ignoring stored knocks!");
			}
		} catch (BackingStoreException e) {
			//System.out.println("DEBUG (cokd:cokd) " + e.getMessage());
			SimpleSyslogger.syslog("DEBUG (cokd:cokd) " + e.getMessage());
		}
	}

	/**
	 * Parse command-line arguments badly... like really badly!
	 */
	private static COKPrefs parseCmdLine(String[] args) {
		COKPrefs cokPrefs = new COKPrefs();
		for (int x = 0; x < args.length; x++) {
			String arg = args[x];
			if (arg.equals("--debug") || arg.equals("-d") ||
				arg.equals("--verbose") || arg.equals("-v")) {
				cokPrefs.verbose = true;
			} else if (arg.equals("--clear") || arg.equals("-C")) {
				cokPrefs.clearStoredKnocks = true;
			} else if (arg.equals("--ignore") || arg.equals("-I")) {
				cokPrefs.readStoredKnocks = false;
			} else if (arg.equals("--interface") || arg.equals("-i")) {
				if (x+1<args.length)
					cokPrefs.captureDevice=args[++x];
				else {
					System.out.println("-i requires device argument");
					usage();
				}
			} else if (arg.equals("--help") || arg.equals("-h")) {
				usage();
			}  else {
				System.out.println("Invalid argument " + arg);
				usage();
			}
		}

		return cokPrefs;
	}

	/**
	 * Display some sort of help and exit
	 */
	private static void usage() {
		System.out.println("COKd Usage: %runCOKd.pl <options>");
		System.out.println("Options: --debug   (-d)                  : Print debugging output to STDOUT");
		System.out.println("         --verbose (-v)                  : same as --debug");
		System.out.println("         --interface <device> (-i <dev>) : Specify interface to listen on");
		System.out.println("         --clear   (-C)                  : Delete stored knocks on startup");
		System.out.println("         --ignore  (-I)                  : Ignore stored knocks on startup");
		System.out.println("         --help    (-h)                  : Print this lovely help screen");
		System.exit(1);
	}

	/**
	 * Inner preferences class
	 */
	private static class COKPrefs {
		/** Do we read knocks from Preferences? */
		public boolean readStoredKnocks = true;
		
		/** Do we destry stored knocks? */
		public boolean clearStoredKnocks = false;

		/** Do we produce output to STDOUT? */
		public boolean verbose = false;

		/** Device to capture packets on */
		public String captureDevice;
	}
}

/**
 * Thread in which the packet capturing occurs!
 */
class PacketCaptureThread extends Thread {

	protected static final int INFINITE = -1;
	protected static final int PACKET_COUNT = INFINITE;	
	protected String pcapFilter = "";

	protected String captureDevice;
	protected PacketHandler packetHandler;
	protected PacketCapture packetCapture;

	HashSet knockdescs;

	PacketCaptureThread(HashSet knockdescs, String captureDevice) {
		this.knockdescs = new HashSet(knockdescs);
		this.captureDevice = captureDevice;
	}

	PacketCaptureThread(HashSet knockdescs) {
		this.knockdescs = new HashSet(knockdescs);
	}

	public void run() {
		if (knockdescs.size() > 0) 
			beginCapture();
	}

	public HashSet getKnockDescriptors() {
		HashSet rethash = null;
		if (packetHandler != null)
			rethash = packetHandler.getKnockDescriptors();
		return rethash;
	}

	/**
	 * Actually begin the capturing process.
	 */
	private void beginCapture() {
		try {
			packetCapture = new PacketCapture();

			// later allow device to listen on to be specified.
			if (captureDevice == null || captureDevice.length() == 0) {
				SimpleSyslogger.syslog("Finding capture device... ");
				try {
					captureDevice = packetCapture.findDevice();
					SimpleSyslogger.syslog("Found device: " + captureDevice);
				} catch (CaptureDeviceNotFoundException e) {
					SimpleSyslogger.syslog("DEBUG (PacketCaptureThread:beginCapture): Could not find available device!");
					return;
				}
			} else {
				SimpleSyslogger.syslog("Using specified capture device: " + captureDevice);
			}

			SimpleSyslogger.syslog("Attempting to Open " + captureDevice + "... ");
			try {
				packetCapture.open(captureDevice, true);
				SimpleSyslogger.syslog("success!");
			} catch (CaptureDeviceOpenException e) {
				SimpleSyslogger.syslog("DEBUG (PacketCaptureThread:beginCapture): Failed to open " + captureDevice + "!  Make sure you are root, and the device exists!\n\t" + e.getMessage());
				return;
			}

			buildFilter();
			SimpleSyslogger.syslog("Setting filter on " + captureDevice + "... ["+pcapFilter+"]");
			try {
				packetCapture.setFilter(pcapFilter, true);
				SimpleSyslogger.syslog("success!");
			} catch (InvalidFilterException e) {
				SimpleSyslogger.syslog("DEBUG (PacketCaptureThread:beginCapture): Failed!  The filter string is invalid!" + e.getMessage());
			}

		} catch (AccessControlException e) {
			SimpleSyslogger.syslog("DEBUG (PacketCaptureThread:beginCapture): " + e.getMessage());
		}


		SimpleSyslogger.syslog("Adding packet listener... ");
		packetHandler = new PacketHandler(knockdescs);
		packetCapture.addPacketListener(packetHandler);
		
		SimpleSyslogger.syslog("Beginning packet capture... ");
		try {
			packetCapture.capture(PACKET_COUNT);
		} catch (CapturePacketException e) {
			SimpleSyslogger.syslog("DEBUG (PacketCaptureThread:beginCapture): An error occurred during packet capture!" + e.getMessage());
		}
	}

	/**
	 * "Restart" the capture session by rebuilding the pcap Filter
	 * string, deleting the old listener, and adding a new one
	 *
	 * @param knockdescs a Hashset containing the current knock
	 *  descriptors
	 */
	public void restartCapture(HashSet knockdescs) {
		this.knockdescs = new HashSet(knockdescs);
		packetCapture.removePacketListener(packetHandler);

		buildFilter();
		SimpleSyslogger.syslog("Setting new filter on " + captureDevice + "... ["+pcapFilter+"]");
		try {
			packetCapture.setFilter(pcapFilter, true);
			SimpleSyslogger.syslog("success!");
		} catch (InvalidFilterException e) {
			SimpleSyslogger.syslog("DEBUG (PacketCaptureThread:beginCapture): Failed!  The filter string is invalid!" + e.getMessage());
		}
		packetHandler = new PacketHandler(knockdescs);
		packetCapture.addPacketListener(packetHandler);
	}

	/**
	 * Stop packet capture by removing all listeners, though the
	 * device will remain open officially
	 */
	public void stopCapture() {
		this.knockdescs = null;
		packetCapture.removePacketListener(packetHandler);
		packetCapture.close();
	}
	
	/**
	 * Gather all of the current knock descriptors, parse the ports on
	 * which they listen, and build a nice filter for libpcap to chew
	 * on
	 */
	private void buildFilter() {
		HashSet TCPports = new HashSet();
		HashSet UDPports = new HashSet();

		pcapFilter = "";

		KnockDescriptor rawKnockDesc;
		for (Iterator it = knockdescs.iterator(); it.hasNext(); ) {
			rawKnockDesc=(KnockDescriptor)it.next();

			if (rawKnockDesc != null) {
						
				if (rawKnockDesc instanceof PortSequenceKnockDescriptor) {
					Vector portSeq = ((PortSequenceKnockDescriptor)rawKnockDesc).getPortSeq();

					if (portSeq != null) {
						for (int p = 0; p < portSeq.size(); p++)
							TCPports.add((Integer)portSeq.get(p));
								
					} 
				}
						
				else if (rawKnockDesc instanceof UDP_OTP_KnockDescriptor) {
					int p = ((UDP_OTP_KnockDescriptor)rawKnockDesc).getPort();
					UDPports.add(new Integer(p));
				}
						
				else {
					SimpleSyslogger.syslog("Unknown knock type: " + rawKnockDesc.getKnockDesc());
				}
			} 
		}

		if (TCPports.size() > 0) {					
			Iterator it = TCPports.iterator();
			pcapFilter += " tcp port " + ((Integer)it.next()).intValue();
			while(it.hasNext()) {
				pcapFilter += " or tcp port " + ((Integer)it.next()).intValue();
			}
		}

		if (UDPports.size() > 0) {
			Iterator it = UDPports.iterator();
			if (pcapFilter.trim().length() > 0)
				pcapFilter += " or ";
			pcapFilter += "udp port " + ((Integer)it.next()).intValue();
			while(it.hasNext()) {
				pcapFilter += " or udp port " + ((Integer)it.next()).intValue();
			}
		}
	}

}

/** 
 * Class which handles incoming packets and does the right thing with them
 */
class PacketHandler implements PacketListener {

	Vector PortSeqKnocks;
	Vector UDP_OTP_Knocks;
	Vector DNSKnocks;

	/**
	 * Constructor... takes knock from the hash, puts them in the queue
	 */
	PacketHandler(HashSet knockdescs) {

		PortSeqKnocks  = new Vector();
		UDP_OTP_Knocks = new Vector();
		DNSKnocks      = new Vector();
		
		Knock rawKnock;
		for (Iterator it= knockdescs.iterator(); it.hasNext();) {
			KnockDescriptor desc = (KnockDescriptor)it.next();

			rawKnock = null;
			if (desc instanceof PortSequenceKnockDescriptor) {
				rawKnock = new PortSequenceKnock((PortSequenceKnockDescriptor)desc);
				PortSeqKnocks.add(rawKnock);
			} else if (desc instanceof DNSKnockDescriptor) {
				rawKnock = new DNSKnock((DNSKnockDescriptor)desc);
				DNSKnocks.add(rawKnock);
			} else if (desc instanceof UDP_OTP_KnockDescriptor) {
				rawKnock = new UDP_OTP_Knock((UDP_OTP_KnockDescriptor)desc);
				UDP_OTP_Knocks.add(rawKnock);
								   
			} else {
				SimpleSyslogger.syslog(" >>> Got unknown knock type "+rawKnock.getKnockDescriptor().getKnockDesc());
			}
			
			if (rawKnock != null)
				SimpleSyslogger.syslog(rawKnock.getKnockDescriptor().getKnockDesc());

		}
	}

	/**
	 * Gather all of the active knock descriptors, and return them to
	 * COKd's RMI thread, in an effort to synchronize state
	 *
	 * @return a Hashset containing the knock descriptors from all of
	 *   the active knocks
	 */
	public HashSet getKnockDescriptors() {
		HashSet knockdescs = new HashSet();

		for (int x = 0; x < PortSeqKnocks.size(); x++)
			knockdescs.add(((PortSequenceKnock)PortSeqKnocks.get(x)).getKnockDescriptor());

		for (int x = 0; x < UDP_OTP_Knocks.size(); x++)
			knockdescs.add(((UDP_OTP_Knock)UDP_OTP_Knocks.get(x)).getKnockDescriptor());

		for (int x = 0; x < DNSKnocks.size(); x++)
			knockdescs.add(((DNSKnock)DNSKnocks.get(x)).getKnockDescriptor());
	
		return knockdescs;
	}

	/**
	 * Call the right handler for an incoming packet
	 *
	 * @param packet Packet to check and hand off...
	 */
	public void packetArrived(Packet packet) {

		if (packet instanceof TCPPacket) {
			for (int x = 0; x < PortSeqKnocks.size(); x++) {
				((Knock)PortSeqKnocks.get(x)).checkPacket(packet);
			}
		} else if (packet instanceof UDPPacket) {

			// Check for DNS Knocks...
			if (((UDPPacket)packet).getDestinationPort() == 53) {
				for (int x = 0; x < DNSKnocks.size(); x++) {
					((Knock)DNSKnocks.get(x)).checkPacket(packet);
				}
			}

			for (int x = 0; x < UDP_OTP_Knocks.size(); x++)
				((Knock)UDP_OTP_Knocks.get(x)).checkPacket(packet);

		}
		
	}

}
