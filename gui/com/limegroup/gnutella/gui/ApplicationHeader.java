package com.limegroup.gnutella.gui;

import java.awt.Button;
import java.awt.FlowLayout;

import javax.swing.JPanel;

public class ApplicationHeader extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 4800214468508213106L;


    public ApplicationHeader() {
        setLayout(new FlowLayout());
        
        Button b1 = new Button("Search");
        add(b1);
        Button b2 = new Button("Library");
        add(b2);
        Button b3 = new Button("Chat");
        add(b3);
    }
}
