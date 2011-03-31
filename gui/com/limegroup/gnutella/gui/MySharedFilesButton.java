package com.limegroup.gnutella.gui;

import java.awt.Dimension;

import com.limegroup.gnutella.gui.actions.MySharedFilesAction;

public final class MySharedFilesButton extends URLLabel {
    
    /**
     * 
     */
    private static final long serialVersionUID = -7176716769779145964L;

    public MySharedFilesButton() {
        super(new MySharedFilesAction());
        
        setMinimumSize(new Dimension(150, 20));
    }
}
