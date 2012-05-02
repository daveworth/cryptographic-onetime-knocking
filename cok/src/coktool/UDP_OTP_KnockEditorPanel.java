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

package coktool;

import java.util.*;
import java.text.*;

import java.rmi.*;
import java.security.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import layout.TableLayout;

import cokshare.MDC;
import cokshare.COKRuleset;
import cokshare.UsefulFilter;
import cokshare.UDP_OTP_KnockDescriptor;
import cokshare.DNSKnockDescriptor;

/**
 * Editing panel for UDP_OTP_Knocks and DNS Knocks
 */
class UDP_OTP_KnockEditorPanel extends JPanel {

	private JLabel labelPassphrase;
	private JLabel labelCount;
	private JLabel labelPort;
	private JLabel labelKnockDomain;
	private JLabel labelRulename;
	private JLabel labelMessage;

	private JPasswordField passPassphrase;

	private JTextField textCount;
	private JTextField textPort;
	private JTextField textRulename;
	private JTextField textKnockDomain;

	private JButton buttonCalculatePasswords;
	private JButton buttonEditSuccessRules;
	private JButton buttonEditReplayRules;
	private JButton buttonEditBadSourceRules;
	private JButton buttonEditValidSourceList;
	private JButton buttonDone;

	private JComboBox listAlgorithm;

	private JSeparator sepBottom;

	private byte[] firstOTP;

	private UDP_OTP_KnockDescriptor udpKnockDesc = null;
	private DNSKnockDescriptor      dnsKnockDesc = null;

	public final static int UDP_OTP_EDITOR = 1;
	public final static int DNS_EDITOR     = 2;
	
	private int EditorType = 0;

	UDP_OTP_KnockEditorPanel(UDP_OTP_KnockDescriptor udpKnockDesc) {
		this.udpKnockDesc  = udpKnockDesc;

		EditorType = UDP_OTP_EDITOR;

		buildGUI();
		populateGUI();
	}

	UDP_OTP_KnockEditorPanel(DNSKnockDescriptor dnsKnockDesc) {
		this.dnsKnockDesc  = dnsKnockDesc;

		EditorType = DNS_EDITOR;

		buildGUI();
		populateGUI();
	}

