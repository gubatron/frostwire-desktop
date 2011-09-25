package com.frostwire.gui.library;

import java.io.File;

import javax.swing.Icon;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;

class StarredDirectoryHolder implements DirectoryHolder {

    private final Icon icon;

    public StarredDirectoryHolder() {
        icon = GUIMediator.getThemeImage("star_on");
    }

    public boolean accept(File pathname) {
        return true;
    }

    public String getName() {
        return I18n.tr("Starred");
    }

    public String getDescription() {
        return I18n.tr("Starred");
    }

    public File getDirectory() {
        return null;
    }

    public File[] getFiles() {
        return null;
    }

    public int size() {
        return 0;
    }

    public Icon getIcon() {
        return icon;
    }

    public boolean isEmpty() {
        return false;
    }
}
