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

import java.awt.*;
import javax.swing.*;

/**
 * Just a nice wrapper for COKnocker's two interfaces.
 */
public class COKnocker {
	
	/**
	 * Mainline, simply calls argument parsers or launches the GUI
	 */
	public static void main(String[] args) {
		parseArgs(args);
	}	

	/**
	 * Parse commandline arguments and call the appropriate methods
	 */
	private static void parseArgs(String[] args) {
		if (args.length == 0)
			launchGUI();
		else {
			String cmd = args[0];
			
			if (cmd.equals("--help") || cmd.equals("-h"))
				usage();			
			else if (cmd.equals("--commandline") || cmd.equals("-c")) {
				launchCLI(args);
			} else {
				System.out.println("Illegal argument: " + cmd);
				usage();
			}
		}
	}

	/**
	 * Run the CLI and pass the arguments from main on...
	 */
	private static void launchCLI(String[] args) {
		clCOKnocker.startCommandline(args);
	}

	/**
	 * Run the spiffy GUI
	 */
	private static void launchGUI() {
		JFrame editor = new JFrame("COKnocker");
		Container pane = editor.getContentPane();
		Component panel = new COKnockerPanel();
		pane.add(panel);
		editor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		editor.setBounds(0,0,(int)panel.getPreferredSize().getWidth(),
						 (int)panel.getPreferredSize().getHeight());
		editor.show();
	}

	/**
	 * Print a simple usage statement
	 */
	private static void usage() {
		System.out.println("COKnocker Usage: %java -jar coknocker.jar <options>");
		System.out.println("Options: --commandline (-c) -> Run CLI");
		System.out.println("         --help        (-h) -> Display this fine verbage");
		System.out.println("  Empty options list runs GUI.");
		System.out.println();
		clCOKnocker.usage();
	}

	/** Declare away */
	private COKnocker() {}
}
