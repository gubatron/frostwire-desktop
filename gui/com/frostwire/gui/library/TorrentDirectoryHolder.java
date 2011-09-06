package com.frostwire.gui.library;

import java.io.File;

import javax.swing.Icon;

import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.search.NamedMediaType;
import com.limegroup.gnutella.settings.SharingSettings;

public class TorrentDirectoryHolder extends FileSettingDirectoryHolder {
    
    private final MediaType type;
	
	public TorrentDirectoryHolder() {
		super(SharingSettings.TORRENTS_DIR_SETTING, "Torrents");
		type = MediaType.getTorrentMediaType();
	}
	
	public Icon getIcon() {
		NamedMediaType nmt = NamedMediaType.getFromMediaType(type);
		return nmt.getIcon();
	}
	
	public boolean accept(File file) {
        return type.matches(file.getName());
    }
}
