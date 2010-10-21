package com.limegroup.gnutella.gui.library;

import java.io.File;

import javax.swing.Icon;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;

/**
 * Holder for files that are displayed in the Library Tree under the node 
 * Store->Special Purchased Files. 
 * 
 * These represent songs purchased from the LWS found in the shared directories folders.
 *
 */
public class LWSSpecialFilesHolder extends AbstractDirectoryHolder {

    public String getName() {
        return I18n.tr
            ("Individual Store Files");
    }

    public String getDescription() {
        return I18n.tr
            ("List of All Individual Purchased Files");
    }

    public File getDirectory() {
        return null;
    }
    
    public boolean isEmpty() {
        return !GuiCoreMediator.getFileManager().hasIndividualStoreFiles();
    }
    
    public File[] getFiles() {
        return GuiCoreMediator.getFileManager().getIndividualStoreFiles();
    }
    
    public boolean accept(File file) {
        return GuiCoreMediator.getFileManager().isIndividualStore(file); 
    }
    
    public Icon getIcon() {
        return GUIMediator.getThemeImage("multifile_small");
    }

    @Override
    public boolean isStoreNode(){
        return true;
    }
}
