package com.frostwire.gui.library;

import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.limegroup.gnutella.gui.I18n;

public class LibraryFiles extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 9192882931064269836L;

    private final DefaultListModel _model;

    private JList _list;
    private JScrollPane _scrollPane;

    public LibraryFiles() {

        _model = new DefaultListModel();
        _model.addElement(I18n.tr("Finished Downloads"));
        _model.addElement(I18n.tr("Audio"));
        _model.addElement(I18n.tr("Video"));
        _model.addElement(I18n.tr("Programs"));
        _model.addElement(I18n.tr("Documents"));
        _model.addElement(I18n.tr("Torrents"));

        setupUI();
    }

    protected void setupUI() {
        setLayout(new BorderLayout());

        _list = new JList(_model);

        _scrollPane = new JScrollPane(_list);

        add(_scrollPane);
    }
}
