package com.limegroup.gnutella.gui.library;

import java.io.File;

import javax.swing.Icon;

import org.limewire.setting.FileSetting;

import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.search.NamedMediaType;

public class MediaTypeSavedFilesDirectoryHolder extends SavedFilesDirectoryHolder {

	private MediaType type;
	
	public MediaTypeSavedFilesDirectoryHolder(FileSetting saveDir, String name, 
											  MediaType type) {
		super(saveDir, name);
		if (type == null) {
			throw new NullPointerException();
		}
		this.type = type;
	}
	
	public boolean accept(File file) {
		return super.accept(file) && type.matches(file.getName());
	}

	public Icon getIcon() {
		NamedMediaType nmt = NamedMediaType.getFromMediaType(type);
		return nmt.getIcon();
	}
}
