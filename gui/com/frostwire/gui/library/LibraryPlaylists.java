package com.frostwire.gui.library;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultListCellRenderer;

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
        _newPlaylistCell = new LibraryPlaylistsListCell(I18n.tr("New Playlist"), I18n.tr("Creates a new Playlist"), null, null, _newPlaylistAction);
        
        _defaultPlaylistCell = new LibraryPlaylistsListCell(null, null, null, LibraryMediator.instance().getLibrary().getDefaultPlaylist(), null);
        
        _model.addElement(_newPlaylistCell);
        _model.addElement(_defaultPlaylistCell);
    }
    
    private void setupList() {
        _list = new JList(_model);
    }

    private class LibraryPlaylistsListCell {

        private final String _text;
        private final String _description;
        private final Icon _icon;
        private final Playlist _playlist;
        private final ActionListener _action;

        public LibraryPlaylistsListCell(String text, String description, Icon icon, Playlist playlist, ActionListener action) {
            _text = text;
            _description = description;
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
        
        public String getDescription() {
            if (_description != null) {
                return _description;
            } else if (_playlist != null && _playlist.getDescription() != null) {
                return _playlist.getDescription();
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
    
    private class LibraryFileCellRenderer extends SubstanceDefaultListCellRenderer {

        /**
         * 
         */
        private static final long serialVersionUID = -2047182373734965968L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            LibraryPlaylistsListCell cell = (LibraryPlaylistsListCell) value;
            setText(cell.getText());
            setToolTipText(cell.getDescription());
            Icon icon = cell.getIcon();
            if (icon != null) {
                setIcon(icon);
            }
            return this;
        }
    }
    
    private class NewPlaylistActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            
        }
    }
}
