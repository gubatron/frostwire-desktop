package com.frostwire.gui.library;

import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.limegroup.gnutella.gui.I18n;

public class LibraryPlaylists extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 6317109161466445259L;

    private final DefaultListModel _model;

    private JList _list;
    private JScrollPane _scrollPane;

    public LibraryPlaylists() {

        _model = new DefaultListModel();
        // dummy data
        _model.addElement(I18n.tr("New Playlist"));
        _model.addElement(I18n.tr("Dance department Podcast"));
        _model.addElement(I18n.tr("Same other playlist"));
        _model.addElement(I18n.tr("Favorite artist discography"));
        _model.addElement(I18n.tr("Audiobook playlist"));

        setupUI();
    }

    protected void setupUI() {
        setLayout(new BorderLayout());

        _list = new JList(_model);

        _scrollPane = new JScrollPane(_list);

        add(_scrollPane);
    }
}
