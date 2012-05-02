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
import java.security.*;
import java.text.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import layout.TableLayout;

import cokshare.MDC;
import cokshare.UsefulFilter;

/**
 * UDP OTK Client hidden in a JPanel for easy inclusion in the
 * JTabbedPane above it in the applet or frame
 */
class UDPKnockerPanel extends JPanel {

	private JLabel labelHost;
	private JLabel labelPort;
	private JLabel labelOneTimePass;
	private JLabel labelMessage;
	private JLabel labelPassphrase;
	private JLabel labelNumber;
	private JLabel labelCalcWarning;
	private JLabel labelRulename;

	private JTextField textHost;
	private JTextField textOneTimePass;
	private JTextField textRulename;
	private JTextField textPort;
	private JTextField textNumber;

	private JPasswordField passPassphrase;

	private JButton buttonSend;
	private JButton buttonCalculate;

	private JComboBox listAlgorithm;

	private JSeparator sepKnockCalc;
	private JSeparator sepBottom;

	/**
	 * Constructor.  Builds layout and arranges Widgets in GUI..
	 */
	UDPKnockerPanel() {
		buildGUI();
	}

	private void buildGUI() {		

		double bord = 10;
		double hgap = 10;
		double vgap = 10;
		double fill = TableLayout.FILL;
		double pref = TableLayout.PREFERRED;

		double[][] table = {
			{bord, pref, hgap, fill, hgap, pref, hgap, pref, hgap},
			{bord, pref, vgap, pref, vgap, pref, vgap, pref, vgap, pref,vgap,pref, vgap, pref, vgap, pref, bord}
		};
		
		setLayout(new TableLayout(table));

		labelHost        = new JLabel("Host:");
		labelPort        = new JLabel("Port:");
		labelOneTimePass = new JLabel("One Time Password:");
		labelMessage     = new JLabel();
		labelPassphrase  = new JLabel("Passphrase:");
		labelNumber      = new JLabel("Number:");
		labelCalcWarning = new JLabel("WARNING: This is being run locally!");
		labelRulename    = new JLabel("Rulename:");

		setMessage(true, "Welcome to the UDP COKnocker Client");
		labelCalcWarning.setForeground(Color.RED);

		textHost        = new JTextField();
		textOneTimePass = new JTextField(30);
		textRulename    = new JTextField();
		textPort        = new JTextField(5);
		textNumber      = new JTextField(3);

		textPort.setDocument(new UsefulFilter(UsefulFilter.NUM));
		textNumber.setDocument(new UsefulFilter(UsefulFilter.NUM));

		passPassphrase  = new JPasswordField(20); 

		buttonSend      = new JButton("Send Knock >>");
		buttonCalculate = new JButton("Calculate OTP");

		String[] algorithms = {"MD5","SHA1"};
		listAlgorithm = new JComboBox(algorithms);
		listAlgorithm.setSelectedIndex(1);

		sepKnockCalc = new JSeparator();
		sepBottom    = new JSeparator();

		add(labelHost,        "1,1");
		add(textHost,         "3,1");
		add(labelPort,        "5,1");
		add(textPort,         "7,1");
		add(labelOneTimePass, "1,3");
		add(textOneTimePass,  "3,3");
		add(buttonSend,       "7,3");

		add(sepKnockCalc,     "1,5,7,5");

		add(labelPassphrase,  "1,7");
		add(passPassphrase,   "3,7");
		add(labelNumber,      "5,7");
		add(textNumber,       "7,7");

		add(labelRulename,    "1,9");
		add(textRulename,     "3,9");

		add(listAlgorithm,    "1,11");
		add(labelCalcWarning, "3,11,5,11");
		add(buttonCalculate,  "7,11");

		add(sepBottom,        "1,13,7,13");
		add(labelMessage,     "1,15,7,15");

		setPreferredSize(new Dimension(580,320));

		buttonSend.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (validateSendParams()) {
						try {
							DatagramSocket  socket;
							DatagramPacket  packet;
							InetAddress     address;
							String          message = textOneTimePass.getText();
							int port = 0;
							try {
								port = Integer.parseInt(textPort.getText());
							} catch (NumberFormatException e) {
								// Ignored as it should be caught in validateSendParams()
							}
							
							// Send One Time Knock
							socket = new DatagramSocket();
							address = InetAddress.getByName(textHost.getText());
							
							packet = new DatagramPacket(message.getBytes(), message.length(),
														address, port);
							socket.send(packet);
							socket.close();
							
						} catch (UnknownHostException e) {
							// Ignored as it should be caught in validateSendParams()
						} catch (Exception e) {
							setMessage(false, "ERROR: " + e.getMessage());
						}
						
					}
				}
			});
		
		buttonCalculate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (validateCalcParams()) {
						try {
							int cnt = -1;
							try {
								cnt = Integer.parseInt(textNumber.getText());
							} catch (NumberFormatException e) {
								// ignored as it should be caught in validateCalcParams()
							}
														
							String algo = (String)listAlgorithm.getSelectedItem();
							if (cnt > 0 && textRulename.getText().length() > 0) {
								String OTP = MDC.getOTPData(algo, textRulename.getText() + new String(passPassphrase.getPassword()), cnt).readablePasswords[0];
								textOneTimePass.setText(OTP);
							}
						} catch (NoSuchAlgorithmException e) {
							setMessage(false, "Error:" + e.getMessage());
						}
					}
				}
			});		
		
	}

	private void setMessage(boolean goodNews, String msg) {
		labelMessage.setForeground(goodNews ? Color.BLUE : Color.RED);
		labelMessage.setText(msg);
	}
	
	/**
	 * Validate parameters in text boxes for sending a One Time Knock
	 *
	 * Note: This has side effects in terms of the message label!
	 */
	private boolean validateSendParams() {

		String host = textHost.getText();
		String pass = textOneTimePass.getText();

		String errString = "ERROR: ";
		int errors = 0;
		boolean retval = true;
		try { 
			InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			retval = false;
			errString += e.getMessage();
			errors++;
		}

		String[] words = pass.split("\\s+");
		if (words.length != 6) {
			retval = false;
			if (errors > 0)
				errString += " and ";
			errString += "Invalid Passphrase!";
			errors++;
		}

		try {
			int port = Integer.parseInt(textPort.getText());
			if (port < 0 || port > 65535) { 
				retval = false;
				if (errors > 0)
					errString += " and ";
				errString += "Invalid Port Number";
				errors++;
			}
		} catch (NumberFormatException e) {
			retval = false;
			if (errors > 0)
				errString += " and ";
			errString += "Invalid Port Number";
			errors++;
		}

		if (errors > 0) {
			setMessage(false, errString);
		} else {
			setMessage(true, "Looks good, sending knock now!");
		}
		
		return retval;
	}

	/**
	 * Validate parameters in text boxes for calculating a One Time Knock
	 * from a passphrase and index
	 *
	 * Note: This has side effects in terms of the message label!
	 */
	private boolean validateCalcParams() {
		boolean retval = true;
		int errors = 0;
		String errString = "ERROR: ";

		if (passPassphrase.getPassword().length == 0) {
			retval = false;
			errString += "Blank Passphrase";
			errors++;
		}

		try {
			int passnum = Integer.parseInt(textNumber.getText());
			if (passnum < 1) {
				retval = false;
				if (errors > 0)
					errString += " and ";
				errString += "Invalid Password Number";
				errors++;
			}
		} catch (NumberFormatException e) {
			retval = false;
			if (errors > 0)
				errString += " and ";
			errString += "Invalid Password Number";
			errors++;
		}

		if (errors > 0) {
			setMessage(false, errString);
		} else {
			setMessage(true, "OK... Calculating One Time Password...");
		}

		return retval;
	}

}
