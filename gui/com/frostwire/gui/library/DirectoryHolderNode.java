package com.frostwire.gui.library;


public class DirectoryHolderNode extends LibraryNode {

    private static final long serialVersionUID = 8351386662518599629L;

    private DirectoryHolder directoryHolder;

    public DirectoryHolderNode(DirectoryHolder directoryHolder) {
        super(directoryHolder.getName());
        this.directoryHolder = directoryHolder;
    }

    public DirectoryHolder getDirectoryHolder() {
        return directoryHolder;
    }
}
