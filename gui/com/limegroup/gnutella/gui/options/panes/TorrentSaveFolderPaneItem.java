package com.limegroup.gnutella.gui.options.panes;

import java.io.File;
import java.io.IOException;

import org.appwork.storage.config.JsonConfig;
import org.jdownloader.settings.GeneralSettings;

import com.frostwire.gui.bittorrent.TorrentSaveFolderComponent;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.options.OptionsConstructor;
import com.limegroup.gnutella.gui.options.OptionsMediator;
import com.limegroup.gnutella.settings.LibrarySettings;
import com.limegroup.gnutella.settings.SharingSettings;

public class TorrentSaveFolderPaneItem extends AbstractPaneItem {

	public final static String TITLE = I18n.tr("Default Save Folder");
	
	private TorrentSaveFolderComponent COMPONENT;

	public TorrentSaveFolderPaneItem() {
        super(TITLE, I18n.tr("Choose the folder where downloads will be saved to"));
        
        COMPONENT = new TorrentSaveFolderComponent(false);
        add(COMPONENT);
	}

	@Override
	public void initOptions() {
		// nothing the component does it.
	}

	@Override
	public boolean applyOptions() throws IOException {
		if (!COMPONENT.isTorrentSaveFolderPathValid(true)) {
			GUIMediator.showError(TorrentSaveFolderComponent.getError());
			throw new IOException();
		}

        if (isDirty()) {
            final File newSaveFolder = new File(COMPONENT.getTorrentSaveFolderPath());
            updateLibraryFolders(newSaveFolder);
            updateDefaultSaveFolders(newSaveFolder);
        }
		
		return false;
	}

	/**
	 * Adds this save folder to the Library so the user can find the files he's going to save in the different sections of the Library.
	 * If the user wants the previous save folder out of the library she'll have to remove it by hand.
	 * @param newSaveFolder
	 */
    private void updateLibraryFolders(final File newSaveFolder) {
        LibrarySettings.DIRECTORIES_TO_INCLUDE.add(newSaveFolder);
        
        //if we don't re-init the Library Folders Pane, it will exclude this folder when options are applied.
        //so we reload it with our new folder from here.
        OptionsMediator.instance().reinitPane(OptionsConstructor.LIBRARY_KEY);
    }

    private void updateDefaultSaveFolders(File newSaveFolder) {
        //torrent save folder
        SharingSettings.TORRENT_DATA_DIR_SETTING.setValue(newSaveFolder);

        //jDownloader save folder
        GeneralSettings jDownloaderSettings = JsonConfig.create(GeneralSettings.class);
        jDownloaderSettings.setDefaultDownloadFolder(newSaveFolder.getAbsolutePath());
    }

    @Override
	public boolean isDirty() {
		return !SharingSettings.TORRENT_DATA_DIR_SETTING.getValueAsString().equals(COMPONENT.getTorrentSaveFolderPath());
	}
}