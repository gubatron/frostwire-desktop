package com.limegroup.gnutella.gui.library;

import java.io.File;

import com.limegroup.gnutella.library.SharingUtils;

/**
 * DirectoryHandler implementation backed by a simple directory.
 */
public class FileDirectoryHolder extends AbstractDirectoryHolder {

	private File dir;
	
	public FileDirectoryHolder(File dir) {
		this.dir = dir;
	}
	
	public String getName() {
		return dir.getName();
	}

	public String getDescription() {
		return dir.getAbsolutePath();
	}

	public File getDirectory() {
		return dir;
	}
	
	public boolean accept(File f) {
	    return super.accept(f) && (SharingUtils.isFilePhysicallyShareable(f) || f.isDirectory());
    }
}
