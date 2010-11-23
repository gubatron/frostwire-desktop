package com.frostwire.gnutella.gui.android;

import javax.swing.DefaultListModel;

public class FileDescriptorListModel extends DefaultListModel {

    /**
     * 
     */
    private static final long serialVersionUID = 3826940677788298380L;
    
    public void update(FileDescriptor fileDescriptor) {
        int index = indexOf(fileDescriptor);
        fireContentsChanged(this, index, index);
    }
}
