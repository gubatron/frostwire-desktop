package com.limegroup.gnutella.gui.init;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.limewire.i18n.I18nMarker;
import org.limewire.util.CommonUtils;
import org.limewire.util.StringUtils;

import com.frostwire.components.TorrentSaveFolderComponent;
import com.limegroup.gnutella.gui.ButtonRow;
import com.limegroup.gnutella.gui.FileChooserHandler;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledComponent;
import com.limegroup.gnutella.gui.SaveDirectoryHandler;
import com.limegroup.gnutella.gui.actions.RemoveSharedDirectoryAction;
import com.limegroup.gnutella.gui.actions.SelectSharedDirectoryAction;
import com.limegroup.gnutella.gui.library.RecursiveSharingPanel;
import com.limegroup.gnutella.settings.SharingSettings;
/**
 * This class displays a setup window for allowing the user to choose
 * the directory for saving their files.
 */
//2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|

class SaveWindow extends SetupWindow {

	private static final long serialVersionUID = 4918724013794478084L;

	private static final String LEARN_MORE_URL = "http://www.frostwire.com/faq#fil1";
    
	/**
	 * Constant handle to the <tt>LabeledTextField</tt> instance for the 
	 * save directory.
	 */
	private final JTextField SAVE_FIELD = new JTextField(20); 
		
	/**
	 * Variable for the default save directory to use.
	 */    
	private String _defaultSaveDir;

    private final RecursiveSharingPanel recursiveSharingPanel;
    
    // change for sharing files in saved folder
    private final JCheckBox CHECK_BOX = new JCheckBox();
    private final String CHECK_BOX_LABEL = I18nMarker.marktr("Share Finished Downloads");
    private final JLabel explanationLabel = new JLabel();

	private TorrentSaveFolderComponent torrentSaveFolderComponent;

	/**
	 * Creates the window and its components
	 */
	SaveWindow(SetupManager manager, boolean migrate) {
		super(manager, I18nMarker.marktr("Save Folders and Shared Folders"), describeText(migrate), LEARN_MORE_URL);
		File oldSaveDir = new File(CommonUtils.getUserHomeDir(), "Shared");
		if (oldSaveDir.exists()) {
			SharingSettings.DIRECTORIES_TO_SHARE.add(oldSaveDir);
		}

		recursiveSharingPanel = new RecursiveSharingPanel();
		recursiveSharingPanel.getTree().setRootVisible(false);
        recursiveSharingPanel.getTree().setShowsRootHandles(true);
        recursiveSharingPanel.setRoots(SharingSettings.DIRECTORIES_TO_SHARE.getValueAsArray());
        recursiveSharingPanel.addRoot(SharingSettings.DEFAULT_SHARE_DIR);
        recursiveSharingPanel.addRoot(SharingSettings.DEFAULT_SHARED_TORRENTS_DIR);
        recursiveSharingPanel.setFoldersToExclude(GuiCoreMediator.getFileManager().getFolderNotToShare());
        recursiveSharingPanel.setRootsExpanded();
    }
	
	private static String describeText(boolean migrate) {
	    if(!migrate)
	        return I18nMarker.marktr("Please choose a folder where you would like your files to be downloaded.\nYou can also choose folders you would like to share with other users on the Gnutella and BitTorrent Networks.");
	    else
	        return I18nMarker.marktr("FrostWire now downloads files to a new, different folder.\nPlease confirm the folder where you would like your files to be downloaded. You can also choose folders you would like to share with other users on the Gnutella and BitTorrent Networks.");
	}
    
