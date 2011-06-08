package com.limegroup.gnutella.gui.options.panes;

import java.io.File;
import java.io.IOException;

import com.frostwire.components.TorrentSaveFolderComponent;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.download.DownloadMediator;
import com.limegroup.gnutella.gui.upload.UploadMediator;
import com.limegroup.gnutella.settings.SharingSettings;

public class TorrentSaveFolderPaneItem extends AbstractPaneItem {

	public final static String TITLE = I18n.tr("Torrent Data Save Folder");
	
	private TorrentSaveFolderComponent COMPONENT;
	
	public TorrentSaveFolderPaneItem() {
        super(TITLE, I18n.tr("Choose the folder where the torrent data will be saved"));
        
        COMPONENT = new TorrentSaveFolderComponent(false);
        add(COMPONENT);
	}

	@Override
	public void initOptions() {
		// nothing the component does it.		
	}

	@Override
	public boolean applyOptions() throws IOException {
		if (!COMPONENT.isTorrentSaveFolderPathValid(true, SharingSettings.getAllSaveDirectories(),GuiCoreMediator.getFileManager().getAllSharedDirectories())) {
			GUIMediator.showError(COMPONENT.getError());
			throw new IOException();
		}

		boolean restart = isDirty();
		
        SharingSettings.TORRENT_DATA_DIR_SETTING.setValue(new File(COMPONENT.getTorrentSaveFolderPath()));
        SharingSettings.SEED_FINISHED_TORRENTS.setValue(COMPONENT.isSeedingSelected());
        
        if (!SharingSettings.SEED_FINISHED_TORRENTS.getValue()) {
            UploadMediator.instance().stopSeeding();
        }
		
		return restart;
	}

	@Override
	public boolean isDirty() {
		return !SharingSettings.TORRENT_DATA_DIR_SETTING.getValueAsString().equals(COMPONENT.getTorrentSaveFolderPath()) || SharingSettings.SEED_FINISHED_TORRENTS.getValue() != COMPONENT.isSeedingSelected();
	}
}
