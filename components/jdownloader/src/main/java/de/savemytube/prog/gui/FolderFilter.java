package de.savemytube.prog.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FolderFilter extends FileFilter {

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        return false;
    }

    public String getDescription() {
        // TODO Auto-generated method stub
        return "Folders";
    }

}
