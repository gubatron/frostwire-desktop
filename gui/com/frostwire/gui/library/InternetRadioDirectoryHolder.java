package com.frostwire.gui.library;

import java.io.File;

import javax.swing.Icon;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;

class InternetRadioDirectoryHolder implements DirectoryHolder {

    private final Icon icon;

    public InternetRadioDirectoryHolder() {
        icon = GUIMediator.getThemeImage("radio_small");
    }

    public boolean accept(File pathname) {
        return true;
    }

    public String getName() {
        return I18n.tr("Radio");
    }

    public String getDescription() {
        return I18n.tr("Internet Radio");
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
