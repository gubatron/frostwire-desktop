package com.frostwire.gui.library;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import com.limegroup.gnutella.gui.player.MediaPlayerComponent2;

public class LibraryPlayer extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 7736253561290623706L;

    public LibraryPlayer() {
        setupUI();
    }
    
    protected void setupUI() {
        setLayout(new BorderLayout());
        
        setPreferredSize(new Dimension(100, 100));
        
        add(MediaPlayerComponent2.getInstance().getMediaPanel(), BorderLayout.LINE_END);
    }
}
