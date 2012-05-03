package com.frostwire.gui.bittorrent;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.limewire.util.OSUtils;

import com.limegroup.gnutella.gui.ButtonRow;
import com.limegroup.gnutella.gui.FileChooserHandler;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.themes.SkinCustomUI;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.settings.SharingSettings;

public class TorrentSaveFolderComponent extends JPanel {

	private static final long serialVersionUID = -6564593945827058369L;
	private JTextField folderTextField;
    private static String errorMessage;
	
	public TorrentSaveFolderComponent(boolean border) {
		folderTextField = new JTextField(SharingSettings.TORRENT_DATA_DIR_SETTING.getValueAsString());
		
		setLayout(new GridBagLayout());
		if (border) {
		    this.putClientProperty(SkinCustomUI.CLIENT_PROPERTY_DARK_DARK_NOISE, true);
			setBorder(ThemeMediator.CURRENT_THEME.getCustomUI().createTitledBorder(I18n.tr("Torrent Data Save Folder")));
		}
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		// "Save Folder" text field
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(10,10,10,10);
		gbc.gridwidth = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.LINE_START;
		add(folderTextField, gbc);

		// "Save Folder" buttons "User Default", "Browse..."
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.gridwidth = GridBagConstraints.REMAINDER;

		add(new ButtonRow(new Action[] { new DefaultAction(), new BrowseAction() },
				ButtonRow.X_AXIS, ButtonRow.LEFT_GLUE), gbc);
	}
	
	public String getTorrentSaveFolderPath() {
		return folderTextField.getText();
	}
	
	public boolean isTorrentSaveFolderPathValid(boolean checkExist) {
		//has to be non empty, writeable, must be a folder, and must not be Saved, Shared, or inside any of them.
		if (folderTextField.getText().trim().length() == 0) {
			errorMessage = I18n.tr("You forgot to enter a path for the Torrent Data Folder.");
			return false;
		}
		
		String path = folderTextField.getText().trim();
		File folder = new File(path);

		return isTorrentSaveFolderPathValid(checkExist, folder);
	}
	
	/**
	 * The torrent save path is only valid as long as it's not inside (anywhere)
	 * the Gnutella Save Folder.
	 * 
	 * This folder cannot also be a parent of the Gnutella Save folder.
	 * 
	 * @param gnutellaSaveFolders
	 * @return
	 */
	public static boolean isTorrentSaveFolderPathValid(boolean checkExist, File folder) {

	    if (checkExist) {
    		//is folder useable
    		if (!(folder.exists() && folder.isDirectory() && folder.canWrite())) {
    			errorMessage = I18n.tr("Please enter a valid path for the Torrent Data Folder");
    			return false;
    		}
	    }
	    String lowerCaseFolderPath = folder.getAbsolutePath().toLowerCase();
	    
	    //avoid user stupidity, do not save files anywhere in program files.
	    if (OSUtils.isWindows() && lowerCaseFolderPath.contains(System.getenv("ProgramFiles").toLowerCase())) {
	        return false;
	    }
		
		return true;
	}
	
	public static boolean isParentOrChild(File torrentFolder, File otherFolder, String errorMessageSuffix) {
		//is folder inside gnutella save folder?
		try {
			if (torrentFolder.getCanonicalPath().startsWith(otherFolder.getCanonicalPath())) {
				errorMessage = I18n.tr("The Torrent Data Folder cannot be inside the " + errorMessageSuffix);
				return true;
			}
		} catch (IOException e) {
			errorMessage = I18n.tr("Could not resolve folder path.");
			return true;
		}

		//is folder a parent of the gnutella save folder?
		try {
			if (otherFolder.getCanonicalPath().startsWith(torrentFolder.getCanonicalPath())) {
				errorMessage = I18n.tr("The Torrent Data Folder cannot be a parent folder of the " + errorMessageSuffix);
				return true;
			}
		} catch (IOException e) {
			errorMessage = I18n.tr("Could not resolve folder path.");
			return true;
		}
		
		return false;

	}
	
	private class DefaultAction extends AbstractAction {
		
		private static final long serialVersionUID = 7266666461649699221L;

		public DefaultAction() {
			putValue(Action.NAME, I18n.tr("Use Default"));
			putValue(Action.SHORT_DESCRIPTION, I18n.tr("Use the Default Folder"));
		}
		
        public void actionPerformed(ActionEvent e) {
        	folderTextField.setText(SharingSettings.DEFAULT_TORRENT_DATA_DIR.getAbsolutePath());
        }
    }
	
	private class BrowseAction extends AbstractAction {
		
		private static final long serialVersionUID = 2976380710515726420L;

		public BrowseAction() {
			putValue(Action.NAME, I18n.tr("Browse..."));
			putValue(Action.SHORT_DESCRIPTION, I18n.tr("Choose Another Folder"));
		}
		
        public void actionPerformed(ActionEvent e) {
            File saveDir = 
                    FileChooserHandler.getInputDirectory(TorrentSaveFolderComponent.this);
			if(saveDir == null || !saveDir.isDirectory()) return;
			folderTextField.setText(saveDir.getAbsolutePath());
        }
    }

	public static String getError() {
		return errorMessage;
	}
}
