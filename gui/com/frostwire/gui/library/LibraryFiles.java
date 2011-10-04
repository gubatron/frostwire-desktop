package com.frostwire.gui.library;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.limewire.util.CommonUtils;
import org.limewire.util.OSUtils;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultListCellRenderer;

import com.frostwire.alexandria.Playlist;
import com.frostwire.gui.bittorrent.TorrentUtil;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.actions.LimeAction;
import com.limegroup.gnutella.gui.options.ConfigureOptionsAction;
import com.limegroup.gnutella.gui.options.OptionsConstructor;
import com.limegroup.gnutella.gui.search.NamedMediaType;
import com.limegroup.gnutella.gui.tables.DefaultMouseListener;
import com.limegroup.gnutella.gui.tables.MouseObserver;
import com.limegroup.gnutella.gui.themes.SkinMenuItem;
import com.limegroup.gnutella.gui.themes.SkinPopupMenu;
import com.limegroup.gnutella.gui.util.BackgroundExecutorService;
import com.limegroup.gnutella.settings.SharingSettings;

public class LibraryFiles extends AbstractLibraryListPanel {

    private static final long serialVersionUID = 9192882931064269836L;

    private LibraryFilesListCell _finishedDownloadsCell;
    private SavedFilesDirectoryHolder _finishedDownloadsHolder;

    private LibraryFilesListCell _torrentsCell;
    private TorrentDirectoryHolder _torrentsHolder;

    private ListMouseObserver _listMouseObserver;
    private ListSelectionListener _listSelectionListener;

    private DefaultListModel _model;
    private JList _list;
    private JScrollPane _scrollPane;

    private JPopupMenu _popup;
    private Action refreshAction = new RefreshAction();
    private Action exploreAction = new ExploreAction();

    public LibraryFiles() {
        setupUI();
    }

    public DirectoryHolder getSelectedDirectoryHolder() {
        LibraryFilesListCell cell = (LibraryFilesListCell) _list.getSelectedValue();
        return cell != null ? cell.getDirectoryHolder() : null;
    }

    public Dimension getRowDimension() {
        Rectangle rect = _list.getUI().getCellBounds(_list, 0, 0);
        return rect.getSize();
    }

    public int getRowsCount() {
        return _model.getSize();
    }

    public void clearSelection() {
        _list.clearSelection();
    }

    protected void setupUI() {
        setLayout(new BorderLayout());
        GUIMediator.addRefreshListener(this);

        setupModel();
        setupList();
        setupPopupMenu();

        _scrollPane = new JScrollPane(_list);

        add(_scrollPane);
    }

    private void setupModel() {
        _model = new DefaultListModel();

        _finishedDownloadsHolder = new SavedFilesDirectoryHolder(SharingSettings.TORRENT_DATA_DIR_SETTING, I18n.tr("Finished Downloads"));
        _finishedDownloadsCell = new LibraryFilesListCell(_finishedDownloadsHolder);

        _torrentsHolder = new TorrentDirectoryHolder();
        _torrentsCell = new LibraryFilesListCell(_torrentsHolder);

        _model.addElement(_finishedDownloadsCell);
        addPerMediaTypeCells();
        _model.addElement(_torrentsCell);
        _model.addElement(new LibraryFilesListCell(new StarredDirectoryHolder()));
    }

    private void setupList() {
        _listMouseObserver = new ListMouseObserver();
        _listSelectionListener = new LibraryFilesSelectionListener();
        
        _list = new LibraryIconList(_model);
        _list.setCellRenderer(new LibraryFilesCellRenderer());
        _list.addMouseListener(new DefaultMouseListener(_listMouseObserver));
        _list.addListSelectionListener(_listSelectionListener);
        _list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _list.setDragEnabled(true);
        _list.setTransferHandler(new LibraryFilesTransferHandler(_list));
      
        ToolTipManager.sharedInstance().registerComponent(_list);
    }

