package com.limegroup.gnutella.gui.library;

import java.io.File;

import javax.swing.Icon;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;

public class SpeciallySharedFilesDirectoryHolder extends AbstractDirectoryHolder {

	public String getName() {
		return I18n.tr
			("Individually Shared Files");
	}

	public String getDescription() {
		return I18n.tr
			("List of All Individually Shared Files");
	}

	public File getDirectory() {
		return null;
	}
	
	public boolean isEmpty() {
	    return !GuiCoreMediator.getFileManager().hasIndividualFiles();
	}
	
	public File[] getFiles() {
	    return GuiCoreMediator.getFileManager().getIndividualFiles();
	}
	
	public boolean accept(File file) {
		return GuiCoreMediator.getFileManager().isIndividualShare(file); 
	}
	
	public Icon getIcon() {
		return GUIMediator.getThemeImage("multifile_small");
	}
}
