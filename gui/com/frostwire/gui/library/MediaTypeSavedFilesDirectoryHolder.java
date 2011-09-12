package com.frostwire.gui.library;

import java.io.File;

import javax.swing.Icon;

import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.search.NamedMediaType;

public class MediaTypeSavedFilesDirectoryHolder implements DirectoryHolder {

	private MediaType type;
	
	public MediaTypeSavedFilesDirectoryHolder(MediaType type) {
		this.type = type;
	}
	
	public boolean accept(File file) {
		return type.matches(file.getName());
	}

	public Icon getIcon() {
		NamedMediaType nmt = NamedMediaType.getFromMediaType(type);
		return nmt.getIcon();
	}

    public String getName() {
        return NamedMediaType.getFromMediaType(type).getName();
    }

    public String getDescription() {
        return I18n.tr("Holds the Results for " + type.getDescriptionKey());
    }

    public File getDirectory() {
        return null;
    }

    public File[] getFiles() {
        return new File[0];
    }

    public int size() {
        return 0;
    }

    public boolean isEmpty() {
        return true;
    }
}