    protected void createWindow() {
        super.createWindow();
        
        recursiveSharingPanel.updateLanguage();

		File saveDir = SharingSettings.getSaveDirectory();
		try {
		    _defaultSaveDir = saveDir.getCanonicalPath();
		} catch(IOException e) {
		    _defaultSaveDir = saveDir.getAbsolutePath();
		}

		JPanel mainPanel = new JPanel(new GridBagLayout());
		
		JPanel saveFolderPanel = new JPanel(new GridBagLayout());
		saveFolderPanel.setBorder(BorderFactory.createTitledBorder(I18n.tr("Gnutella Save Folder")));
		
		
		// "Save Folder" text field
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.insets = new Insets(0, 0, ButtonRow.BUTTON_SEP, 0);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		saveFolderPanel.add(SAVE_FIELD, gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        saveFolderPanel.add(createOptionForShareInSavedFolderComponent(), gbc);
		
		// "Save Folder" buttons "User Default", "Browse..."
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		saveFolderPanel.add(new ButtonRow(new Action[] { new DefaultAction(), new BrowseAction() },
				ButtonRow.X_AXIS, ButtonRow.LEFT_GLUE), gbc);
		
		gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.weighty = 1;
		mainPanel.add(saveFolderPanel, gbc);
		
        // "Saved Torrent Data" container
		torrentSaveFolderComponent = new TorrentSaveFolderComponent(true);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.weighty = 1;
        mainPanel.add(torrentSaveFolderComponent,gbc);        

        
        // "Shared Folders" panel
        JPanel sharingPanelContainer = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        recursiveSharingPanel.setBorder(BorderFactory.createTitledBorder(I18n.tr("Shared Folders")));
        sharingPanelContainer.add(recursiveSharingPanel, gbc);
        
        // "Shared Folders" actions
        gbc = new GridBagConstraints();
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(0, ButtonRow.BUTTON_SEP, 0, 0);
        sharingPanelContainer.add(new JButton(new SelectSharedDirectoryAction(recursiveSharingPanel, _manager.getOwnerComponent())), gbc);
        gbc.insets = new Insets(ButtonRow.BUTTON_SEP, ButtonRow.BUTTON_SEP, 0, 0);
        sharingPanelContainer.add(new JButton(new RemoveSharedDirectoryAction(recursiveSharingPanel)), gbc); 
        
        // "Shared Folders" container
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.weighty = 1;
        mainPanel.add(sharingPanelContainer, gbc);
        
        try {
		    SAVE_FIELD.setText(_defaultSaveDir);
        } catch(NullPointerException npe) {
            // internal swing error -- no biggie if it happens,
            // just means the user has to manually click 'Use Default'.
        }
		
		setSetupComponent(mainPanel);
	}
    
    
	/**
	 * Overrides applySettings method in SetupWindow.
	 *
	 * This method applies any settings associated with this setup window.
	 */
	public void applySettings(boolean loadCoreComponents) throws ApplySettingsException {
	    List<String> errors = new ArrayList<String>(2);
		try {
			String saveDirString = SAVE_FIELD.getText();
			File saveDir = new File(saveDirString);
            
			// Only do strict checks if we're loading...
			if(loadCoreComponents) {
    			if (!SaveDirectoryHandler.showVistaWarningIfNeeded(saveDir))
    			    throw new ApplySettingsException();
                
                if (!saveDir.isDirectory() && !saveDir.mkdirs())
                    throw new IOException();
			}
            
            // updates Incomplete directory etc...
            SharingSettings.setSaveDirectory(saveDir); 
		} catch(IOException ioe) {
		    errors.add(I18n.tr("FrostWire was unable to use the specified folder for saving files. Please try a different folder."));
		}
		File defaultShareDir = SharingSettings.DEFAULT_SHARE_DIR; 
        Set<File> roots = recursiveSharingPanel.getRootsToShare();
        if (roots.contains(defaultShareDir)) {
            // try to create it
            if (defaultShareDir.isFile()) {
                errors.add(I18n.tr("FrostWire could not create default share folder {0}, a file with that name already exists.", defaultShareDir));
            }
            else if (!defaultShareDir.isDirectory()) {
                if (!defaultShareDir.mkdirs()) {
                    errors.add(I18n.tr("FrostWire could not create default share folder {0}, it will not be shared.", defaultShareDir));
                }
            }
        }
        
        if(loadCoreComponents)
            GuiCoreMediator.getFileManager().loadWithNewDirectories(roots, recursiveSharingPanel.getFoldersToExclude());
        
        String saveDirString = SAVE_FIELD.getText();
		File saveDir = new File(saveDirString);
		
		Set<File> saveDirs = new HashSet<File>();
		saveDirs.add(saveDir);
		
        if (!torrentSaveFolderComponent.isTorrentSaveFolderPathValid(saveDirs, roots)) {
        	errors.add(torrentSaveFolderComponent.getError());
        }
        
        if (!errors.isEmpty()) {
            throw new ApplySettingsException(StringUtils.explode(errors, "\n\n"));
        }
        
        SharingSettings.SHARE_DOWNLOADED_FILES_IN_NON_SHARED_DIRECTORIES.
            setValue(CHECK_BOX.isSelected());
        
        SharingSettings.TORRENT_DATA_DIR_SETTING.setValue(new File(torrentSaveFolderComponent.getTorrentSaveFolderPath()));
        SharingSettings.SEED_FINISHED_TORRENTS.setValue(torrentSaveFolderComponent.isSeedingSelected());
        
	}
	
    private Component createOptionForShareInSavedFolderComponent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        LabeledComponent comp = new LabeledComponent(CHECK_BOX_LABEL, CHECK_BOX, LabeledComponent.NO_GLUE, LabeledComponent.RIGHT);

        explanationLabel.setFont(explanationLabel.getFont().deriveFont(Math.max(explanationLabel.getFont().getSize() - 2.0f, 9.0f)).deriveFont(Font.PLAIN));
        CHECK_BOX.addItemListener(new ItemListener() {
           public void itemStateChanged(ItemEvent e) {
                setExplanationText(true);
            }
        });
        setExplanationText(false);

        CHECK_BOX.setSelected(SharingSettings.SHARE_DOWNLOADED_FILES_IN_NON_SHARED_DIRECTORIES.getValue());

        comp.getComponent().setAlignmentX(Component.LEFT_ALIGNMENT);
        explanationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(comp.getComponent());
        panel.add(explanationLabel);

        return panel;
    }
    
