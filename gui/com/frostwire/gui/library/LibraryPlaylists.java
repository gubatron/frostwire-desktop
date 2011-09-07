package com.frostwire.gui.library;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultListCellRenderer;

import com.frostwire.alexandria.Library;
import com.frostwire.alexandria.Playlist;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.options.ConfigureOptionsAction;
import com.limegroup.gnutella.gui.options.OptionsConstructor;
import com.limegroup.gnutella.gui.tables.DefaultMouseListener;
import com.limegroup.gnutella.gui.tables.MouseObserver;
import com.limegroup.gnutella.gui.themes.SkinMenuItem;
import com.limegroup.gnutella.gui.themes.SkinPopupMenu;

public class LibraryPlaylists extends JPanel {

    private static final long serialVersionUID = 6317109161466445259L;

    private DefaultListModel _model;
    private int _selectedIndexToRename;

    private LibraryPlaylistsListCell _newPlaylistCell;
    private ActionListener _newPlaylistAction;

    private ActionListener _selectedPlaylistAction;

    private LibraryPlaylistsMouseObserver _listMouseObserver;
    private ListSelectionListener _listSelectionListener;

    private JList _list;
    private JScrollPane _scrollPane;
    private JTextField _textName;

    private JPopupMenu _popup;
    private Action refreshAction = new RefreshAction();
    private Action exploreAction = new ExploreAction();

    public LibraryPlaylists() {
        setupUI();
    }
    
    public Dimension getRowDimension() {
        Rectangle rect = _list.getUI().getCellBounds(_list, 0, 0);
        return rect.getSize();
    }
    
    public void addPlaylist(Playlist playlist) {
        LibraryPlaylistsListCell cell = new LibraryPlaylistsListCell(null, null, null, playlist, _selectedPlaylistAction);
        _model.addElement(cell);
    }

    protected void setupUI() {
        setLayout(new BorderLayout());

        setupPopupMenu();
        setupModel();
        setupList();

        _scrollPane = new JScrollPane(_list);

        add(_scrollPane);
    }

    private void setupPopupMenu() {
        _popup = new SkinPopupMenu();
        _popup.add(new SkinMenuItem(refreshAction));
        _popup.add(new SkinMenuItem(exploreAction));
        _popup.add(new SkinMenuItem(new ConfigureOptionsAction(OptionsConstructor.SHARED_KEY, I18n.tr("Configure Options"), I18n
                .tr("You can configure the FrostWire\'s Options."))));
    }

    private void setupModel() {
        _model = new DefaultListModel();

        _newPlaylistAction = new NewPlaylistActionListener();
        _newPlaylistCell = new LibraryPlaylistsListCell(I18n.tr("New Playlist"), I18n.tr("Creates a new Playlist"), null, null, _newPlaylistAction);

        Library library = LibraryMediator.getLibrary();

        _selectedPlaylistAction = new SelectedPlaylistActionListener();
        
        _model.addElement(_newPlaylistCell);
        for (Playlist playlist : library.getPlaylists()) {
            LibraryPlaylistsListCell cell = new LibraryPlaylistsListCell(null, null, null, playlist, _selectedPlaylistAction);
            _model.addElement(cell);
        }
    }

