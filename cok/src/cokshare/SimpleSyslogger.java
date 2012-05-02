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

import java.text.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Simple syslogging done simply: Logging may either be atomic via
 * syslog(host, message) or one may setup listeners and use
 * syslog(message) which is broadcast to all listeners in batch mode.
 *
 * This should be compliant with RFC 3164 (Syslog)
 */ 
public class SimpleSyslogger {

	public static final int SYSLOG_PORT=514;

	private static boolean init = false;

	/** List of listeners */
	protected static Vector listeners;

	/** Our socket to the world */
	protected static DatagramSocket sock;

	protected static boolean mirrorSTDOUT = false;
	
	/** 
	 * Default constructor
	 */
	private SimpleSyslogger() {}

	public static void init() { 
		listeners = new Vector();
		try {
			sock = new DatagramSocket();
		} catch (SocketException e) {
			System.out.println("Cannot log to host Socket Exception!");
		}
		init = true;
	}

	/**
	 * Dump every message to stdout as well
	 */
	public static void mirrorSTDOUT() { mirrorSTDOUT = true; }

	/**
	 * Do not dump every message to stdout as well
	 */
	public static void unmirrorSTDOUT() { mirrorSTDOUT = false; }

	/**
	 * Add a new listener for syslog messages
	 *
	 * @param addr An InetAddress of the syslog host
	 */
	public static void addListener(InetAddress addr) {
		if (init)
			listeners.add(new SyslogListener(addr));
	}

	/**
	 * Add a new listener for syslog messages
	 *
	 * @param hostname Hostname of syslog host
	 */
	public static void addListener(String hostname) {
		if (init)
			listeners.add(new SyslogListener(hostname));
	}

	/**
	 * Clears the list of listeners
	 */
	public static void removeAllListeners() { 
		if (init)
			listeners.clear(); 
	}

	/**
	 * Send a message to all listening syslog hosts with Facility 1 (User Messages) and severity 6 (Informational)
	 *
	 * @param msg a String to send as a syslog mesasge
	 */
	public static void syslog(String msg) {
		syslog(1, 7, msg);
	}

	/**
	 * Generalized call to syslog, reference RFC 3164 for facility
	 * and severity numbers!
	 *
	 * @param facility Facility number
	 * @param severity Severity value
	 * @param msg      Actual message to syslog
	 */
	public static void syslog(int facility, int severity, String inmsg) {
		if (init) {
			for (int x = 0; x < listeners.size(); x++) {

				int pri = facility * 8 + severity;
                
				String smallDay = "MMM  d hh:mm:ss";
				String bigDay   = "MMM dd hh:mm:ss";

				String format = "";
				if(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) < 10)
					format = smallDay;
				else
					format = bigDay;

                Format formatter = new SimpleDateFormat(format);
				String date = formatter.format(Calendar.getInstance().getTime());
				

				SyslogListener listener = (SyslogListener)listeners.get(x);
				String hostname = listener.getListenerAddress().getCanonicalHostName();

				// XXX bogus XXX
				String tag = "[COK]:";

				String msg = "<"+pri+">"+date+" "+hostname+" "+tag+" "+inmsg;

				// Make sure the string is not too long!
				if (msg.length() > 1024)
					msg = msg.substring(0,1023);

				DatagramPacket logpacket = new DatagramPacket(msg.getBytes(), 
															  msg.length(), 
															  listener.getListenerAddress(), 
															  SYSLOG_PORT);

				if (mirrorSTDOUT) {
					String outmsg = date+" "+hostname+" "+tag+" "+inmsg;
					System.out.println(outmsg);
				}

				try {
					sock.send(logpacket);
				} catch (IOException e) {
					System.out.println("Cannot send syslogged message... IOException");
				}
			}
		}
	}

	/**
	 * End syslogging
	 */
	public static void close() { sock.close(); }

	/**
	 * SYslog to a given host
	 *
	 * @param host hostname of syslog server
	 * @param msg  Message to send tot he syslog server
	 */
	public static void syslog(String host, String msg) {
		try { 
			InetAddress addr = InetAddress.getByName(host);
			syslog(addr, msg);
		} catch (UnknownHostException e) {
			System.out.println("Unknown host " + host + "\n\t" + e.getMessage());
		}
	}

	/**
	 * Syslog to a given host!
	 *
	 * @param addr Hostname of syslog server
	 * @param msg  Message to send to syslog server
	 */
	public static void syslog(InetAddress addr, String msg) {
		try { 			
			DatagramPacket logpacket = new DatagramPacket(msg.getBytes(), msg.length(), addr, SYSLOG_PORT);
			DatagramSocket socket = new DatagramSocket();
		
			socket.send(logpacket);
			socket.close();
		} catch (SocketException e) {
			System.out.println("Socket Exception: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		}
	}

	/**
	 * Inner class specifying a syslog host (a listener for syslog messages)
	 */
	public static class SyslogListener {
		/** Address of syslgo server */
 		protected InetAddress addr;

		/** 
		 * Constructor 
		 *
		 * @param addr inet address of syslog hsot
		 */
		SyslogListener(InetAddress addr) {
			this.addr = addr;
		}

		/**
		 * Constructor
		 *
		 * @param hostname hostname of syslog host
		 */
		SyslogListener(String hostname) {
			try { 
				InetAddress addr = InetAddress.getByName(hostname);
				this.addr = addr;
			} catch (UnknownHostException e) {
				System.out.println("Unknown host " + hostname + "\n\t" + e.getMessage());
			}
		}

		/** 
		 * get inet address of host 
		 *
		 * @return inet address associated with this listener
		 */
		public InetAddress getListenerAddress() { return addr; }

	}

	/**
	 * Testing routine (in an effort to get Mac OSX's syslogd to listen)
	 */
	public static void main(String[] args) {
		init();
		try {
			InetAddress addr = InetAddress.getLocalHost();
			addListener(addr);
			syslog("This is a syslog message from SimpleSyslogger via built-in listener!");
			syslog(addr, "This is a syslog message from SimpleSyslogger via explicit host address");
		} catch (UnknownHostException e) {
			System.out.println("Hmm.. localhost is not found!");
		}
	}

}
