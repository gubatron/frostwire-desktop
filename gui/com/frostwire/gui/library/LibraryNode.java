package com.frostwire.gui.library;

import javax.swing.tree.DefaultMutableTreeNode;

public abstract class LibraryNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = -2284277166512093559L;

    public LibraryNode(Object userObject) {
        super(userObject);
    }
}