    private void setupList() {
        _listMouseObserver = new LibraryPlaylistsMouseObserver();
        _listSelectionListener = new LibraryFilesSelectionListener();

        _list = new JList(_model);
        _list.setCellRenderer(new LibraryPlaylistsCellRenderer());
        _list.addMouseListener(new DefaultMouseListener(_listMouseObserver));
        _list.addListSelectionListener(_listSelectionListener);
        _list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _list.setLayoutOrientation(JList.VERTICAL);
        _list.setPrototypeCellValue(new LibraryPlaylistsListCell("test", "", null, null, null));
        _list.setVisibleRowCount(-1);
        ToolTipManager.sharedInstance().registerComponent(_list);
        
        _textName = new JTextField();
        _textName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                textName_keyPressed(e);
            }
        });
        _textName.setVisible(false);
        
        _list.add(_textName);
    }
    
    protected void textName_keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (_selectedIndexToRename != -1 && key == KeyEvent.VK_ENTER) {
            renameSelectedItem(_selectedIndexToRename);
        } else if (_selectedIndexToRename == -1 && key == KeyEvent.VK_ENTER) {
            createNewPlaylist();
        } else if (key == KeyEvent.VK_ESCAPE) {
            _textName.setVisible(false);
        }
    }

    private void refreshListCellSelection() {
        LibraryPlaylistsListCell cell = (LibraryPlaylistsListCell) _list.getSelectedValue();

        if (cell == null)
            return;

        Playlist playlist = cell.getPlaylist();
        playlist.refresh();

        LibraryMediator.instance().updateTableItems(playlist.getItems());

        //        DirectoryHolder directoryHolder = getSelectedDirectoryHolder();
        //        if (directoryHolder != null && directoryHolder instanceof MediaTypeSavedFilesDirectoryHolder) {
        //            LibraryMediator.instance().showView(LibraryMediator.FILES_TABLE_KEY);
        //            MediaTypeSavedFilesDirectoryHolder mtsfdh = (MediaTypeSavedFilesDirectoryHolder) directoryHolder;
        //            BackgroundExecutorService.schedule(new SearchByMediaTypeRunnable(mtsfdh));
        //        }
    }
    
    private void actionStartRename() {
        cancelEdit();
        int index = _list.getSelectedIndex();
        if (index != -1) {
            startEdit(index);
        }
    }

    private void startEdit(int index) {
        if (index < 0) {
            return;
        }

        LibraryPlaylistsListCell cell = (LibraryPlaylistsListCell) _model.getElementAt(index);
        _selectedIndexToRename = cell.getPlaylist() != null ? index : -1;
        
        String text = cell.getText();

        Rectangle rect = _list.getUI().getCellBounds(_list, index, index);
        Dimension lsize = rect.getSize();
        Point llocation = rect.getLocation();
        _textName.setSize(lsize);
        _textName.setLocation(llocation);

        _textName.setText(text);
        _textName.setSelectionStart(0);
        _textName.setSelectionEnd(text.length());
        
        _textName.setVisible(true);

        _textName.requestFocusInWindow();
        _textName.requestFocus();
    }
    
    private void renameSelectedItem(int index) {
        if (!_textName.isVisible()) {
            return;
        }
//        String text = _textName.getText();
//        if (text != null && text.length() > 0) {
//            if (text.indexOf('.') == -1) { // no extension? put the old
//                                           // extension
//                LocalFile localFile = (LocalFile) _model.getElementAt(index);
//                if (localFile != null && localFile.getFile().isFile() && localFile.getExt() != null) {
//                    text += "." + localFile.getExt();
//                }
//            }
//            _model.rename(index, text);
//        }
        _textName.setVisible(false);
    }
    
    private void createNewPlaylist() {
        if (!_textName.isVisible()) {
            return;
        }
        
        String name = _textName.getText();
        
        Library library = LibraryMediator.getLibrary();
        
        Playlist playlist = library.newPlaylist(name, name);
        playlist.save();
        LibraryPlaylistsListCell cell = new LibraryPlaylistsListCell(null, null, null, playlist, _selectedPlaylistAction);
        _model.addElement(cell);
        _list.setSelectedValue(cell, true);
        
        _textName.setVisible(false);
    }

    private void cancelEdit() {
        _selectedIndexToRename = -1;
        _textName.setVisible(false);
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

    private class LibraryPlaylistsCellRenderer extends SubstanceDefaultListCellRenderer {

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

    private class SelectedPlaylistActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            refreshListCellSelection();
        }
    }

    private class LibraryPlaylistsMouseObserver implements MouseObserver {

        public void handleMouseClick(MouseEvent e) {
            int index = _list.locationToIndex(e.getPoint());
            _list.setSelectedIndex(index);
            if (((LibraryPlaylistsListCell) _list.getSelectedValue()).getPlaylist() == null) {
                actionStartRename();
            }
        }

        /**
         * Handles when the mouse is double-clicked.
         */
        public void handleMouseDoubleClick(MouseEvent e) {
        }

        /**
         * Handles a right-mouse click.
         */
        public void handleRightMouseClick(MouseEvent e) {
        }

        /**
         * Handles a trigger to the popup menu.
         */
        public void handlePopupMenu(MouseEvent e) {
            _list.setSelectedIndex(_list.locationToIndex(e.getPoint()));
            _popup.show(_list, e.getX(), e.getY());
        }
    }

    private class LibraryFilesSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }

            LibraryPlaylistsListCell cell = (LibraryPlaylistsListCell) _list.getSelectedValue();

            if (cell == null)
                return;

            if (cell.getAction() != null) {
                cell.getAction().actionPerformed(null);
            }
        }
    }

    private class RefreshAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = 3259221218056223423L;

        public RefreshAction() {
            putValue(Action.NAME, I18n.tr("Refresh"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Refresh selected"));
            putValue(LimeAction.ICON_NAME, "LIBRARY_REFRESH");
        }

        public void actionPerformed(ActionEvent e) {
            refreshListCellSelection();
        }
    }

    private class ExploreAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = 520856485566457934L;

        public ExploreAction() {
            putValue(Action.NAME, I18n.tr("Explore"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Open Library Folder"));
            putValue(LimeAction.ICON_NAME, "LIBRARY_EXPLORE");
        }

        public void actionPerformed(ActionEvent e) {
            //            DirectoryHolder directoryHolder = getSelectedDirectoryHolder();
            //            if (directoryHolder == null) {
            //                return;
            //            }
            //            File directory = directoryHolder.getDirectory();
            //            if (directory == null) {
            //                directory = _finishedDownloadsHolder.getDirectory();
            //            }
            //            GUIMediator.launchExplorer(directory);        
        }
    }
}
