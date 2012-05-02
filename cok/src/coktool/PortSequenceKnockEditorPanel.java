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

import java.text.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import layout.TableLayout;

import cokshare.*;

class PortSequenceKnockEditorPanel extends JPanel {

	private JScrollPane paneTableHolder;
	private JTable tableSequenceEditor;
	private DefaultTableModel model;

	private JButton buttonAdd;
	private JButton buttonUp;
	private JButton buttonDown;
	private JButton buttonDelete;
	private JButton buttonClear;
	private JButton buttonEditSuccessRules;
	private JButton buttonEditBadSourceRules;
	private JButton buttonEditValidSourceList;
	private JButton buttonDone;	

	private JLabel labelPort;
	private JLabel labelTimeout;
	private JLabel labelMessage;

	private JTextField textPorts;
	private JTextField textTimeout;

	private PortSequenceKnockDescriptor knockDesc = null;

	public static final int NOCHANGE=-1;
	private int changeEntry = NOCHANGE;

	PortSequenceKnockEditorPanel(PortSequenceKnockDescriptor knockDesc) {
		this.knockDesc = knockDesc;

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
 			{bord,pref,hgap,pref,hgap,pref,hgap,pref,hgap,pref,bord},
 			{bord,pref,vgap,pref,vgap,pref,vgap,fill,vgap,pref,vgap,pref,vgap,pref,vgap,pref,vgap,pref,bord}
 		};
		setLayout(new TableLayout(tableParams));
		
		final String[] columnNames = {"Port"};
		model = new NoEditTableModel(columnNames, 0);
		tableSequenceEditor = new JTable(model);
		tableSequenceEditor.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		paneTableHolder = new JScrollPane(tableSequenceEditor);

		buttonAdd                 = new JButton("Add");
		buttonUp                  = new JButton("Up");
		buttonDown                = new JButton("Down");
		buttonDelete              = new JButton("Delete");
		buttonClear               = new JButton("Clear");
		buttonEditSuccessRules    = new JButton("Edit Success Rules");
		buttonEditBadSourceRules  = new JButton("Edit Bad Source Rules");
		buttonEditValidSourceList = new JButton("Edit Valid Source List");
		buttonDone                = new JButton("Done >>");

		labelPort    = new JLabel("Port(s):");
		labelTimeout = new JLabel("Timeout (ms):");
		labelMessage = new JLabel();

		setMessage(true, "Port-Sequence Editor");

		textPorts   = new JTextField(15);
		textTimeout = new JTextField();
		textPorts.setDocument(new UsefulFilter(UsefulFilter.SEQ));
		textTimeout.setDocument(new UsefulFilter(UsefulFilter.NUM));

		add(paneTableHolder, "1,1,5,7");

		add(labelPort,       "1,9");
		add(textPorts,       "3,9,5,9");

		add(buttonUp,        "1,11");
		add(buttonDown,      "1,13");

		add(buttonAdd,       "3,11");

		add(buttonDelete,    "5,11");
		add(buttonClear,     "5,13");

		add(labelTimeout,    "1,15");
		add(textTimeout,     "3,15");

		add(buttonEditSuccessRules,   "9,1");
		add(buttonEditBadSourceRules, "9,3");
		add(buttonEditValidSourceList,  "9,5");
		add(buttonDone,      "9,15");

		add(labelMessage,    "1,17,5,17");

		setPreferredSize(new Dimension(450,375));

		//Ask to be notified of selection changes.
		ListSelectionModel rowSM = tableSequenceEditor.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					//Ignore extra messages.
					if (e.getValueIsAdjusting()) return;
                                        
