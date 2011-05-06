package com.frostwire.components;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.limewire.i18n.I18nMarker;

import com.limegroup.gnutella.gui.ButtonRow;
import com.limegroup.gnutella.gui.FileChooserHandler;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledComponent;
import com.limegroup.gnutella.settings.SharingSettings;

public class TorrentSaveFolderComponent extends JPanel {

	private static final long serialVersionUID = -6564593945827058369L;
	private JTextField folderTextField;
    private final JCheckBox CHECK_BOX = new JCheckBox();
    private final String CHECK_BOX_LABEL = I18nMarker.marktr("Seed Finished Torrent Downloads");
    private final JLabel explanationLabel = new JLabel();
    private static String errorMessage;
	
	public TorrentSaveFolderComponent(boolean border) {
		folderTextField = new JTextField(SharingSettings.TORRENT_DATA_DIR_SETTING.getValueAsString());
		
		setLayout(new GridBagLayout());
		if (border) {
			setBorder(BorderFactory.createTitledBorder(I18n.tr("Torrent Data Save Folder")));
		}
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		// "Save Folder" text field
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.insets = new Insets(0, 0, ButtonRow.BUTTON_SEP, 0);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		add(folderTextField, gbc);

		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(createSeedingOptionsComponents(), gbc);
		
		// "Save Folder" buttons "User Default", "Browse..."
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(new ButtonRow(new Action[] { new DefaultAction(), new BrowseAction() },
				ButtonRow.X_AXIS, ButtonRow.LEFT_GLUE), gbc);

	}
	
	public String getTorrentSaveFolderPath() {
		return folderTextField.getText();
	}
	
	public boolean isSeedingSelected() {
		return CHECK_BOX.isSelected();
	}
	
	
	public boolean isTorrentSaveFolderPathValid(Set<File> gnutellaSaveFolders, Set<File> sharedFolders) {
		//has to be non empty, writeable, must be a folder, and must not be Saved, Shared, or inside any of them.
		if (folderTextField.getText().trim().length() == 0) {
			errorMessage = I18n.tr("You forgot to enter a path for the Torrent Data Folder.");
			return false;
		}
		
		String path = folderTextField.getText().trim();
		File folder = new File(path);

		return isTorrentSaveFolderPathValid(folder, gnutellaSaveFolders, sharedFolders);
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
	public static boolean isTorrentSaveFolderPathValid(File folder, Set<File> gnutellaSaveFolders, Set<File> sharedFolders) {

		//is folder useable
		if (!(folder.exists() && folder.isDirectory() && folder.canWrite())) {
			errorMessage = I18n.tr("Please enter a valid path for the Torrent Data Folder");
			return false;
		}
		
		//is parent or child of a default save folder
		for (File saveFolder : gnutellaSaveFolders) {
			if (isParentOrChild(folder, saveFolder, "Gnutella Save Folder " + saveFolder.getName())) {
				return false;
			}
		}

		//is parent or child of a default share folder
		for (File sharedFolder : sharedFolders) {
			if (isParentOrChild(folder, sharedFolder, "Shared Folder " + sharedFolder.getName())) {
				return false;
			}
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
	
	private Component createSeedingOptionsComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        LabeledComponent comp = new LabeledComponent(CHECK_BOX_LABEL, CHECK_BOX, LabeledComponent.NO_GLUE, LabeledComponent.RIGHT);

        explanationLabel.setFont(explanationLabel.getFont().deriveFont(Math.max(explanationLabel.getFont().getSize() - 2.0f, 9.0f)).deriveFont(Font.PLAIN));
        CHECK_BOX.addItemListener(new ItemListener() {
           public void itemStateChanged(ItemEvent e) {
                setExplanationText();
            }
        });
        setExplanationText();

        CHECK_BOX.setSelected(SharingSettings.SEED_FINISHED_TORRENTS.getValue());

        comp.getComponent().setAlignmentX(Component.LEFT_ALIGNMENT);
        explanationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(comp.getComponent());
        panel.add(explanationLabel);

        return panel;
    }
	
	private void setExplanationText() {
        if (CHECK_BOX.isSelected()) {
            explanationLabel.setText(I18n.tr("Finished torrents will be seeded with other peers on the BitTorrent Network"));
        } else {
            explanationLabel.setText(I18n.tr("<html>Finished torrents will not be seeded. <b>You will become a Leecher</b>.<p>You still have to seed pieces of the torrent data files during their download."));
        }
    }
	
	private class DefaultAction extends AbstractAction {
		
		public DefaultAction() {
			putValue(Action.NAME, I18n.tr("Use Default"));
			putValue(Action.SHORT_DESCRIPTION, I18n.tr("Use the Default Folder"));
		}
		
        public void actionPerformed(ActionEvent e) {
        	folderTextField.setText(SharingSettings.DEFAULT_TORRENT_DATA_DIR.getAbsolutePath());
        }
    }
	
	private class BrowseAction extends AbstractAction {
		
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
