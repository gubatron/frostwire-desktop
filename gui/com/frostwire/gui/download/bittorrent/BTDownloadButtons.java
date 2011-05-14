package com.frostwire.gui.download.bittorrent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.limegroup.gnutella.gui.BoxPanel;
import com.limegroup.gnutella.gui.ButtonRow;
import com.limegroup.gnutella.gui.I18n;

/**
 * This class contains the buttons in the download window, allowing
 * classes in this package to enable or disable buttons at specific
 * indeces in the row.
 */
public final class BTDownloadButtons {
    
    /**
	 * The row of buttons for the donwload window.
	 */
	private ButtonRow BUTTONS;
	
	/**
	 * The index of the up button.
	 */
	static final int UP_BUTTON = 0;
	
	/**
	 * The index of the down button.
	 */
	static final int DOWN_BUTTON = 1;


  	BTDownloadButtons(final BTDownloadMediator dm) {
    
		BUTTONS = new ButtonRow(dm.getActions(),
		                        ButtonRow.X_AXIS, ButtonRow.NO_GLUE);
	}
	
	ButtonRow getComponent() { return BUTTONS; }
}
