package com.limegroup.gnutella.gui.library;

import java.io.File;

import javax.swing.Icon;

import com.limegroup.gnutella.downloader.IncompleteFileManager;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.SharingSettings;

public class IncompleteDirectoryHolder extends FileSettingDirectoryHolder {

	public IncompleteDirectoryHolder() {
		super(SharingSettings.INCOMPLETE_DIRECTORY,
				I18n.tr("Incomplete Files"));
	}
		
	
	public boolean accept(File file) {
		String name = file.getName();
		return super.accept(file) &&
		!file.isHidden() &&
		!name.startsWith(".") &&
		isAppropriateType(file) &&
		!name.equals("downloads.dat") &&
		!name.equals("downloads.bak");
	}
	
	public Icon getIcon() {
		return GUIMediator.getThemeImage("incomplete");
	}
	
	private boolean isAppropriateType(File file) {
		if (file.isFile())
			return true;
		
		return IncompleteFileManager.isTorrentFolder(file);
	}
}
