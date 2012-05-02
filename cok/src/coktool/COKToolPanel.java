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
import java.net.*;
import java.rmi.*;
import java.io.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import cokshare.*;

import layout.TableLayout;

/**
 * Panel to hold COKtool panel!
 */
class COKToolPanel extends JPanel {

	private JLabel labelHost;
	private JLabel labelMessage;

	private JTextField textHost;

	private JButton buttonConnector;
	private JButton buttonNewUDP_OTP_Knock;
	private JButton buttonNewDNSKnock;
	private JButton buttonNewPortSequence;
	private JButton buttonDeleteKnock;
	private JButton buttonDeleteAllKnocks;
	private JButton buttonHaltCOKd;

	private NoEditTableModel model;
	private JTable tableBoundKnocks;
	private JScrollPane paneTableHolder;

	private JPanel panelLauncher;

	private JSeparator sepLauncher;
	private JSeparator sepActionType;

	private COKManager cokManager = null;

	private final int DISCONNECTED = 0;
	private final int CONNECTED    = 1;

	private int state = DISCONNECTED;
	private int selectedKnock = -1;

	private KnockDescriptor boundKnocks[];

	private PortSequenceKnockDescriptor newPortSequenceKnockDesc = null;
	private UDP_OTP_KnockDescriptor     newUDP_OTP_KnockDesc     = null;
	private DNSKnockDescriptor          newDNSKnockDesc          = null;

	COKToolPanel() {
		buildGUI();		
	}

