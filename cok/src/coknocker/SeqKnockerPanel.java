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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import layout.TableLayout;

import cokshare.UsefulFilter;

/**
 * Port Sequence Client hidden in a JPanel for easy inclusion in the
 * JTabbedPane above it in the frame or applet
 */
class SeqKnockerPanel extends JPanel {

	private JLabel labelHost;
	private JLabel labelSequence;
	private JLabel labelMessage;

	private JTextField textHost;
	private JTextField textSequence;

	private JButton buttonSend;
	
	private JSeparator sepMessage;

	private InetAddress hostAddr;
	private Integer[] ports;

	SeqKnockerPanel() {
		buildGUI();
	}
	
	public void buildGUI() {
		double bord = 10;
		double hgap = 10;
		double vgap = 10;
		double fill = TableLayout.FILL;
		double pref = TableLayout.PREFERRED;

		double[][] table = {
			{bord,pref,hgap,fill,hgap,pref,bord},
			{bord,pref,vgap,pref,vgap,pref,vgap,pref,vgap,pref,bord}
		};
		
		setLayout(new TableLayout(table));

		labelHost     = new JLabel("Host:");
		labelSequence = new JLabel("Port Sequence:");
		labelMessage  = new JLabel();

		setMessage(true, "Welcome to the Port-Sequence COKnocker Client");

		textHost     = new JTextField();
		textSequence = new JTextField(30);

		textHost.setDocument(new UsefulFilter(UsefulFilter.HOST));
		textSequence.setDocument(new UsefulFilter(UsefulFilter.SEQ));

		buttonSend = new JButton("Send Knock >>");

		sepMessage = new JSeparator();

		add(labelHost,     "1,1");
		add(textHost,      "3,1,5,1");

		add(labelSequence, "1,3");
		add(textSequence,  "3,3,5,3");

		add(buttonSend,    "5,5");

		add(sepMessage,    "1,7,5,7");
		
		add(labelMessage,  "1,9,5,9");

		setPreferredSize(new Dimension(300,300));

		buttonSend.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {

					String message = "Knocking Now: ";

					if (validateParams()) {

						Socket sock;
						for (int x = 0; x < ports.length; x++) {
							try {
								sock = new Socket(hostAddr,ports[x].intValue());
							} catch (IOException e) {
								message += ports[x] + " ";
								setMessage(true, message);
							}
						}

						message+="DONE!";
						setMessage(true, message);
					}
				}
			});

	}

	private void setMessage(boolean goodNews, String msg) {
		labelMessage.setForeground(goodNews ? Color.BLUE : Color.RED);
		labelMessage.setText(msg);
	}
	
	private boolean validateParams() {
		boolean retval = false;

		try {
			hostAddr = InetAddress.getByName(textHost.getText().trim());

			ports = UsefulFilter.parsePortSequenceString(textSequence.getText().trim());
			
			retval = true;
		} catch (IllegalArgumentException e) { 
			setMessage(false, "Broken Port sequence string!");
		} catch (java.net.UnknownHostException e) {
			setMessage(false, "Unknown Host: "+textHost.getText().trim());
		}

		return retval;
	}
}