    private void addPerMediaTypeCells() {
        addPerMediaTypeCell(NamedMediaType.getFromMediaType(MediaType.getAudioMediaType()));
        addPerMediaTypeCell(NamedMediaType.getFromMediaType(MediaType.getVideoMediaType()));
        addPerMediaTypeCell(NamedMediaType.getFromMediaType(MediaType.getImageMediaType()));
        addPerMediaTypeCell(NamedMediaType.getFromMediaType(MediaType.getProgramMediaType()));
        addPerMediaTypeCell(NamedMediaType.getFromMediaType(MediaType.getDocumentMediaType()));
    }

    private void addPerMediaTypeCell(NamedMediaType nm) {
        DirectoryHolder holder = new MediaTypeSavedFilesDirectoryHolder(nm.getMediaType());
        LibraryFilesListCell cell = new LibraryFilesListCell(holder);
        _model.addElement(cell);
    }

    private void setupPopupMenu() {
        _popup = new SkinPopupMenu();
        _popup.add(new SkinMenuItem(refreshAction));
        _popup.add(new SkinMenuItem(exploreAction));
        _popup.add(new SkinMenuItem(new ConfigureOptionsAction(OptionsConstructor.SHARED_KEY, I18n.tr("Configure Options"), I18n
                .tr("You can configure the FrostWire\'s Options."))));
    }

    public void refreshSelection() {
        LibraryFilesListCell node = (LibraryFilesListCell) _list.getSelectedValue();

        if (node == null) {
            return;
        }

        DirectoryHolder directoryHolder = getSelectedDirectoryHolder();

        if (directoryHolder instanceof StarredDirectoryHolder) {
            Playlist playlist = LibraryMediator.getLibrary().getStarredPlaylist();
            LibraryMediator.instance().updateTableItems(playlist);
            String status = LibraryUtils.getPlaylistDurationInDDHHMMSS(playlist) + ", " + playlist.getItems().size() + " " + I18n.tr("tracks");
            LibraryMediator.instance().getLibrarySearch().setStatus(status);
        } else {
            LibraryMediator.instance().updateTableFiles(node.getDirectoryHolder());

            if (directoryHolder != null && directoryHolder instanceof MediaTypeSavedFilesDirectoryHolder) {
                MediaTypeSavedFilesDirectoryHolder mtsfdh = (MediaTypeSavedFilesDirectoryHolder) directoryHolder;
                BackgroundExecutorService.schedule(new SearchByMediaTypeRunnable(mtsfdh));
            }
        }

        LibraryMediator.instance().getLibrarySearch().clear();
    }

    private class LibraryFilesCellRenderer extends SubstanceDefaultListCellRenderer {

