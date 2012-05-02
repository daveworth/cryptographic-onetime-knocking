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

import java.util.regex.*;
import java.io.Serializable;

/**
 * A class to operate on CIDR Blocks
 */
public class CIDRBlock implements Serializable {

	private long min=0, max=0;
	private int masklen = 32;

	/** Declare away */
	private CIDRBlock() {}

	/**
	 * Constructor 
	 */
	CIDRBlock(String cidr) 
		throws IllegalArgumentException {

		cidr = cidr.trim();

		Pattern cidrPattern = Pattern.compile("^(?:\\d+\\.){3}\\d+/\\d+$");
		Matcher m = cidrPattern.matcher(cidr);

		if (m.matches() || isIP(cidr)) {			

			// dirty hack: assume it's an IP, if it's a CIDR change these
			String IP = cidr;

			if (m.matches()) {
				String[] cidrbits = cidr.split("/");
				IP = cidrbits[0];
				masklen = Integer.parseInt(cidrbits[1]);
			}
			// end dirty hack

			if (isIP(IP)) {
				if (masklen >= 0 && masklen <= 32) {
					max = min = IP2Long(IP);

					long mask = 0;
					for (int x = 0; x < 32-masklen; x++) {
						mask <<= 1;
						mask |= 1;
					}

					min &= ~mask;
					max |= mask;					

				} else
					throw new IllegalArgumentException("Invalid mask "+masklen+", it must be 0<= x <=32!");
			} else
				throw new IllegalArgumentException("Invalid IP address "+IP+"!");
				
		} else
			throw new IllegalArgumentException("CIDR format invalid!");

	}
	
	/**
	 * Check if this cidr block contains the IP address specified
	 *
	 * @param addr a string containing the IP address to check
	 *
	 * @throws IllegalArgumentException if the String provided is not
	 * a valid IP address
	 */
	public boolean contains(String addr) 
		throws IllegalArgumentException {		

		if (!isIP(addr))
			throw new IllegalArgumentException("Invalid IP address "+addr+"!");

		boolean retval = false;

		long iplong = IP2Long(addr);

		if (iplong >= min && iplong <= max)
			retval = true;

		return retval;
	}

	public boolean equals(CIDRBlock cidr) {
		boolean retval = false;

		if (min == cidr.min && max == cidr.max)
			retval = true;

		return retval;
	}

	public String toString() {
		return Long2IP(min)+"/"+masklen;
	}

	public static boolean isIP(String inaddr) {
		boolean retval = false;

		String addr = new String(inaddr);
		addr = addr.trim();

		Pattern ipPattern = Pattern.compile("^(?:\\d+\\.){3}\\d+$");
		Matcher m = ipPattern.matcher(addr);
		if (m.matches()) {
			String[] octets = addr.split("\\.");

			boolean outofbounds = false;

			for (int x = 0; x < octets.length; x++) {
				int octetval = Integer.parseInt(octets[x]);
				if (octetval < 0 || octetval > 255) {
					outofbounds=true;
					break;
				}
			}

			if (!outofbounds)
				retval = true;
		}

		return retval;
	}

	private static long IP2Long(String addr) 
		throws IllegalArgumentException {

		if (!isIP(addr))
			throw new IllegalArgumentException("Invalid IP address "+addr+"!");

		int[] octets = getIPOctets(addr);
		long retlong = 0;
		for (int x = 0; x < octets.length; x++) {
			retlong <<=8;
			retlong |= (octets[x] & 0xff);
		}

		return retlong;
	}

	private static String Long2IP(long inaddr) {
		long addr = inaddr;
		int[] octets = new int[4];
		for (int x = 3; x >= 0; x--) {
			octets[x] = (int)(addr & 0xff);
			addr >>= 8;
		}
		
		String retstr = "";
		for (int x = 0; x < octets.length-1; x++)
			retstr += octets[x] + ".";
		retstr += octets[octets.length-1];
		
		return retstr;
	}

	private static int[] getIPOctets(String addr)
		throws IllegalArgumentException {

		if (!isIP(addr))
			throw new IllegalArgumentException("Invalid IP " + addr);

		addr = addr.trim();
		String[] octets = addr.split("\\.");
		int[] retoctets = new int[octets.length];
		for (int x = 0; x < octets.length; x++)
			retoctets[x] = Integer.parseInt(octets[x]);

		return retoctets;
	}

	public static void main(String[] args) {
		{
			String IP = "129.24.244.21";
			System.out.println("isIP("+IP+") = " + isIP(IP));

			int[] octets = getIPOctets(IP);
			for (int x = 0; x < octets.length-1; x++)
				System.out.print(octets[x] + ".");
			System.out.println(octets[octets.length-1]);
		}

		{
			String IP = "129.24.244.256";
			System.out.println("isIP("+IP+") = " + isIP(IP));
		}

		{
			String IP = "129.24.244.254.111";
			System.out.println("isIP("+IP+") = " + isIP(IP));
		}

		{
			String cidrstr = "192.168.1.143/24";
			CIDRBlock block = new CIDRBlock(cidrstr);
			System.out.println(cidrstr + " - " + block);

			String inAddr = "192.168.1.10";
			System.out.println(cidrstr+" contains " + inAddr + " = " + block.contains(inAddr));

			inAddr = "192.168.1.255";
			System.out.println(cidrstr+" contains " + inAddr + " = " + block.contains(inAddr));

			inAddr = "192.168.1.0";
			System.out.println(cidrstr+" contains " + inAddr + " = " + block.contains(inAddr));

			inAddr = "192.168.0.0";
			System.out.println(cidrstr+" contains " + inAddr + " = " + block.contains(inAddr));
		}

		{
			String cidrstr = "192.168.1.1";
			CIDRBlock block = new CIDRBlock(cidrstr);
			System.out.println(cidrstr + " - " + block);

			String inAddr = "192.168.1.1";
			System.out.println(cidrstr+" contains " + inAddr + " = " + block.contains(inAddr));

			inAddr = "192.168.1.2";
			System.out.println(cidrstr+" contains " + inAddr + " = " + block.contains(inAddr));

			inAddr = "192.168.1.0";
			System.out.println(cidrstr+" contains " + inAddr + " = " + block.contains(inAddr));
		}
	}
}
