package com.limegroup.gnutella.gui.options.panes;

import java.io.File;
import java.io.IOException;

import org.appwork.storage.config.JsonConfig;
import org.jdownloader.settings.GeneralSettings;

import com.frostwire.gui.bittorrent.TorrentSaveFolderComponent;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
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

		boolean restart = isDirty();
		
        SharingSettings.TORRENT_DATA_DIR_SETTING.setValue(new File(COMPONENT.getTorrentSaveFolderPath()));
        GeneralSettings jDownloaderSettings = JsonConfig.create(GeneralSettings.class);
        jDownloaderSettings.setDefaultDownloadFolder(COMPONENT.getTorrentSaveFolderPath());
		
		return restart;
	}

	@Override
	public boolean isDirty() {
		return !SharingSettings.TORRENT_DATA_DIR_SETTING.getValueAsString().equals(COMPONENT.getTorrentSaveFolderPath());
	}
}
