package com.frostwire.gui.library;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.pushingpixels.substance.api.renderers.SubstanceDefaultListCellRenderer;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.SharingSettings;

public class LibraryFiles extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 9192882931064269836L;

    private LibraryFilesListCell _finishedDownloadsCell;
    private SavedFilesDirectoryHolder _finishedDownloadsHolder;

    private ListSelectionListener _listSelectionListener;

    private DefaultListModel _model;
    private JList _list;
    private JScrollPane _scrollPane;

    public LibraryFiles() {
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

        _finishedDownloadsHolder = new SavedFilesDirectoryHolder(SharingSettings.TORRENT_DATA_DIR_SETTING, I18n.tr("Finished Downloads"));
        _finishedDownloadsCell = new LibraryFilesListCell(_finishedDownloadsHolder);

        //        _model.addElement(I18n.tr("Finished Downloads"));
        //        _model.addElement(I18n.tr("Audio"));
        //        _model.addElement(I18n.tr("Video"));
        //        _model.addElement(I18n.tr("Programs"));
        //        _model.addElement(I18n.tr("Documents"));
        _model.addElement(_finishedDownloadsCell);
    }

    private void setupList() {
        _listSelectionListener = new LibraryFilesSelectionListener();

        _list = new JList(_model);
        _list.setCellRenderer(new LibraryFileCellRenderer());
        _list.addListSelectionListener(_listSelectionListener);
        _list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ToolTipManager.sharedInstance().registerComponent(_list);
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

    private class LibraryFilesSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            LibraryFilesListCell node = (LibraryFilesListCell) _list.getSelectedValue();

            if (node == null)
                return;

            LibraryMediator.instance().updateTableFiles(node.getDirectoryHolder());
        }
    }
}
