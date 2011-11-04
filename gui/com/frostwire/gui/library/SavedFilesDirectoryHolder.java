package com.frostwire.gui.library;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;

import org.limewire.setting.FileSetting;

import com.frostwire.gui.bittorrent.TorrentUtil;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.settings.LibrarySettings;

public class SavedFilesDirectoryHolder extends FileSettingDirectoryHolder {
    
    private final MediaType type;
    
    private File[] cache;

	public SavedFilesDirectoryHolder(FileSetting saveDir, String name) {
		super(saveDir, name);
		type = MediaType.getAnyTypeMediaType();
	}
	
	public Icon getIcon() {
		return GUIMediator.getThemeImage("save");
	}
	
	public boolean accept(File file) {
        return super.accept(file) && type.matches(file.getName()) && !file.isDirectory();
    }
	
	private Set<File> getFilesRecursively(File folder, Set<File> excludeFolders) {
		File[] listFiles = folder.listFiles();
		
		if (listFiles == null || listFiles.length == 0) {
			return new HashSet<File>();
		}
		
		Set<File> results = new HashSet<File>();
		
		for (File f : listFiles) {
			if (!f.isDirectory() && 
				!_hideFiles.contains(f)) {
				results.add(f);
			} else if (f.isDirectory() && !excludeFolders.contains(f)) {
				results.addAll(getFilesRecursively(f, excludeFolders));
			}
		}
		
		return results;
	}
	
	public void clearCache() {
		cache = null;
	}
	
	@Override
	public File[] getFiles() {
		
		if (cache != null && cache.length > 0) {
			return cache;
		}
		
		_hideFiles = TorrentUtil.getIgnorableFiles();
		
		Set<File> directoriesToNotInclude = LibrarySettings.DIRECTORIES_NOT_TO_INCLUDE.getValue();
		Set<File> files = getFilesRecursively(getDirectory(),directoriesToNotInclude);
		
		cache = files.toArray(new File[0]);
		
		return cache;
	}
}