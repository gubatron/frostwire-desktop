package com.frostwire.gui.library;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LibrarySearch extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 2266243762191789491L;

    public LibrarySearch() {
        setupUI();
    }
    
    protected void setupUI() {
        setLayout(new BorderLayout());
        
        add(new JLabel("...Search results"), BorderLayout.LINE_START);
        add(new JTextField("input here"), BorderLayout.LINE_END);
    }
}
