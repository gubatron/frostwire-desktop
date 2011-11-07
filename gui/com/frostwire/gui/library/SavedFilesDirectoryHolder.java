package com.frostwire.gui.library;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
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
    
	private Set<File> cache;

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
		
		if (folder.isDirectory() && excludeFolders.contains(folder)) {
			return Collections.emptySet();
		}
		
		File[] listFiles = folder.listFiles();
		
		if (listFiles == null || listFiles.length == 0) {
			return Collections.emptySet();
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
		
		if (cache != null && cache.size() > 0) {
			return cache.toArray(new File[0]);
		}
		
		_hideFiles = TorrentUtil.getIgnorableFiles();
		
		Set<File> directoriesToNotInclude = LibrarySettings.DIRECTORIES_NOT_TO_INCLUDE.getValue();		
		Set<File> directoriesToInclude = LibrarySettings.DIRECTORIES_TO_INCLUDE.getValue();
		
		Set<File> files = new HashSet<File>(); 
		
		for (File directory : directoriesToInclude) {
			files.addAll(getFilesRecursively(directory,directoriesToNotInclude));
		}
		
		cache = new HashSet<File>(files);
		
		return cache.toArray(new File[0]);
	}
	
	public Collection<File> getCache() {
		return cache;
	}
}