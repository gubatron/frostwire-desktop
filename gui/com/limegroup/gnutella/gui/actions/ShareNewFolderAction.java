package com.limegroup.gnutella.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;

public class ShareNewFolderAction extends AbstractAction {

	/**
     * 
     */
    private static final long serialVersionUID = -4798827595247405133L;

    public ShareNewFolderAction() {
		putValue(Action.NAME, I18n.tr
				("Share New Folder..."));
		putValue(Action.SHORT_DESCRIPTION, I18n.tr("Opens a Window and Lets You Choose a New Folder to Share"));
	}

	/**
	 * Prompts for adding a shared library folder.
	 */
	public void actionPerformed(ActionEvent e) {
		GUIMediator.instance().addSharedLibraryFolder();
	}
}
