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

import javax.swing.text.*;
import java.util.*;

/**
 * Allows for filtering JTextFields in simple ways, thus far hostnames
 * and port sequence lists are supported, along with standars such as
 * numbers, alphabetics, and alphanumerics.
 */
public class UsefulFilter extends PlainDocument {

	/** Port Sequence Filter */
	public static final int SEQ       = 0;
	/** Hostname Filter */
	public static final int HOST      = 1;
	/** Numeric Filter */
	public static final int NUM       = 2;
	/** Alphabetic Filter */
	public static final int ALPHA     = 3;
	/** Alpha Numeric Filter */
	public static final int ALPHA_NUM = 4;
	/** CIDR Block Filter*/
	public static final int CIDR      = 5;
	/** CIDR/IP Listing Filter */
	public static final int CIDRIPLIST = 6;

	private static final String lowerChars =
        "abcdefghijklmnopqrstuvwxyz";
	private static final String upperChars =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String alphaChars = 
        lowerChars + upperChars;
	private static final String numChars = 
        "0123456789";
	private static final String alphanumChars = 
        alphaChars + numChars;
	
	private static final String seqChars  = 
		numChars + " ,-";
	private static final String hostChars =
		alphanumChars + ".-_";
	private static final String cidrChars = 
		numChars+"./";	
	private static final String cidriplistChars =
		cidrChars + " ,";

	private String acceptedChars;

	/** 
	 * Default constructor: builds an alpha-numeric filter
	 */
	public UsefulFilter() { this(ALPHA_NUM); }

	/** 
	 * Constructor
	 *
	 * @param type Integer specifying type from above... defaults to
	 * alpha numeric
	 */
	public UsefulFilter(int type) {
		switch(type) {
		case SEQ:
			acceptedChars = seqChars;
			break;
			
		case HOST:
			acceptedChars = hostChars;
			break;

		case ALPHA:
			acceptedChars = alphaChars;
			break;

		case NUM:
			acceptedChars = numChars;
			break;

		case CIDR:
			acceptedChars = cidrChars;
			break;

		case CIDRIPLIST:
			acceptedChars = cidriplistChars;
			break;

		case ALPHA_NUM:
		default:
			acceptedChars = alphanumChars;
			break;
		}
	}

	/**
	 * Necessary parsing routine for PlainDocument class...
	 */
	public void insertString (int offset, String  str, AttributeSet attr)
		throws BadLocationException {
		if (str == null) return;

		for (int i=0; i < str.length(); i++) {
			if (acceptedChars.indexOf(str.valueOf(str.charAt(i))) == -1)
				return;
		}

		super.insertString(offset, str, attr);
	}

	/**
	 * Routine to convert strings from convoluted, optionally comma
	 * separated, port lists including ranges, into an array of
	 * Integers
	 *
	 * @param inPortSeqStr is a string containing a list of ports in a
	 * format to be prettied up and then parsed.  Numbers are
	 * optionally comma separated meaning the following are all
	 * equivalent: <code>"53, 14", "53 14", and "53 , , ,,, , ,
	 * 14"</code> as are <code>"1 2 3, 4, 5", "1-5"</code>, and
	 * finally ranges many be decending as well: <code>"3 2 1" and
	 * "3-1"</code>
	 *
	 * @throws IllegalArgumentException if anything is wrong with the
	 * string
	 */
	public static Integer[] parsePortSequenceString(String inPortSeqStr) 
		throws IllegalArgumentException {		

		Integer[] ports;

		String portSeqStr = new String(inPortSeqStr);
		// Remote sets of spaces and commas
		portSeqStr = portSeqStr.replaceAll("\\s*,(?:\\s|,)*\\s*", " ");
		// remove spaces around dashes
		portSeqStr = portSeqStr.replaceAll("\\s*-\\s*", "-");
		// Make sets of spaces into one space
		portSeqStr = portSeqStr.replaceAll("\\s+", " ");

		String pattern = "^\\s*\\d+(?:\\s*-\\d+\\s*)?(?:\\s+\\d+(?:\\s*-\\d+\\s*)?)*\\s*$";
		if (portSeqStr.matches(pattern)) {
			
			String[] portstrs = portSeqStr.trim().split("\\s+");		   			

			Vector portVec = new Vector();
			for (int x = 0; x < portstrs.length; x++) {
				if (portstrs[x].matches("\\d+-\\d+")) {
					String[] range = portstrs[x].split("-");
					int beg = Integer.parseInt(range[0]);
					int end = Integer.parseInt(range[1]);

					if (beg < end)
						for (int y = beg; y <= end; y++)
							portVec.add(new Integer(y));
					else
						for (int y = beg; y >= end; y--)
							portVec.add(new Integer(y));

				} else {
					try {
						portVec.add(new Integer(portstrs[x]));
					} catch(NumberFormatException e) {}
				}
			}

			ports = new Integer[portVec.size()];
			for (int x = 0; x < ports.length; x++)
				ports[x] = (Integer)portVec.get(x);				

		} else {
			throw new IllegalArgumentException("String is not a valid Port Sequence String");
		}

		return ports;
	}


