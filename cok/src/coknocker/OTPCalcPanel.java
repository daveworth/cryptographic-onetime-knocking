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
import java.awt.event.*;

import java.security.*;

import javax.swing.*;

import layout.TableLayout;

import cokshare.UsefulFilter;
import cokshare.MDC;

/**
 * a Nice quick to calculate OTPs for COK
 */
class OTPCalcPanel extends JPanel {

	private JLabel labelPassphrase;
	private JLabel labelNumber;
	private JLabel labelRuleSeed;
	private JLabel labelOTP;
	private JLabel labelMessage;

	private JPasswordField passPassphrase;

	private JTextField textNumber;
	private JTextField textRuleSeed;
	private JTextField textOTP;

	private JComboBox listAlgorithm;
	private JCheckBox checkRFCCompliant;

	private JButton buttonCalc;

	private JSeparator sepBottom;

	private boolean rfcCompliant = false;

	public OTPCalcPanel() {
		buildGUI();
	}
	
	private void buildGUI() {
		double bord = 10;
		double hgap = 10;
		double vgap = 10;
		double fill = TableLayout.FILL;
		double pref = TableLayout.PREFERRED;

		double[][] table = {
			{bord, pref, hgap, fill, hgap, pref, hgap, pref, bord},
			{bord, pref, vgap, pref, vgap, pref, vgap, pref, vgap, pref, bord}
		};
                
		setLayout(new TableLayout(table));

		labelPassphrase = new JLabel("Passphrase:");
		labelNumber     = new JLabel("Number:");
		labelRuleSeed   = new JLabel("Rulename/Seed:");
		labelOTP        = new JLabel("One-Time-Password:");
		labelMessage    = new JLabel();

		setMessage(false, "WARNING: This is being run Locally!");

		passPassphrase = new JPasswordField(20);
		
		textNumber   = new JTextField(3);
		textRuleSeed = new JTextField(10);
		textOTP      = new JTextField(30);

		buttonCalc = new JButton("Calculate OTP");

		String[] algorithms = {"MD5","SHA1"};
		listAlgorithm = new JComboBox(algorithms);
		listAlgorithm.setSelectedIndex(1);	textNumber.setDocument(new UsefulFilter(UsefulFilter.NUM));

		checkRFCCompliant = new JCheckBox(new AbstractAction("RFC Compliant?"){
				public void actionPerformed(ActionEvent evt) {
					JCheckBox cb = (JCheckBox)evt.getSource();    
					rfcCompliant = cb.isSelected();
				}
			});

		sepBottom = new JSeparator();

		add(labelPassphrase, "1,1");
		add(passPassphrase,  "3,1");
		add(labelRuleSeed,   "5,1");
		add(textRuleSeed,    "7,1");

		add(labelNumber,       "1,3");
		add(textNumber,        "3,3");
		add(listAlgorithm,     "5,3");
		add(checkRFCCompliant, "7,3");

		add(buttonCalc, "1,5");
		add(labelOTP,   "3,5");
		add(textOTP,    "5,5,7,5");
		
		add(sepBottom,  "1,7,7,7");

		add(labelMessage, "1,9,7,9");

		setPreferredSize(new Dimension(580,320));

		buttonCalc.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					try {
						int cnt = -1;
						try {
							cnt = Integer.parseInt(textNumber.getText());
						} catch (NumberFormatException e) {
							// ignored as it should be caught in validateCalcParams()
						}
														
						String algo = (String)listAlgorithm.getSelectedItem();
						if (passPassphrase.getPassword().length > 0 && 
							textRuleSeed.getText().length() > 0     && 
							cnt >= 1) {

							String ruleseed = textRuleSeed.getText().trim();
							if (rfcCompliant)
								ruleseed = ruleseed.toLowerCase();

							String OTP = MDC.getOTPData(algo, ruleseed + new String(passPassphrase.getPassword()), cnt).readablePasswords[0];
							textOTP.setText(OTP);
							setMessage(true, "Password Calculated");
						} 
						else if (passPassphrase.getPassword().length == 0)
							setMessage(false, "Empty Passphrases are not Allowed");
						else if (textRuleSeed.getText().length() == 0)
							setMessage(false, "Empty Rulenames/Seeds are not Allowed");
						else if (cnt < 1)
							setMessage(false, "Invalid Password Number, must be >= 1");
					} catch (NoSuchAlgorithmException e) {
						setMessage(false, "Error: " + e.getMessage());
					}
				}
			});

		
		
	}

	private void setMessage(boolean goodNews, String msg) {
		labelMessage.setForeground(goodNews ? Color.BLUE : Color.RED);
		labelMessage.setText(msg);
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