	private void buildGUI() {
		double bord = 10;
		double hgap = 10;
		double vgap = 10;
		double fill = TableLayout.FILL;
		double pref = TableLayout.PREFERRED;

		double[][] tableParams = {
			{bord,pref,hgap,pref,hgap,pref,hgap,pref,hgap,pref,hgap,pref,hgap,bord},
			{bord,pref,vgap,pref,vgap,pref,vgap,pref,vgap,pref,vgap,pref,bord}
		};
		setLayout(new TableLayout(tableParams));

		labelPassphrase  = new JLabel("Passphrase:");
		labelCount       = new JLabel("Count:");
		labelPort        = new JLabel("Port:");
		labelKnockDomain = new JLabel("Knock Domain:");
		labelRulename    = new JLabel("Rulename:");
		labelMessage     = new JLabel();

		labelCount.setHorizontalAlignment(JLabel.RIGHT);

		if (EditorType == UDP_OTP_EDITOR)
			setMessage(true, "UDP OTP Knock Editor");
		else if (EditorType == DNS_EDITOR)
			setMessage(true, "DNS Knock Editor");
		
		passPassphrase = new JPasswordField(30);

		textPort  = new JTextField(5);
		textCount = new JTextField(3);
		textRulename    = new JTextField();
		textKnockDomain = new JTextField();	
		textPort.setDocument(new UsefulFilter(UsefulFilter.NUM));
		textCount.setDocument(new UsefulFilter(UsefulFilter.NUM));
	
		buttonCalculatePasswords  = new JButton("Calculate OTP List");
		buttonEditSuccessRules    = new JButton("Edit Success Rules");
		buttonEditReplayRules     = new JButton("Edit Replay Rules");
		buttonEditBadSourceRules  = new JButton("Edit Bad Source Rules");
		buttonEditValidSourceList = new JButton("Edit Valid Source List");
		buttonDone                = new JButton("Done >>");

		String[] algorithms = {"MD5","SHA1"};
		listAlgorithm = new JComboBox(algorithms);
		listAlgorithm.setSelectedIndex(1);

		sepBottom = new JSeparator();

		add(labelRulename,   "1,3");
		add(textRulename,    "3,3");
		add(labelCount,      "9,3");
		add(textCount,       "11,3");

		if (EditorType == UDP_OTP_EDITOR) {
			add(labelPort,       "5,3");
			add(textPort,        "7,3");
		} else if (EditorType == DNS_EDITOR) {
			add(labelKnockDomain, "5,3");
			add(textKnockDomain,  "7,3");
		} else {
			// XXX OTHER? XXX
		}

		add(labelPassphrase,           "1,1");
		add(passPassphrase,            "3,1,11,1");


		add(listAlgorithm,             "1,5");
		add(buttonCalculatePasswords,  "3,5");
		add(buttonEditSuccessRules,    "5,5");
		add(buttonEditReplayRules,     "7,5");
		
		add(buttonEditBadSourceRules,  "5,7");
		add(buttonEditValidSourceList, "7,7");
		add(buttonDone,                "11,7");

		add(sepBottom,                 "1,9,11,9");
		
		add(labelMessage,              "1,11,11,11");

		setPreferredSize(new Dimension(760,200));

		buttonCalculatePasswords.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {

					String passwd = new String(passPassphrase.getPassword());
					String rulename = textRulename.getText();

					if (passwd.length() > 0 && rulename.length() > 0) {
						try {
							MDC.OTPData data = MDC.getOTPData((String)listAlgorithm.getSelectedItem(),
														  rulename+passwd,
														  Integer.parseInt(textCount.getText())
														  );

							JFrame frameDisplayPassphrases = new JFrame("Calculated Passphrases for rule: " + rulename);
							Container pane = frameDisplayPassphrases.getContentPane();
							pane.add(new OTPListPanel(data.readablePasswords));
							frameDisplayPassphrases.setBounds(200,200,
															  (int)pane.getPreferredSize().getWidth(),
															  (int)pane.getPreferredSize().getHeight());
							frameDisplayPassphrases.show();

						} catch (NumberFormatException e) {
							// should be caught because of formatting!
									setMessage(false, "Error - Number Format: " + e.getMessage());
						} catch (NoSuchAlgorithmException e) {
							// should be caught because we force the algorithm
							setMessage(false, "Error - No Such Algorithm: " + e.getMessage());
						} catch (IllegalArgumentException e) {
							setMessage(false, "Error - Illegal Argument" + e.getMessage());
						}
						
					} else if (passwd.length() == 0)
						setMessage(false, "Error - Empty Password");
					else if (rulename.length() == 0)
						setMessage(false, "Error - Empty Rulename");
				}
			});

		buttonEditSuccessRules.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					COKRuleset rules = null;
					if (EditorType == UDP_OTP_EDITOR) {
						rules = udpKnockDesc.getSuccessRules();
					} else if (EditorType == DNS_EDITOR) {
						rules = dnsKnockDesc.getSuccessRules();
					} else {
						// XXX Do something XXX
					}
						
					COKTool.buildFrame("Edit Success Rules", new RuleEditorPanel(rules));
				}
			});

		buttonEditReplayRules.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					COKRuleset rules = null;
					if (EditorType == UDP_OTP_EDITOR) {
						rules = udpKnockDesc.getReplayRules();
					} else if (EditorType == DNS_EDITOR) {
						rules = dnsKnockDesc.getReplayRules();
					} else {
						// XXX Do something XXX
					}

					COKTool.buildFrame("Edit Replay Rules", new RuleEditorPanel(rules));
				}
			});

		buttonEditValidSourceList.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					HashSet validSourceAddrs = null;
					if (EditorType == UDP_OTP_EDITOR) {
						validSourceAddrs = udpKnockDesc.getValidSourceAddrs();
					} else if (EditorType == DNS_EDITOR) {
						validSourceAddrs = dnsKnockDesc.getValidSourceAddrs();
					} else {
						// XXX Do something XXX
					}

					COKTool.buildFrame("Edit Valid Source Address List", new CIDRListEditorPanel(validSourceAddrs));
				}
			});


		buttonEditBadSourceRules.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					COKRuleset rules = null;
					if (EditorType == UDP_OTP_EDITOR) {
						rules = udpKnockDesc.getBadSourceRules();
					} else if (EditorType == DNS_EDITOR) {
						rules = dnsKnockDesc.getBadSourceRules();
					} else {
						// XXX Do something XXX
					}

					COKTool.buildFrame("Edit Bad Source Rules", new RuleEditorPanel(rules));
				}
			});

		
		buttonDone.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
										
					MDC.OTPData data = null;
					String errStr = "";

					try {
						data = MDC.getOTPData((String)listAlgorithm.getSelectedItem(),
													  textRulename.getText()+(new String(passPassphrase.getPassword())),
													  Integer.parseInt(textCount.getText())
													  );
					// copy OTP over
					firstOTP = new byte[data.firstOTP.length];
					System.arraycopy(data.firstOTP, 0, firstOTP,  0, firstOTP.length);
					
					} catch (NoSuchAlgorithmException e) {
						setMessage(false, "No Such Algorithm" + e.getMessage());
					} catch (NumberFormatException e) {
						setMessage(false, "Invalid Count!");;
					} catch (IllegalArgumentException e) {
						setMessage(false, "Illegal Argument" + e.getMessage());
					}

					if (validateDescriptorInfo()) {
						JFrame parentFrame = (JFrame)getRootPane().getParent();
						parentFrame.setVisible(false);
						parentFrame.dispose();
					}
				}
			});
	}

	private boolean validateDescriptorInfo() {

		boolean valid = false;
		int errs = 0;
		String errStr = "";

		int port = -1;
		try {		   
			if (EditorType == UDP_OTP_EDITOR)
				port = Integer.parseInt(textPort.getText());
		} catch (NumberFormatException e) { 
			if (errStr.length() > 0)
				errStr+=", ";
			errStr+="Invalid Port Number"; 
		}

		String algo = (String)listAlgorithm.getSelectedItem();
		String rulename = textRulename.getText();
			
		String knockDomain = "";
		if (EditorType == DNS_EDITOR)
			knockDomain = textKnockDomain.getText();

		if (rulename.length() == 0) {
			errs++;
			errStr = "No Rulename";
		}

		if ((new String(passPassphrase.getPassword())).length() == 0) {
			errs++;
			if (errStr.length() > 0)
				errStr+=", ";
			errStr += "No passphrase";
		}

		if (firstOTP == null || firstOTP.length != 8) {

			errs++;

			if (errStr.length() > 0)
				errStr+=", ";

			errStr += "OTP is bad: ";
			if (firstOTP == null) {
				errStr += "OTP is null";
			} else {
				errStr += "bad OPT length - " + firstOTP.length;
			}			
		}

		if (EditorType == UDP_OTP_EDITOR && port < 0 || port > 65535) {
			errs++;

			if (errStr.length() > 0)
				errStr+=", ";		   
			errStr += (port == -1) ? "Port is Empty" : "Port is bad: " + port;
		}

		if (!MDC.validAlgorithm(algo)) {
			errs++;

			if (errStr.length() > 0)
				errStr+=", ";
			errStr += "Algorithm is bad: " + algo;
		}

		if (EditorType == DNS_EDITOR && knockDomain.length() == 0) {
			errs++;

			if (errStr.length() > 0)
				errStr+=", ";
			errStr += "No Knock Domain";
		}

		if (errs == 0)
			valid = true;

		if (valid) {
			if (EditorType == UDP_OTP_EDITOR) {
				udpKnockDesc.setFirstOTP(firstOTP);
				udpKnockDesc.setNextOTP(firstOTP);
				udpKnockDesc.setPort(port);
				udpKnockDesc.setRulename(rulename);
				udpKnockDesc.setAlgorithm(algo);
			} else if (EditorType == DNS_EDITOR) {
				dnsKnockDesc.setFirstOTP(firstOTP);
				dnsKnockDesc.setNextOTP(firstOTP);
				dnsKnockDesc.setRulename(rulename);
				dnsKnockDesc.setAlgorithm(algo);
				dnsKnockDesc.setKnockDomain(knockDomain);
			} else {
				// XXX ERROR! XXX
			}

			// will actually never be seen!
			errStr = "Everything looks good, building knock now!";
		}

		setMessage(valid, errStr);

		return valid;

	}

	private void populateGUI() {
		if (EditorType == UDP_OTP_EDITOR &&
			udpKnockDesc.getRulename().length() > 0) {
			textRulename.setText(udpKnockDesc.getRulename());				
			textPort.setText(""+udpKnockDesc.getPort());
		} else if (EditorType == DNS_EDITOR &&
				   dnsKnockDesc.getRulename().length() > 0) {
			textRulename.setText(dnsKnockDesc.getRulename());
			textKnockDomain.setText(dnsKnockDesc.getKnockDomain());
		}
	}

	private void setMessage(boolean goodNews, String msg) {
		labelMessage.setForeground(goodNews ? Color.BLUE : Color.RED);
		labelMessage.setText(msg);
	} 

	/** declare away */
	private UDP_OTP_KnockEditorPanel() {}
}
