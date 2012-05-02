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

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;

import layout.TableLayout;

import cokshare.NoEditTableModel;

/**
 * Class to display a list of passphrases generated for a OTP system
 */
class OTPListPanel extends JPanel {
	
	private JScrollPane paneTableHolder;
	private JTable tableListPassphrases;
	private NoEditTableModel model;

	private JButton buttonSave;
	private JButton buttonDone;
	
	/**
	 * Constructor
	 *
	 * @param passphrases a String[] containing the passphrases to display
	 */
	OTPListPanel(String[] passphrases) {
		buildGUI(passphrases);
	}

	/**
	 * Build the GUI 
	 *
	 * @param passphrases as passed by the constructor!
	 */
	private void buildGUI(String[] passphrases) {
		double bord = 10;
		double hgap = 10;
		double vgap = 10;
		double fill = TableLayout.FILL;
		double pref = TableLayout.PREFERRED;

		double[][] tableParams = { 
			{bord,pref,hgap,fill,bord},
			{bord,fill,vgap,pref,bord}
		};
		setLayout(new TableLayout(tableParams));

		String[] columnNames = {"#", "Passphrase"};
		Object[][] data = new Object[passphrases.length][2];
		for (int x = 0; x < passphrases.length; x++) {
			data[x][0] = new Integer(passphrases.length-x);
			data[x][1] = passphrases[x];
		}
		model = new NoEditTableModel(data, columnNames);

		tableListPassphrases = new JTable(model);
		tableListPassphrases.getColumnModel().getColumn(0).setPreferredWidth(30);
		tableListPassphrases.getColumnModel().getColumn(1).setPreferredWidth(230);
		tableListPassphrases.doLayout();
		tableListPassphrases.setColumnSelectionAllowed(false);
		tableListPassphrases.setRowSelectionAllowed(false);
		tableListPassphrases.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		paneTableHolder = new JScrollPane(tableListPassphrases);

		buttonSave = new JButton("Save to File");

		add(paneTableHolder, "1,1,3,1");
		add(buttonSave,      "1,3");

		setPreferredSize(new Dimension(290,250));
		
		buttonSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					JFileChooser fc = new JFileChooser();
					if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();

						if (!file.exists()) {
							try {
								FileWriter out = new FileWriter(file);
								for (int x = 0; x < tableListPassphrases.getRowCount(); x++) {
									out.write(model.getValueAt(x,0)+" "+model.getValueAt(x,1)+"\n");
								}
								out.close();
							} catch(IOException e) {
								JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}			
			});
	}

	/** declare away */
	private OTPListPanel() {}
}
