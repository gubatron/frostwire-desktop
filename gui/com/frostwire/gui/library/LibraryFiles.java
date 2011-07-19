package com.frostwire.gui.library;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultListCellRenderer;

import com.frostwire.gui.bittorrent.TorrentUtil;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.search.NamedMediaType;
import com.limegroup.gnutella.gui.tables.DefaultMouseListener;
import com.limegroup.gnutella.gui.tables.MouseObserver;
import com.limegroup.gnutella.gui.util.BackgroundExecutorService;
import com.limegroup.gnutella.settings.SharingSettings;

public class LibraryFiles extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 9192882931064269836L;

    private LibraryFilesListCell _finishedDownloadsCell;
    private SavedFilesDirectoryHolder _finishedDownloadsHolder;

    private LibraryFilesListCell _torrentsCell;
    private TorrentDirectoryHolder _torrentsHolder;

    private LibraryFilesMouseObserver _listMouseObserver;
    private ListSelectionListener _listSelectionListener;

    private DefaultListModel _model;
    private JList _list;
    private JScrollPane _scrollPane;

    public LibraryFiles() {
        setupUI();
    }
    
    public DirectoryHolder getSelectedDirectoryHolder() {
        return ((LibraryFilesListCell)_list.getSelectedValue()).getDirectoryHolder();
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

        _finishedDownloadsHolder = new SavedFilesDirectoryHolder(SharingSettings.TORRENT_DATA_DIR_SETTING, I18n.tr("Finished Downloads"));
        _finishedDownloadsCell = new LibraryFilesListCell(_finishedDownloadsHolder);

        _torrentsHolder = new TorrentDirectoryHolder();
        _torrentsCell = new LibraryFilesListCell(_torrentsHolder);
        
        _model.addElement(_finishedDownloadsCell);
        addPerMediaTypeCells();
        _model.addElement(_torrentsCell);
    }

    private void setupList() {
        _listMouseObserver = new LibraryFilesMouseObserver();
        _listSelectionListener = new LibraryFilesSelectionListener();

        _list = new JList(_model);
        _list.setCellRenderer(new LibraryFileCellRenderer());
        _list.addMouseListener(new DefaultMouseListener(_listMouseObserver));
        _list.addListSelectionListener(_listSelectionListener);
        _list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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

    private class LibraryFileCellRenderer extends SubstanceDefaultListCellRenderer {

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

    private class LibraryFilesListCell {

        private DirectoryHolder _holder;

        public LibraryFilesListCell(DirectoryHolder holder) {
            _holder = holder;
        }

        public DirectoryHolder getDirectoryHolder() {
            return _holder;
        }

        public File getFile() {
            return _holder.getDirectory();
        }
    }
    
    private class LibraryFilesMouseObserver implements MouseObserver {
        public void handleMouseClick(MouseEvent e) {
//            DirectoryHolder directoryHolder = getSelectedDirectoryHolder();
//            if (directoryHolder != null && directoryHolder instanceof MediaTypeSavedFilesDirectoryHolder) {
//                LibraryMediator.instance().showView(LibraryMediator.FILES_TABLE_KEY);
//                MediaTypeSavedFilesDirectoryHolder mtsfdh = (MediaTypeSavedFilesDirectoryHolder) directoryHolder;
//                BackgroundExecutorService.schedule(new SearchByMediaTypeRunnable(mtsfdh));
//            }
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
//            int row = getRowForLocation(e.getX(), e.getY());
//            if (row == -1)
//                return;
//
//            setSelectionRow(row);
//            DIRECTORY_POPUP.show(this, e.getX(), e.getY());
        }
    }

    private class LibraryFilesSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            
            LibraryFilesListCell node = (LibraryFilesListCell) _list.getSelectedValue();

            if (node == null)
                return;

            LibraryMediator.instance().updateTableFiles(node.getDirectoryHolder());
            
            DirectoryHolder directoryHolder = getSelectedDirectoryHolder();
            if (directoryHolder != null && directoryHolder instanceof MediaTypeSavedFilesDirectoryHolder) {
                LibraryMediator.instance().showView(LibraryMediator.FILES_TABLE_KEY);
                MediaTypeSavedFilesDirectoryHolder mtsfdh = (MediaTypeSavedFilesDirectoryHolder) directoryHolder;
                BackgroundExecutorService.schedule(new SearchByMediaTypeRunnable(mtsfdh));
            }
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
        }

        private void search(File file, Set<File> ignore) {

            if (!file.isDirectory()) {
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
}
