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
import java.io.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import layout.TableLayout;

import cokshare.*;

/**
 * A Handy GUI for editting COKRulesets
 *
 * @see cokshare.COKRuleset
 */
class RuleEditorPanel extends JPanel {

	private NoEditTableModel model;
	private JTable tableRuleEditor;
	private JScrollPane paneTableHolder;

	private JLabel labelRule;
	private JLabel labelMessage;

	private JButton buttonAddRule;
	private JButton buttonUpdateRule;
	private JButton buttonDeleteRule;
	private JButton buttonUp;
	private JButton buttonDown;
	private JButton buttonClear;
	private JButton buttonDone;
	private JButton buttonImportRules;

	private JTextField textRule;

	private JSeparator sepBottom;

	public static final int NOCHANGE=-1;
	private int changeEntry = NOCHANGE;

	private COKRuleset ruleset;

	/**
	 * Standard constructor which allows for pass-by-reference only!
	 */
	RuleEditorPanel(COKRuleset ruleset) {
		this.ruleset = ruleset;
		buildGUI();
		populateGUI();
	}

	/**
	 * Build the GUI and make it nice!
	 */
	private void buildGUI() {
		double bord = 10;
		double hgap = 10;
		double vgap = 10;
		double fill = TableLayout.FILL;
		double pref = TableLayout.PREFERRED;

		double[][] tableParams = { 
			{bord,pref,hgap,pref,hgap,pref,hgap,pref,hgap,pref,hgap,pref,hgap,pref,hgap,fill,hgap,pref,bord},
			{bord,fill,vgap,pref,vgap,pref,vgap,pref,vgap,pref,vgap,pref,bord}
		};
		setLayout(new TableLayout(tableParams));

		final String[] columnNames = { "Act. #", "Action"};

		model = new NoEditTableModel(columnNames, 0);

		tableRuleEditor = new JTable(model);
		tableRuleEditor.getColumnModel().getColumn(0).setPreferredWidth(60);
		tableRuleEditor.getColumnModel().getColumn(1).setPreferredWidth(690);
		tableRuleEditor.doLayout();
		tableRuleEditor.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableRuleEditor.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		changeEntry = NOCHANGE;

		paneTableHolder = new JScrollPane(tableRuleEditor);

		labelRule    = new JLabel("Rule:");
		labelMessage = new JLabel();

		buttonAddRule     = new JButton("Add");
		buttonUpdateRule  = new JButton("Update Entry");
		buttonDeleteRule  = new JButton("Delete");
		buttonUp          = new JButton("Up");
		buttonDown        = new JButton("Down");
		buttonClear       = new JButton("Clear All");
		buttonImportRules = new JButton("Import Rules");
		buttonDone        = new JButton("Done >>");

		textRule = new JTextField();

		sepBottom = new JSeparator();

		add(paneTableHolder,   "1,1,17,1");

		add(labelRule,         "1,3");
		add(textRule,          "3,3,17,3");

		add(buttonAddRule,     "1,5");
		add(buttonUpdateRule,  "3,5");
		add(buttonDeleteRule,  "5,5");
		add(buttonUp,          "7,5");
		add(buttonDown,        "9,5");
		add(buttonClear,       "11,5");
		add(buttonImportRules, "13,5");
		add(buttonDone,        "17,5");

		add(sepBottom,         "1,7,17,7");

		add(labelMessage,      "1,9,17,9");

		setPreferredSize(new Dimension(780,250));

		//Ask to be notified of selection changes.
		ListSelectionModel rowSM = tableRuleEditor.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					//Ignore extra messages.
					if (e.getValueIsAdjusting()) return;
					
					ListSelectionModel lsm =
						(ListSelectionModel)e.getSource();
					if (lsm.isSelectionEmpty()) {
						changeEntry = NOCHANGE;	
					} else {
						changeEntry = lsm.getMinSelectionIndex();
						textRule.setText((String)model.getValueAt(changeEntry, 1));
						//selectedRow is selected
					}
				}
			});

		buttonAddRule.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					addRule();
				}
			});

		buttonUpdateRule.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					updateRule();
				}
			});

		textRule.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (changeEntry == NOCHANGE)
						addRule();
					else
						updateRule();
				}
			});

		buttonUp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (changeEntry > 0) {
						tableRuleEditor.setValueAt(new Integer(changeEntry+1), changeEntry-1, 0);
						tableRuleEditor.setValueAt(new Integer(changeEntry),   changeEntry,   0);
						model.moveRow(changeEntry, changeEntry, changeEntry-1);
						changeEntry--;
						tableRuleEditor.setRowSelectionInterval(changeEntry,changeEntry);
					}
				}
			});

		buttonDown.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (changeEntry != NOCHANGE &&
						changeEntry < tableRuleEditor.getRowCount()-1) {
						tableRuleEditor.setValueAt(new Integer(changeEntry+2), changeEntry,   0);
						tableRuleEditor.setValueAt(new Integer(changeEntry+1), changeEntry+1, 0);
						model.moveRow(changeEntry, changeEntry, changeEntry+1);
						changeEntry++;
						tableRuleEditor.setRowSelectionInterval(changeEntry,changeEntry);
					}
				}
			});

		buttonClear.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (JOptionPane.showConfirmDialog(null,"Are you sure...?", "Are you sure...?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
						int cnt = model.getRowCount();
						for (int x = 0; x < cnt; x++)
							model.removeRow(0);
					}
				}
			});

		buttonDeleteRule.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent ae) {
					if (changeEntry >= 0) {
						int myChangeEntry = changeEntry;
						model.removeRow(changeEntry);
						if (tableRuleEditor.getRowCount() > 0) {
							tableRuleEditor.removeRowSelectionInterval(0,tableRuleEditor.getRowCount()-1);

							for (int x = myChangeEntry; x < tableRuleEditor.getRowCount(); x++) {
								model.setValueAt(new Integer(x+1), x, 0);
							}
						}
						changeEntry = NOCHANGE;
					}
				}
			});

		buttonImportRules.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					JFileChooser fc = new JFileChooser();
					if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();

						if (file.isFile() && file.exists() && file.canRead() ) {
							try {
								FileReader filein = new FileReader(file);
								BufferedReader reader = new BufferedReader(filein);

								String line;
								while( (line = reader.readLine()) != null ) {
									if (line.length() > 0) {
										Object[] row = {new Integer(tableRuleEditor.getRowCount()+1),
														line};
										model.addRow(row);
									}
								}

								filein.close();
							} catch(FileNotFoundException e) {
								setMessage(false, "Error - File Not Found: " + e.getMessage());
							} catch (IOException e) {
								setMessage(false, "Error - IOException: " + e.getMessage());
							}
						}
					}

				}
			});

		buttonDone.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					ruleset.clearRules();
					for (int x = 0; x < model.getRowCount(); x++)
						ruleset.addRule((String)model.getValueAt(x, 1));

					JFrame parentFrame = (JFrame)getRootPane().getParent();
					parentFrame.setVisible(false);
					parentFrame.dispose();
				}
			});

	}

	private void populateGUI() {
		Vector rules = ruleset.getRules();
		for (int x = 0; x < rules.size(); x++) {
			Object[] row = {new Integer(x+1), rules.get(x)};
			model.addRow(row);
		}		
	}

	private void addRule() {
		if (textRule.getText().length() > 0) {
			Object[] row = {
				new Integer(tableRuleEditor.getRowCount()+1), textRule.getText()
			};
			model.addRow(row);
			textRule.setText("");
		}
	}

	private void updateRule() {
		if (textRule.getText().length() > 0 && changeEntry >= 0) {
			tableRuleEditor.setValueAt(textRule.getText(),changeEntry, 1);
			textRule.setText("");
		} 
	}

	private void setMessage(boolean goodNews, String msg) {
		labelMessage.setForeground(goodNews ? Color.BLUE : Color.RED);
		labelMessage.setText(msg);
	} 

	/** Declare Away */
	private RuleEditorPanel() {}
}
