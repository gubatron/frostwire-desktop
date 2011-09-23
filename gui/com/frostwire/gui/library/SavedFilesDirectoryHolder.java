package com.frostwire.gui.library;

import java.io.File;

import javax.swing.Icon;

import org.limewire.setting.FileSetting;

import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.GUIMediator;

public class SavedFilesDirectoryHolder extends FileSettingDirectoryHolder {
    
    private final MediaType type;

	public SavedFilesDirectoryHolder(FileSetting saveDir, String name) {
		super(saveDir, name);
		type = MediaType.getAnyTypeMediaType();
	}
	
	public Icon getIcon() {
		return GUIMediator.getThemeImage("save");
	}
	
	public boolean accept(File file) {
        return type.matches(file.getName());
    }
}
