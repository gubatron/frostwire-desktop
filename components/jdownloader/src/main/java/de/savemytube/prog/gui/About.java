/**
 * JToothpaste - Copyright (C) 2007 Matthias
 * Schuhmann
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package de.savemytube.prog.gui;

import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Rectangle;

import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.awt.GridLayout;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class About extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JLabel lblMessage = null;
	private JPanel jPanelS = null;
	private JButton btnOk = null;
	private JPanel pnlCenter = null;
	private JPanel pnlImage = null;
	/**
	 * @param owner
	 */
	public About(Frame owner) {
		super(owner);
		initialize();
        
        
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(260, 200);
		this.setTitle("About");
		this.setResizable(false);
		this.setModal(true);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			lblMessage = new JLabel();
			lblMessage.setText("JToothpaste V1.1 by Matthias Schuhmann");
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJPanelS(), BorderLayout.SOUTH);
			jContentPane.add(getPnlCenter(), BorderLayout.NORTH);
			jContentPane.add(getPnlImage(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanelS	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelS() {
		if (jPanelS == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(5, 5, 5, 5);
			jPanelS = new JPanel();
			jPanelS.setLayout(new GridBagLayout());
			jPanelS.add(getBtnOk(), gridBagConstraints);
		}
		return jPanelS;
	}

	/**
	 * This method initializes btnOk	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtnOk() {
		if (btnOk == null) {
			btnOk = new JButton();
			btnOk.setText("Ok");
			btnOk.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
				}
			});
			btnOk.setText("Ok");
		}
		return btnOk;
	}

	/**
	 * This method initializes pnlCenter	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPnlCenter() {
		if (pnlCenter == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.anchor = GridBagConstraints.NORTH;
			gridBagConstraints1.insets = new Insets(5, 5, 5, 5);
			pnlCenter = new JPanel();
			pnlCenter.setLayout(new GridBagLayout());
			pnlCenter.add(lblMessage, gridBagConstraints1);
		}
		return pnlCenter;
	}

	/**
	 * This method initializes pnlImage	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPnlImage() {
		if (pnlImage == null) {
			pnlImage = new JPanel();
			pnlImage.setLayout(new GridBagLayout());
            IconLoader ic = new IconLoader();
            JLabel img = new JLabel(ic.createImageIcon("toothpaste.gif",""));
            pnlImage.add(img);
		}
		return pnlImage;
	}

	
	
	public void setVisible(boolean b){
		centerScreen();
		super.setVisible(b);
	}
	public void centerScreen() {
		  Dimension dim = getToolkit().getScreenSize();
		  Rectangle abounds = getBounds();
		  setLocation((dim.width - abounds.width) / 2,
		      (dim.height - abounds.height) / 2);
		  
		  requestFocus();
		}
}  //  @jve:decl-index=0:visual-constraint="44,5"
