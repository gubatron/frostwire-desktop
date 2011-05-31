package com.limegroup.gnutella.gui.library;

import java.io.File;
import java.util.Set;

import javax.swing.Icon;

import com.frostwire.bittorrent.AzureusCoreUtil;

/**
 * Abstract implementation of the DirectoryHolder interface, providing a filtered
 * way for listing the files in the directory.
 */
public abstract class AbstractDirectoryHolder implements DirectoryHolder {

    private Set<File> _incompleteFiles;
    
    /**
     * Uses the file filter for listing the files in the directory provided by
     * {@link #getDirectory}.  
     */
    public File[] getFiles() {
        
        _incompleteFiles = AzureusCoreUtil.getIncompleteFiles();
        
        File[] files = getDirectory().listFiles(this);
        return (files != null) ? files : new File[0];
    }

    public boolean accept(File pathname) {
        if (_incompleteFiles.contains(pathname)) {
            return false;
        }
        
        if (!isFileVisible(pathname)) {
            return false;
        }

        File parent = pathname.getParentFile();
        return parent != null && parent.equals(getDirectory());
    }

    /**
     * Returns true if the given file is visible
     */
    protected boolean isFileVisible(File file) {
        if (file == null || !file.exists() || !file.canRead() || file.isHidden()) {
            return false;
        }

        return true;
    }

    public String getName() {
        return getDirectory().getName();
    }

    public String getDescription() {
        return getDirectory().getAbsolutePath();
    }

    /**
     * Returns the number of files that this directory holder contains.
     */
    public int size() {
        File[] files = getFiles();
        if (files == null)
            return 0;
        return files.length;
    }

    public Icon getIcon() {
        return null;
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
