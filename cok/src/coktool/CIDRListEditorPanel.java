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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import layout.TableLayout;

import cokshare.CIDRBlock;
import cokshare.NoEditTableModel;
import cokshare.UsefulFilter;

/**
 * A panel in which one may edit lists of CIDR Blocks (and IP
 * Addresses) for regulating access to remote resources.
 */
class CIDRListEditorPanel extends JPanel {

	private JScrollPane paneTableHolder;
	private JTable tableSequenceEditor;
	private DefaultTableModel model;

	private JButton buttonAdd;
	private JButton buttonDelete;
	private JButton buttonClear;
	private JButton buttonDone;

	private JLabel labelCIDRList;
	private JLabel labelMessage;

	private JTextField textCIDRList;

	private JSeparator sepBottom;
	
	private HashSet cidrList;

	public static final int NOCHANGE=-1;
	private int changeEntry = NOCHANGE;

	CIDRListEditorPanel(HashSet cidrList) {
		this.cidrList = cidrList;

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
 			{bord,pref,hgap,pref,hgap,pref,fill,bord},
 			{bord,fill,vgap,pref,vgap,pref,vgap,pref,vgap,pref,vgap,pref,bord}
 		};
		setLayout(new TableLayout(tableParams));
		
		final String[] columnNames = {"Valid Addresses"};
		model = new NoEditTableModel(columnNames, 0);
		tableSequenceEditor = new JTable(model);
		tableSequenceEditor.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		paneTableHolder = new JScrollPane(tableSequenceEditor);

		buttonAdd    = new JButton("Add");
		buttonDelete = new JButton("Delete");
		buttonClear  = new JButton("Clear");
		buttonDone   = new JButton("Done >>");

		labelCIDRList = new JLabel("Address(s):");
		labelMessage  = new JLabel();

		setMessage(true, "CIDR List Editor");		

		textCIDRList = new JTextField(30);
		textCIDRList.setDocument(new UsefulFilter(UsefulFilter.CIDRIPLIST));

		sepBottom = new JSeparator();
		
		add(paneTableHolder, "1,1,6,1");

		add(labelCIDRList,   "1,3");
		add(textCIDRList,    "3,3,6,3");

		add(buttonAdd,       "1,5");
		add(buttonDelete,    "3,5");
		add(buttonClear,     "5,5");

		add(buttonDone,      "5,7");

		add(sepBottom,       "1,9,6,9");

		add(labelMessage,    "1,11,6,11");

		setPreferredSize(new Dimension(300,300));

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
					addCIDRs();
				}
			});
		
		textCIDRList.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					addCIDRs();
				}
			});				

		buttonDelete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (model.getRowCount() > 0) {
						if (changeEntry >= 0) {

							CIDRBlock removeCIDR = (CIDRBlock)model.getValueAt(changeEntry,0);
							cidrList.remove(removeCIDR);

							model.removeRow(changeEntry);
							if (tableSequenceEditor.getRowCount() > 0)
								tableSequenceEditor.removeRowSelectionInterval(0,tableSequenceEditor.getRowCount()-1);
						
							changeEntry = NOCHANGE;
						} else
							setMessage(false, "No CIDR Blocks selected to Delete");
					} else
						setMessage(false, "No CIDR Blocks to Delete");
				}
			});
		
		buttonClear.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (model.getRowCount() > 0) {
						if (JOptionPane.showConfirmDialog(null,"Are you sure...?", "Are you sure...?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {

							cidrList.clear();

							int cnt = model.getRowCount();
							for (int x = 0; x < cnt; x++)
								model.removeRow(0);
						}
					} else
						setMessage(false, "No CIDR Blocks to Delete");
				}
			});
		
		buttonDone.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					JFrame parentFrame = (JFrame)getRootPane().getParent();
					parentFrame.setVisible(false);
					parentFrame.dispose();
				}
			});
				
	}

	private void populateGUI() {
		for (Iterator it = cidrList.iterator(); it.hasNext(); ) {
			Object[] row = {(CIDRBlock)it.next()};
			model.addRow(row);
		}			
	}

	private void addCIDRs() {
		if (textCIDRList.getText().length() > 0) {
			try {
				HashSet newCIDRs = UsefulFilter.parseCIDRIPSequenceString(textCIDRList.getText().trim());

				for (Iterator it = newCIDRs.iterator(); it.hasNext(); )
					cidrList.add((CIDRBlock)it.next());

				// clear the list
				int cnt = model.getRowCount();
				for (int x = 0; x < cnt; x++)
					model.removeRow(0);

				populateGUI();

				textCIDRList.setText("");
				setMessage(true, newCIDRs.size() + " addresses added");
			} catch (IllegalArgumentException e) {
				// should never happen thanks to parsing document
				setMessage(false, "Invalid CIDR-list!");
			}
		} else
			setMessage(false, "Empty CIDR-list");
	}

	private void setMessage(boolean goodNews, String msg) {
		labelMessage.setForeground(goodNews ? Color.BLUE : Color.RED);
		labelMessage.setText(msg);
	}

	/** Declare Away */
	private CIDRListEditorPanel() {}
}
