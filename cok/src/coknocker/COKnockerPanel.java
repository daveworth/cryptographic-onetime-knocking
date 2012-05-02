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

import layout.TableLayout;

/**
 * Simple panel to wrap up a JTabbedPane, to stick into any top level
 * container (a JFrame or JApplet)
 */
class COKnockerPanel extends JPanel {
	
	/** Pane containing COKnocker components*/
	private JTabbedPane knockerPane;

	/** The preferred size of the pane (which is a function of the
	 * preferred sizes of the contained object) */
	private Dimension prefSize;

	/** Constructor - Build a GUI */
	COKnockerPanel() {
		buildGUI();
	}

	/**
	 * Construct a GUI 
	 */
	private void buildGUI() {

		knockerPane = new JTabbedPane();
		prefSize = new Dimension();

		{
			UDPKnockerPanel udpPanel = new UDPKnockerPanel();
			knockerPane.addTab("UDP COKnocker", null, udpPanel, 
							   "UDP COKnocker");
			updatePrefSize(udpPanel.getPreferredSize());
		}

		{
			SeqKnockerPanel seqPanel = new SeqKnockerPanel();
			knockerPane.addTab("Sequence COKnocker", null, seqPanel, 
							   "Sequence COKnocker");
			updatePrefSize(seqPanel.getPreferredSize());
		}

		{
			OTPCalcPanel otpPanel = new OTPCalcPanel();
			knockerPane.addTab("One-Time-Password Calculator", null, otpPanel, 
							   "One-Time-Password Calculator"); 
			updatePrefSize(otpPanel.getPreferredSize());
		}

		double bord = 10;
		double hgap = 10;
		double vgap = 10;
		double fill = TableLayout.FILL;
		double pref = TableLayout.PREFERRED;
		
		double[][] tableParams = {
			{bord, fill, bord},
			{bord, fill, bord}
		};
		setLayout(new TableLayout(tableParams));
		
		add(knockerPane, "1,1");
		
		setPreferredSize(prefSize);
	}

	/** Simply update the current preferred size based on the maximum
	 * of x and y dimensions of contained objects */
	private void updatePrefSize(Dimension dim) {
		if (dim.getWidth() > prefSize.getWidth())
			prefSize.setSize(dim.getWidth(), prefSize.getHeight());

		if (dim.getHeight() > prefSize.getHeight())
			prefSize.setSize(prefSize.getWidth(), dim.getHeight());
	}
}
