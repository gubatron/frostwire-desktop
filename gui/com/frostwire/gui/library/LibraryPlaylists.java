package com.frostwire.gui.library;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.frostwire.alexandria.Playlist;
import com.limegroup.gnutella.gui.I18n;

public class LibraryPlaylists extends JPanel {

    private static final long serialVersionUID = 6317109161466445259L;

    private DefaultListModel _model;
    
    private LibraryPlaylistsListCell _newPlaylistCell;    
    private ActionListener _newPlaylistAction;
    
    private LibraryPlaylistsListCell _defaultPlaylistCell;

    private JList _list;
    private JScrollPane _scrollPane;

    public LibraryPlaylists() {
        setupUI();
    }

    protected void setupUI() {
        setLayout(new BorderLayout());

        setupModel();
        setupList();

        _scrollPane = new JScrollPane(_list);

        add(_scrollPane);
    }
    
    private void setupModel() {
        _model = new DefaultListModel();
        
        _newPlaylistAction = new NewPlaylistActionListener();
        _newPlaylistCell = new LibraryPlaylistsListCell(I18n.tr("New Playlist"), null, null, _newPlaylistAction);
        
        //_defaultPlaylistCell new LibraryPlaylistsListCell(null, null, playlist, null);
        
        _model.addElement(_newPlaylistCell);
    }
    
    private void setupList() {
        _list = new JList(_model);
    }

    private class LibraryPlaylistsListCell {

        private final String _text;
        private final Icon _icon;
        private final Playlist _playlist;
        private final ActionListener _action;

        public LibraryPlaylistsListCell(String text, Icon icon, Playlist playlist, ActionListener action) {
            _text = text;
            _icon = icon;
            _playlist = playlist;
            _action = action;
        }

        public String getText() {
            if (_text != null) {
                return _text;
            } else if (_playlist != null && _playlist.getName() != null) {
                return _playlist.getName();
            } else {
                return "";
            }
        }

        public Icon getIcon() {
            return _icon;
        }

        public Playlist getPlaylist() {
            return _playlist;
        }

        public ActionListener getAction() {
            return _action;
        }
    }
    
    private class NewPlaylistActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            
        }
    }
}