        private static final long serialVersionUID = 4566657973124277716L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            LibraryFilesListCell cell = (LibraryFilesListCell) value;
            DirectoryHolder dh = cell.getDirectoryHolder();
            setText(dh.getName());
            setToolTipText(dh.getDescription());
            Icon icon = dh.getIcon();
            if (icon != null) {
                setIcon(icon);
            }
            return this;
        }
    }

    public static class LibraryFilesListCell {

        private DirectoryHolder _holder;

        public LibraryFilesListCell(DirectoryHolder holder) {
            _holder = holder;
        }

        public DirectoryHolder getDirectoryHolder() {
            return _holder;
        }
    }

    private class ListMouseObserver implements MouseObserver {
        public void handleMouseClick(MouseEvent e) {
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

            LibraryFilesListCell node = (LibraryFilesListCell) _list.getSelectedValue();

            if (node == null) {
                return;
            }

            LibraryMediator.instance().getLibraryPlaylists().clearSelection();

            refreshSelection();
        }
    }

    private final class SearchByMediaTypeRunnable implements Runnable {

        private final MediaTypeSavedFilesDirectoryHolder _mtsfdh;

        public SearchByMediaTypeRunnable(MediaTypeSavedFilesDirectoryHolder mtsfdh) {
            _mtsfdh = mtsfdh;
        }
        

        public void run() {
            GUIMediator.safeInvokeLater(new Runnable() {
                public void run() {
                    LibraryMediator.instance().clearLibraryTable();
                }
            });

            File file = SharingSettings.TORRENT_DATA_DIR_SETTING.getValue();

            Set<File> ignore = TorrentUtil.getIncompleteFiles();
            ignore.addAll(TorrentUtil.getSkipedFiles());

            search(file, ignore);
            
            // special case for audio
            if (_mtsfdh.getMediaType().equals(MediaType.getAudioMediaType())) {
                File musicFile = null;
                if (OSUtils.isMacOSX()) {
                    musicFile = new File(CommonUtils.getUserHomeDir(), "Music");
                } else if (OSUtils.isWindowsXP()) {
                    musicFile = new File(CommonUtils.getUserHomeDir(), "My Documents" + File.separator + "My Music");
                } else if (OSUtils.isWindowsVista() || OSUtils.isWindows7()) {
                    musicFile = new File(CommonUtils.getUserHomeDir(), "Music");
                }
                search(musicFile, new HashSet<File>());
            }

            LibraryFiles.this.executePendingRunnables();
        }

        private void search(File file, Set<File> ignore) {

            if (file == null || !file.isDirectory() || !file.exists()) {
                return;
            }

            List<File> directories = new ArrayList<File>();
            final List<File> files = new ArrayList<File>();

            for (File child : file.listFiles()) {

                DirectoryHolder directoryHolder = getSelectedDirectoryHolder();
                if (!_mtsfdh.equals(directoryHolder)) {
                    return;
                }

                if (ignore.contains(child)) {
                    continue;
                }
                if (child.isHidden()) {
                    continue;
                }
                if (child.isDirectory()) {
                    directories.add(child);
                } else if (_mtsfdh.accept(child)) {
                    files.add(child);
                }
            }

            Runnable r = new Runnable() {
                public void run() {
                    LibraryMediator.instance().addFilesToLibraryTable(files);
                }
            };
            
            GUIMediator.safeInvokeLater(r);

            for (File directory : directories) {
                search(directory, ignore);
            }
        }
    }

    private class RefreshAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = 412879927060208864L;

        public RefreshAction() {
            putValue(Action.NAME, I18n.tr("Refresh"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Refresh selected"));
            putValue(LimeAction.ICON_NAME, "LIBRARY_REFRESH");
        }

        public void actionPerformed(ActionEvent e) {
            DirectoryHolder directoryHolder = getSelectedDirectoryHolder();
            if (directoryHolder == null) {
                return;
            }
            refreshSelection();
        }
    }

    private class ExploreAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = 2767346265174793478L;

        public ExploreAction() {
            putValue(Action.NAME, I18n.tr("Explore"));
            putValue(Action.SHORT_DESCRIPTION, I18n.tr("Open Library Folder"));
            putValue(LimeAction.ICON_NAME, "LIBRARY_EXPLORE");
        }

        public void actionPerformed(ActionEvent e) {
            DirectoryHolder directoryHolder = getSelectedDirectoryHolder();
            if (directoryHolder == null) {
                return;
            }
            File directory = directoryHolder.getDirectory();
            if (directory == null) {
                directory = _finishedDownloadsHolder.getDirectory();
            }
            GUIMediator.launchExplorer(directory);
        }
    }

    public void selectFinishedDownloads() {
        _list.setSelectedValue(_finishedDownloadsCell, true);
    }

    @Override
    public void refresh() {
        _list.repaint();
    }

    public void selectAudio() {
        int size = _model.getSize();

        for (int i = 0; i < size; i++) {
            try {
                LibraryFilesListCell cell = (LibraryFilesListCell) _model.get(i);

                if (cell.getDirectoryHolder() instanceof MediaTypeSavedFilesDirectoryHolder
                        && ((MediaTypeSavedFilesDirectoryHolder) cell.getDirectoryHolder()).getMediaType().equals(MediaType.getAudioMediaType())) {
                    _list.setSelectedValue(cell, true);
                    return;
                }
            } catch (Exception e) {
            }
        }
    }

    public void selectStarred() {
        int size = _model.getSize();

        for (int i = 0; i < size; i++) {
            try {
                LibraryFilesListCell cell = (LibraryFilesListCell) _model.get(i);

                if (cell.getDirectoryHolder() instanceof StarredDirectoryHolder) {
                    _list.setSelectedValue(cell, true);
                    return;
                }
            } catch (Exception e) {
            }
        }

    }
}