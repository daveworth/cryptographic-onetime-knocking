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

package coknocker;

import java.net.*;
import java.io.*;
import java.security.*;

import cokshare.UsefulFilter;

/**
 * Quick and *very* dirty CLI for COKnocker, just to have one before
 * BlackHat Vegas '04
 */
class clCOKnocker {
	
	/** Build a new commandline by parsing and going */
	public static void startCommandline(String[] args) {
		parseArgs(args);
	}

	/**
	 * Extremely strict and STUPID commandline parser for COK's CLI
	 */
	private static void parseArgs(String[] args) {
		if (args.length == 1) usage();
		else {
			String knockType = args[1];
			if (knockType.equals("udp")) {
				if (args.length != 5) {
					System.out.println("Illegal argument count!");
					usage();					
				} else {
					String hostname = args[2];
					int    port     = Integer.parseInt(args[3]);
					String OTP      = args[4];

					UDPKnock(hostname, port, OTP);
				}
			} else if (knockType.equals("portseq")) {
				if (args.length != 4) {
					System.out.println("Illegal argument count!");
					usage();
				} else {
					String hostname = args[2];
					String portseq  = args[3];
				}
			} else {
				System.out.println("Unknown knock type: " + knockType);
				usage();
			}
		}
	}

	/**
	 * Send a UDP OTP Knock with appropriate params
	 *
	 * @param hostname Host running COKd
	 * @param port     Port on which the knock is bound
	 * @param OTP      OTP calculated elsewhere
	 */
	private static void UDPKnock(String hostname, int port, String OTP) {
		try {
			DatagramSocket  socket;
			DatagramPacket  packet;
			InetAddress     address;
			
			socket = new DatagramSocket();
			address = InetAddress.getByName(hostname);
			packet = new DatagramPacket(OTP.getBytes(), OTP.length(),
										address, port);
			socket.send(packet);
			socket.close();
		} catch (IOException e) {
			System.out.println("IOException (Unknown Host): " + e.getMessage());
		}
		
	}

	/**
	 * Send a port-sequence knock to a given hostname
	 *
	 * @param hostname Host running COKd
	 * @param portseq  Port Sequence String
	 */
	private static void portseqKnock(String hostname, String portseq) {
		try {
			InetAddress hostAddr = InetAddress.getByName(hostname.trim());
			Integer[] ports = UsefulFilter.parsePortSequenceString(portseq.trim());

			Socket sock;
			for (int x = 0; x < ports.length; x++) {
				try {
					sock = new Socket(hostAddr, ports[x].intValue());
				} catch (IOException e) {}
			}	

		} catch (IllegalArgumentException e) { 
			System.out.println("IllegalArgumentException: " + e.getMessage());
		} catch (java.net.UnknownHostException e) {
			System.out.println("UnknownHostException: " + e.getMessage());
		}
		
	}

	/**
	 * Display a simple usage statement.
	 */
	public static void usage() {
		System.out.println("COKnocker CLI Usage: %java -jar coknocker.jar -c [knock type] [knock type options]");
		System.out.println("  Knock Types: udp, portseq");
		System.out.println("  udp options:     <hostname> <port> <OTP>");
		System.out.println("  portseq options: <hostname> <port sequence>");
	}
	
	/** Declare away */
	private clCOKnocker() {}
}