	private void buildGUI() {
		double bord = 10;
		double hgap = 10;
		double vgap = 10;
		double fill = TableLayout.FILL;
		double pref = TableLayout.PREFERRED;

		double[][] tableParams = { 
			{bord,pref,hgap,pref,hgap,fill,bord},
			{bord,pref,vgap,pref,vgap,fill,vgap,pref,vgap,pref,bord}
		};
		setLayout(new TableLayout(tableParams));
		
		final String[] columnNames = { "Knock Type", "Identifier"};
		
		model = new NoEditTableModel(columnNames, 0);
		tableBoundKnocks = new JTable(model);
		tableBoundKnocks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		paneTableHolder = new JScrollPane(tableBoundKnocks);

		labelMessage = new JLabel();
		labelHost   = new JLabel("Host: ");
		setMessage(false, "Not Connected!");

		InetAddress localHost = null;
		try {
			localHost = InetAddress.getLocalHost();
		} catch (java.net.UnknownHostException e) {
			// XXX if localhost isn't found... this is BAD probably XXX
		}		

		textHost = new JTextField(20);
		textHost.setDocument(new UsefulFilter(UsefulFilter.HOST));
		textHost.setText(localHost.getHostAddress());

		buttonConnector = new JButton("Connect >>");

		sepLauncher = new JSeparator();
		
		{
			panelLauncher = new JPanel();
			double[][] tablePanelParams = {
				{bord,pref,hgap,pref,hgap,pref,bord},
				{bord,pref,vgap,pref,vgap,pref,vgap,pref,bord}
			};
			panelLauncher.setLayout(new TableLayout(tablePanelParams));
			
			buttonDeleteKnock      = new JButton("Delete Knock");
			buttonDeleteAllKnocks  = new JButton("Delete All Knocks");
			buttonNewUDP_OTP_Knock = new JButton("NEW UDP OTP Knock");
			buttonNewDNSKnock      = new JButton("New DNS Knock");
			buttonNewPortSequence  = new JButton("NEW Port Sequence Knock");
			buttonHaltCOKd         = new JButton("Halt COKd");

			sepActionType = new JSeparator();

			panelLauncher.add(buttonDeleteKnock,      "1,1");
			panelLauncher.add(buttonDeleteAllKnocks,  "3,1");

			panelLauncher.add(sepActionType,          "1,3,5,3");

			panelLauncher.add(buttonNewUDP_OTP_Knock, "1,5");
			panelLauncher.add(buttonNewDNSKnock,      "3,5");
			panelLauncher.add(buttonNewPortSequence,  "5,5");

			panelLauncher.add(buttonHaltCOKd,         "5,7");
			buttonDeleteKnock.setEnabled(false);
			buttonDeleteAllKnocks.setEnabled(false);
			buttonNewUDP_OTP_Knock.setEnabled(false);
			buttonNewDNSKnock.setEnabled(false);
			buttonNewPortSequence.setEnabled(false);
			buttonHaltCOKd.setEnabled(false);
		}

		add(labelHost,       "1,1");
		add(textHost,        "3,1");

		add(buttonConnector, "1,3");
		add(labelMessage,     "3,3");
		
		add(paneTableHolder, "1,5,5,5");

		add(sepLauncher,     "1,7,5,7");

		add(panelLauncher,   "1,9,5,9");

		setPreferredSize(new Dimension(575,325));		
		
		//Ask to be notified of selection changes.
		ListSelectionModel rowSM = tableBoundKnocks.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					//Ignore extra messages.
					if (e.getValueIsAdjusting()) return;
					
					ListSelectionModel lsm =
						(ListSelectionModel)e.getSource();
					if (lsm.isSelectionEmpty()) {
						//no rows are selected
					} else {
						//selectedKnock is selected
						selectedKnock = lsm.getMinSelectionIndex();
					}
				}
			});

		buttonConnector.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (state == DISCONNECTED) {
						try {
							InetAddress host = InetAddress.getByName(textHost.getText());
							cokManager = (COKManager)Naming.lookup("//"+host.getHostAddress()+"/COKManager");

							refresh(false);
							setMessage(true, "Connected to "+host.getHostName()+": " + boundKnocks.length + " knocks bound");
							buttonConnector.setText("Refresh");

							state = CONNECTED;

							buttonDeleteKnock.setEnabled(true);
							buttonDeleteAllKnocks.setEnabled(true);
							buttonNewUDP_OTP_Knock.setEnabled(true);
							buttonNewDNSKnock.setEnabled(true);
							buttonNewPortSequence.setEnabled(true);
							buttonHaltCOKd.setEnabled(true);

						} catch (RemoteException e) {
							if (e.getCause() instanceof java.net.ConnectException) {
								setMessage(false, "Connection to " + textHost.getText() + " was refused!");

							} else {
								setMessage(false, "Error - RemoteException: " + e.getMessage());
							}
						} catch (NotBoundException e) {
							setMessage(false, "RMI-Registry reports COKd not bound on" + textHost.getText());
						} catch (MalformedURLException e) {
							setMessage(false, "Error - MalformedURL: " + e.getMessage());
						} catch (java.net.UnknownHostException e) {
							setMessage(false, "Unknown Host: " + textHost.getText());
						}
					} else if (state == CONNECTED) {
						refresh();
					}
				}
			});

		buttonDeleteKnock.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (model.getRowCount() > 0) {
					if (selectedKnock >= 0) {
						try {
							int ret = cokManager.removeKnock(boundKnocks[selectedKnock]);
							selectedKnock = -1;

							if (ret == COKManager.RULE_REMOVED) { 
								setMessage(true, "Knock Deleted");
							} else if (ret == COKManager.RULE_ERROR) {
								setMessage(false, "Server reports and error trying to delete knock!");
							} else {
								// seriously
								setMessage(false, "WHOA... should never see this!");
							}

							refresh(false);
						} catch (RemoteException e) {
							// XXX do something cool here XXX
							setMessage(false, "Error - RemoteException: " + e.getMessage() + e.getCause().getMessage());
						}
					} else
						setMessage(false, "No Knocks Selected!");
					} else
						setMessage(false, "No Knocks to Delete, try Refreshing");
				}
			});

		buttonDeleteAllKnocks.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (model.getRowCount() > 0) {
						if (JOptionPane.showConfirmDialog(null,"Are you sure...?", "Are you sure...?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {

							int errors = 0;
							int cnt = model.getRowCount();
							for (int x = 0; x < cnt; x++) {
								model.removeRow(0);
								try {
									int ret = cokManager.removeKnock(boundKnocks[x]);

									if (ret == COKManager.RULE_ERROR) {
										errors++;
									} else if (ret > 3) {
										setMessage(false, "Unknown response from COKd when deleting knock: "+ret);
									}
								} catch (RemoteException e) {
									setMessage(false, "Error - RemoteException: " + e.getMessage() + e.getCause().getMessage());
								}
							}

							refresh(false);

							if (errors > 0)
								setMessage(false, errors+" Errors encountered when deleting all knocks from COKd!");
							else 
								setMessage(true, "All knocks successfully deleted from COKd");

						}

					} else
						setMessage(false, "No Knocks to Delete, try Refreshing");
				}
			});

		buttonNewUDP_OTP_Knock.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					newUDP_OTP_KnockDesc = new UDP_OTP_KnockDescriptor();
					final JFrame knockPanel = COKTool.buildFrame("UDP OTP Knock Editor", new UDP_OTP_KnockEditorPanel(newUDP_OTP_KnockDesc));

					knockPanel.addWindowListener(new WindowListener() {
							public void windowClosed(WindowEvent we){
								if (knockPanel.getName().equals(we.getWindow().getName()))
									setKnock(newUDP_OTP_KnockDesc);
							}

							public void windowClosing(WindowEvent e) {}
							public void windowOpened(WindowEvent e) {}
							public void windowIconified(WindowEvent e) {}
							public void windowDeiconified(WindowEvent e) {}
							public void windowActivated(WindowEvent e) {}
							public void windowDeactivated(WindowEvent e) {}
						});

				}
			});

		buttonNewDNSKnock.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					newDNSKnockDesc = new DNSKnockDescriptor();
					final JFrame knockPanel = COKTool.buildFrame("DNS Knock Editor", new UDP_OTP_KnockEditorPanel(newDNSKnockDesc));

					knockPanel.addWindowListener(new WindowListener() {
							public void windowClosed(WindowEvent we){
								if (knockPanel.getName().equals(we.getWindow().getName()))
									setKnock(newDNSKnockDesc);
							}

							public void windowClosing(WindowEvent e) {}
							public void windowOpened(WindowEvent e) {}
							public void windowIconified(WindowEvent e) {}
							public void windowDeiconified(WindowEvent e) {}
							public void windowActivated(WindowEvent e) {}
							public void windowDeactivated(WindowEvent e) {}
						});

				}
			});

		buttonNewPortSequence.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					newPortSequenceKnockDesc = new PortSequenceKnockDescriptor();
					final JFrame knockPanel = COKTool.buildFrame("Port Sequence Knock Editor", new PortSequenceKnockEditorPanel(newPortSequenceKnockDesc));

					knockPanel.addWindowListener(new WindowListener() {
							public void windowClosed(WindowEvent we) {
								if (knockPanel.getName().equals(we.getWindow().getName()))
									setKnock(newPortSequenceKnockDesc);
							}

							public void windowClosing(WindowEvent e) {}
							public void windowOpened(WindowEvent e) {}
							public void windowIconified(WindowEvent e) {}
							public void windowDeiconified(WindowEvent e) {}
							public void windowActivated(WindowEvent e) {}
							public void windowDeactivated(WindowEvent e) {}
						});

				}
			});
		
		buttonHaltCOKd.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					try {
						cokManager.HaltCOKd();
					} catch (RemoteException re) {
						try {
							throw (Exception)re.getCause();
						} catch (EOFException eofe) {
							// Ignored... side effect of exiting COKd
						} catch (Exception e) {
							setMessage(false, "Error - Exception: " + e.getMessage());
						}
					}

					cokManager=null;
					state = DISCONNECTED;
					buttonConnector.setText("Connect >>");
					setMessage(false, "COKd Halted!");
					buttonDeleteKnock.setEnabled(false);
					buttonDeleteAllKnocks.setEnabled(false);
					buttonNewUDP_OTP_Knock.setEnabled(false);
					buttonNewDNSKnock.setEnabled(false);
					buttonNewPortSequence.setEnabled(false);
					buttonHaltCOKd.setEnabled(false);
				}				
			});

	}

	private void setKnock(KnockDescriptor desc) {
		try {
			int ret = cokManager.setKnock(desc);

			String msg = "FELL THROUGH";
			boolean goodnews = true;

			switch(ret) {
			case COKManager.RULE_NEW:
				goodnews = true;
				msg = "Knock Added Successfully!";
				break;
			case COKManager.RULE_OVERRIDE:
				goodnews = true;
				msg = "Knock Behaviour Overridden Successfully!";
				break;
			case COKManager.RULE_ERROR:
				goodnews = false;
				msg = "Server reports error adding knock!";
				break;
			default:
				// XXX holy crap! XXX
				break;
			}

			setMessage(goodnews, msg);

		} catch (RemoteException e) {
			setMessage(false, "Error - RemoteException: " + e.getMessage());
		}
									
		updateKnockTable();
	}

	private void updateKnockTable() {
		try { 
			int cnt = model.getRowCount();
			for (int x = 0; x < cnt; x++)
				model.removeRow(0);
			boundKnocks = cokManager.getBoundKnocks();

			for (int x = 0; x < boundKnocks.length; x++) {
				String knockType = boundKnocks[x].getKnockType();
				String knockDesc = null;
				
				Object[] row = {boundKnocks[x].getKnockType(), 
								boundKnocks[x].getKnockDesc()};
				model.addRow(row);
			}

		} catch (RemoteException e) {
			setMessage(false, "Error - RemoteException: " + e.getMessage());
		}
	}

	private void setMessage(boolean goodNews, String msg) {
		labelMessage.setForeground(goodNews ? Color.BLUE : Color.RED);
		labelMessage.setText(msg);
	}	

	private void refresh() { refresh(true); }
	
	private void refresh(boolean updateMessage) {
		updateKnockTable();
		if (updateMessage)
			setMessage(true,"Refreshed: " + boundKnocks.length + " knocks bound");		
	}

}