					ListSelectionModel lsm =
						(ListSelectionModel)e.getSource();
					if (lsm.isSelectionEmpty()) {
						changeEntry = NOCHANGE;
						//no rows are selected
					} else {
						changeEntry = lsm.getMinSelectionIndex();
						//selectedRow is selected
					}
				}
			});


		buttonAdd.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					addPorts();
				}
			});

		textPorts.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					addPorts();
				}
			});

		buttonUp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (changeEntry > 0) {
						model.moveRow(changeEntry, changeEntry, changeEntry-1);
						changeEntry--;
						tableSequenceEditor.setRowSelectionInterval(changeEntry,changeEntry);
					}
				}
			});

		buttonDown.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (changeEntry != NOCHANGE &&
						changeEntry < tableSequenceEditor.getRowCount()-1) {
						model.moveRow(changeEntry, changeEntry, changeEntry+1);
						changeEntry++;
						tableSequenceEditor.setRowSelectionInterval(changeEntry,changeEntry);
					}
				}
			});

		buttonClear.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (model.getRowCount() > 0) {
						if (JOptionPane.showConfirmDialog(null,"Are you sure...?", "Are you sure...?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
							int cnt = model.getRowCount();
							for (int x = 0; x < cnt; x++)
								model.removeRow(0);
						}
					} else
						setMessage(false, "No Ports to Clear!");
				}
			});

		buttonDelete.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent ae) {
					if (model.getRowCount() > 0) {
						if (changeEntry >= 0) {
							model.removeRow(changeEntry);
							if (tableSequenceEditor.getRowCount() > 0)
								tableSequenceEditor.removeRowSelectionInterval(0,tableSequenceEditor.getRowCount()-1);
							
							changeEntry = NOCHANGE;
						} else 
							setMessage(false, "No Port Selected to Delete!");
					} else
						setMessage(false, "No Ports to Delete");
				}
			});

		buttonEditSuccessRules.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					COKTool.buildFrame("Edit Success Rules", new RuleEditorPanel(knockDesc.getSuccessRules()));
				}
			});

		buttonEditBadSourceRules.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					COKTool.buildFrame("Edit Bad Source Address Rules", new RuleEditorPanel(knockDesc.getBadSourceRules()));
				}
			});

		buttonEditValidSourceList.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					COKTool.buildFrame("Edit Valid Source Address List", new CIDRListEditorPanel(knockDesc.getValidSourceAddrs()));
				}
			});

		buttonDone.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {

					if (validateDescriptorInfo()) {
						Vector portSeq = new Vector();
						for (int x = 0; x < tableSequenceEditor.getRowCount(); x++)
							portSeq.add((Integer)model.getValueAt(x,0));
						knockDesc.setPortSeq(portSeq);

						knockDesc.setTimeout(Integer.parseInt(textTimeout.getText()));

						JFrame parentFrame = (JFrame)getRootPane().getParent();
						parentFrame.setVisible(false);
						parentFrame.dispose();
					} else {
						setMessage(false, "Knock isn't ready to be sent!");
					}
				}
			});
	}

	private void addPorts() {
		if (textPorts.getText().length() > 0) {
			try {
				Integer[] ports = UsefulFilter.parsePortSequenceString(textPorts.getText().trim());
				for (int x = 0; x < ports.length; x++) {
					Object[] row = { ports[x] };
					model.addRow(row);
				}
				textPorts.setText("");
				setMessage(true, ports.length + " new ports added, " + model.getRowCount() + " total");
			} catch (IllegalArgumentException e) {
				// should never happen thanks to parsing document
				setMessage(false, "Invalid port-list!");
			}
		} else
			setMessage(false, "Empty port-list");
	}

	private void populateGUI() {
		Vector ports = knockDesc.getPortSeq();
		for (int x = 0; x < ports.size(); x++) {
			Object[] row = {new Integer(x+1), ports.get(x)};
			model.addRow(row);
		}
	}

	private boolean validateDescriptorInfo() {
		boolean retval = false;

		if (model.getRowCount() > 0 && Integer.parseInt(textTimeout.getText()) > 0) {
			retval = true;
		}

		return retval;
	}

	private void setMessage(boolean goodNews, String msg) {
		labelMessage.setForeground(goodNews ? Color.BLUE : Color.RED);
		labelMessage.setText(msg);
	}

	/** declare away */
	private PortSequenceKnockEditorPanel() {}
}
