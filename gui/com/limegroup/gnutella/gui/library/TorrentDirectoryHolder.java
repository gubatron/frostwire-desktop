package com.limegroup.gnutella.gui.library;

import javax.swing.Icon;

import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.search.NamedMediaType;
import com.limegroup.gnutella.settings.SharingSettings;

public class TorrentDirectoryHolder extends FileSettingDirectoryHolder {
	
	public TorrentDirectoryHolder() {
		super(SharingSettings.DEFAULT_DOT_TORRENTS_DIR_SETTING,"Torrent files");
	}
	
	public Icon getIcon() {
		NamedMediaType nmt = NamedMediaType.getFromMediaType(MediaType.getTorrentMediaType());
		return nmt.getIcon();
	}
}