    private void setExplanationText(boolean showMessage) {
        if (CHECK_BOX.isSelected()) {
            //explanationLabel.setText(I18n.tr("All downloads will be shared. INDIVIDUAL FILE NOTICE (FORGOT PREVIOUS FILES)"));
            explanationLabel.setText(I18n.tr("Currently sharing downloaded files to the 'Save Folder' with everybody"));
            if (showMessage) {
                JOptionPane.showMessageDialog(this,
                        I18n.tr("All files downloaded to the 'Save Folder' will be shared as 'individually shared files' with everybody on the network.\nYour 'Save Folder' won't be shared as a whole unless you decide to make it a shared folder.\n\nYou can check the files you are sharing individually in the Library Tab."),
                        I18n.tr("How finished downloads are being shared"),
                        JOptionPane.WARNING_MESSAGE); 
            }
        } else {
            explanationLabel.setText(I18n.tr("Currently not sharing downloaded files to the 'Save Folder' with anybody"));
        }
    }

	private class DefaultAction extends AbstractAction {
		
		public DefaultAction() {
			putValue(Action.NAME, I18n.tr("Use Default"));
			putValue(Action.SHORT_DESCRIPTION, I18n.tr("Use the Default Folder"));
		}
		
        public void actionPerformed(ActionEvent e) {
            SAVE_FIELD.setText(_defaultSaveDir);
        }
    }
	
	private class BrowseAction extends AbstractAction {
		
		public BrowseAction() {
			putValue(Action.NAME, I18n.tr("Browse..."));
			putValue(Action.SHORT_DESCRIPTION, I18n.tr("Choose Another Folder"));
		}
		
        public void actionPerformed(ActionEvent e) {
            File saveDir = 
                    FileChooserHandler.getInputDirectory(SaveWindow.this);
			if(saveDir == null || !saveDir.isDirectory()) return;
			SAVE_FIELD.setText(saveDir.getAbsolutePath());
        }
    }
}