	/**
	 * Routine to convert strings from convoluted, optionally comma
	 * separated, CIDR-Block and/or IP address listing into HashSet of
	 * CIDRBlocks
	 *
	 * @param inCIDRIPSeqStr is a string containing a list of
	 * CIDR-Blocks or IP addresses in a format to be prettied up and
	 * then parsed.  
	 *
	 * @throws IllegalArgumentException if anything is wrong with the
	 * string
	 *
	 * @see CIDRBlock
	 */
	public static HashSet parseCIDRIPSequenceString(String inCIDRIPSeqStr) 
		throws IllegalArgumentException {		

		HashSet cidrSet = new HashSet();

		String cidripSeqStr = new String(inCIDRIPSeqStr);
		// Remote sets of spaces and commas
		cidripSeqStr = cidripSeqStr.replaceAll("\\s*,(?:\\s|,)*\\s*", " ");
		// Make sets of spaces into one space
		cidripSeqStr = cidripSeqStr.replaceAll("\\s+", " ");

		String pattern = "^\\s*(?:\\d+\\.){3}\\d+(?:/\\d+)?((?:\\s*(?:\\d+\\.){3}\\d+(?:/\\d+)?)?)*$";
		if (cidripSeqStr.matches(pattern)) {
			
			String[] cidrstrs = cidripSeqStr.trim().split("\\s+");		   			
			for (int x = 0; x < cidrstrs.length; x++)
				cidrSet.add(new CIDRBlock(cidrstrs[x]));

		} else {
			throw new IllegalArgumentException("String is not a valid list of CIDR-Blocks");
		}

		return cidrSet;
	}


	/**
	 * Bad testing routine, run me, it's fun! 
	 */
	public static void main(String[] args) {

		{
			String portSeqStr = "3";
			System.out.println("String: " + portSeqStr);
			Integer[] output = parsePortSequenceString(portSeqStr);
			System.out.print("Output: ");
			for (int x = 0; x < output.length; x++)
				System.out.print(output[x] + " ");
			System.out.println();
		}


		{
			String portSeqStr = "3 5 6";
			System.out.println("String: " + portSeqStr);
			Integer[] output = parsePortSequenceString(portSeqStr);
			System.out.print("Output: ");
			for (int x = 0; x < output.length; x++)
				System.out.print(output[x] + " ");
			System.out.println();
		}


		{
			String portSeqStr = "3, 5  ,, ,,,   ,  ,   6";
			System.out.println("String: " + portSeqStr);
			Integer[] output = parsePortSequenceString(portSeqStr);
			System.out.print("Output: ");
			for (int x = 0; x < output.length; x++)
				System.out.print(output[x] + " ");
			System.out.println();
		}


		{
			String portSeqStr = "1-5";
			System.out.println("String: " + portSeqStr);
			Integer[] output = parsePortSequenceString(portSeqStr);
			System.out.print("Output: ");
			for (int x = 0; x < output.length; x++)
				System.out.print(output[x] + " ");
			System.out.println();
		}


		{
			String portSeqStr = "3 5 -  9 3-1 11-11 6";
			System.out.println("String: " + portSeqStr);
			Integer[] output = parsePortSequenceString(portSeqStr);
			System.out.print("Output: ");
			for (int x = 0; x < output.length; x++)
				System.out.print(output[x] + " ");
			System.out.println();
		}

		{
			String cidripSeqStr = "192.168.1.0/24";
			System.out.println("String: " + cidripSeqStr);
			HashSet cidrset = parseCIDRIPSequenceString(cidripSeqStr);
			System.out.print("Output: ");
			for (Iterator it = cidrset.iterator(); it.hasNext(); )
				System.out.print((CIDRBlock)it.next() + " ");
			System.out.println();
		}


		{
			String cidripSeqStr = "192.168.1.0/24  ,,, ,,   192.168.0.50";
			System.out.println("String: " + cidripSeqStr);
			HashSet cidrset = parseCIDRIPSequenceString(cidripSeqStr);
			System.out.print("Output: ");
			for (Iterator it = cidrset.iterator(); it.hasNext(); )
				System.out.print((CIDRBlock)it.next() + " ");
			System.out.println();
		}

	}
}
