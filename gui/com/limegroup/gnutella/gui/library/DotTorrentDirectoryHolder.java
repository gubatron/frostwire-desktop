package com.limegroup.gnutella.gui.library;

import javax.swing.Icon;

import com.limegroup.gnutella.gui.search.NamedMediaType;
import com.limegroup.gnutella.settings.SharingSettings;

public class DotTorrentDirectoryHolder extends FileSettingDirectoryHolder {
	
	public DotTorrentDirectoryHolder() {
		super(SharingSettings.DEFAULT_DOT_TORRENTS_DIR_SETTING,".torrent files");
	}
	
	public Icon getIcon() {
		NamedMediaType nmt = NamedMediaType.getFromDescription("torrent");
		return nmt.getIcon();
	}
}
