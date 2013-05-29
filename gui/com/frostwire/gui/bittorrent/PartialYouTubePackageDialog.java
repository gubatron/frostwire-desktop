/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.gui.bittorrent;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.limewire.util.FilenameUtils;

import com.frostwire.gui.bittorrent.PartialFilesDialog.RowFilterExtension;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.IconManager;
import com.limegroup.gnutella.gui.LabeledTextField;
import com.limegroup.gnutella.gui.search.NamedMediaType;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class PartialYouTubePackageDialog extends JDialog {

    /** First time this component has been painted */
    private boolean tablePainted;

    private static final long serialVersionUID = -7028188722959174740L;

    private LabeledTextField _filter;
    private RowFilter<Object, Object> textBasedFilter;

    private JPanel panel;
    private JLabel _label;
    private JTable _table;
    private JScrollPane _scrollPane;
    private JButton _buttonOK;
    private JButton _buttonCancel;

    private String name;

    private final FilePackageTableModel _model;

    private List<FilePackage> _filesSelection;
    private JCheckBox _checkBoxToggleAll;

    public PartialYouTubePackageDialog(JFrame frame, String name, List<FilePackage> filePackages) throws TOTorrentException {
        super(frame, I18n.tr("Select files to download"));

        this.name = name;

        _model = new FilePackageTableModel(filePackages);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                GUIMediator.instance().setRemoteDownloadsAllowed(false);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                GUIMediator.instance().setRemoteDownloadsAllowed(true);
            }
        });

        setupUI();
        setLocationRelativeTo(frame);
    }

    protected void setupUI() {
        setResizable(true);
        setMinimumSize(new Dimension(400, 300));
        panel = new JPanel(new GridBagLayout());

        // title
        setupTitle();

        // filter
        setupTextFilter();

        setupToggleAllSelectionCheckbox();

        // table
        setupTable();

        // ok button
        setupOkButton();

        // cancel button
        setupCancelButton();

        getContentPane().add(panel);

        pack();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        getRootPane().setDefaultButton(_buttonOK);
        GUIUtils.addHideAction((JComponent) getContentPane());
    }

    private void setupCancelButton() {
        GridBagConstraints c;
        _buttonCancel = new JButton(I18n.tr("Cancel"));
        _buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonCancel_actionPerformed(e);
            }
        });
        c = new GridBagConstraints();
        c.insets = new Insets(4, 0, 8, 6);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.EAST;
        c.ipadx = 18;
        c.gridy = 4;
        panel.add(_buttonCancel, c);
    }

    private void setupOkButton() {
        GridBagConstraints c;
        _buttonOK = new JButton(I18n.tr("Download Selected Files Only"));
        _buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonOK_actionPerformed(e);
            }
        });

        c = new GridBagConstraints();
        c.insets = new Insets(4, 100, 8, 4);
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.EAST;
        c.ipadx = 20;
        c.weightx = 1.0;
        c.gridy = 4;

        panel.add(_buttonOK, c);
    }

    private void setupTable() {
        GridBagConstraints c;
        _table = new JTable() {
            private static final long serialVersionUID = 6757772984341572968L;

            public void paint(java.awt.Graphics g) {

                super.paint(g);

                try {
                    if (tablePainted) {
                        return;
                    }
                    tablePainted = true;

                    GUIUtils.adjustColumnWidth(_model, 2, 620, 10, this);
                    GUIUtils.adjustColumnWidth(_model, 3, 150, 10, this);
                } catch (Exception e) {
                    tablePainted = false;
                }

            };
        };

        _table.setPreferredScrollableViewportSize(new Dimension(600, 300));
        _table.setModel(_model);
        _table.getColumnModel().getColumn(0).setHeaderValue(""); //checkbox
        _table.getColumnModel().getColumn(1).setHeaderValue(""); //icon
        _table.getColumnModel().getColumn(2).setHeaderValue(I18n.tr("File"));
        _table.getColumnModel().getColumn(3).setHeaderValue(I18n.tr("Type"));
        _table.getColumnModel().getColumn(4).setHeaderValue(I18n.tr("Extension"));

        _table.getColumnModel().getColumn(0).setPreferredWidth(30);//checkbox
        _table.getColumnModel().getColumn(1).setPreferredWidth(30);//icon
        _table.getColumnModel().getColumn(2).setPreferredWidth(620);
        _table.getColumnModel().getColumn(3).setPreferredWidth(150);
        _table.getColumnModel().getColumn(4).setPreferredWidth(60);

        _scrollPane = new JScrollPane(_table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        _table.setFillsViewportHeight(true);
        _table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        panel.add(_scrollPane, c);
    }

    private void setupToggleAllSelectionCheckbox() {
        GridBagConstraints c;
        _checkBoxToggleAll = new JCheckBox(I18n.tr("Select/Unselect all files"), true);
        _checkBoxToggleAll.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                onCheckBoxToggleAll(e);
            }
        });

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(5, 5, 5, 5);
        panel.add(_checkBoxToggleAll, c);
    }

    private void setupTextFilter() {
        GridBagConstraints c;
        _filter = new LabeledTextField("Filter files", 30);
        _filter.setMinimumSize(_filter.getPreferredSize()); // fix odd behavior
        textBasedFilter = new RowFilterExtension(_filter, 2);

        _filter.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (_filter.getText() == null || _filter.getText().equals("")) {
                    _table.setRowSorter(null);
                    return;
                }

                _checkBoxToggleAll.setSelected(false);

                TableRowSorter<FilePackageTableModel> sorter = new TableRowSorter<FilePackageTableModel>(_model);
                sorter.setRowFilter(textBasedFilter);
                _table.setRowSorter(sorter);
            }

        });

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.insets = new Insets(5, 5, 5, 5);
        panel.add(_filter, c);
    }

    private void setupTitle() {
        GridBagConstraints c;
        _label = new JLabel(name.replace("_", " ").replace(".torrent", "").replace("&quot;", "\""));
        _label.setFont(new Font("Dialog", Font.BOLD, 18));
        _label.setHorizontalAlignment(SwingConstants.LEFT);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(5, 5, 5, 5);
        panel.add(_label, c);
    }

    protected void onCheckBoxToggleAll(ItemEvent e) {
        _model.setAllSelected(_checkBoxToggleAll.isSelected());
        _buttonOK.setEnabled(_checkBoxToggleAll.isSelected());
    }

    /**
     * Change the value of the checkbox but don't trigger any events.
     * (We probably need something generic for this, this pattern keeps appearing all over)
     * @param allSelected
     */
    public void checkboxToggleAllSetSelectedNoTrigger(boolean allSelected) {
        ItemListener[] itemListeners = _checkBoxToggleAll.getItemListeners();

        for (ItemListener l : itemListeners) {
            _checkBoxToggleAll.removeItemListener(l);
        }
        _checkBoxToggleAll.setSelected(allSelected);

        for (ItemListener l : itemListeners) {
            _checkBoxToggleAll.addItemListener(l);
        }

    }

    protected void buttonOK_actionPerformed(ActionEvent e) {

        FilePackageInfo[] fileInfos = _model.getFileInfos();
        _filesSelection = new ArrayList<FilePackage>();
        for (int i = 0; i < fileInfos.length; i++) {
            if (fileInfos[i].selected) {
                _filesSelection.add(fileInfos[i].file);
            }
        }

        GUIUtils.getDisposeAction().actionPerformed(e);
    }

    protected void buttonCancel_actionPerformed(ActionEvent e) {
        GUIUtils.getDisposeAction().actionPerformed(e);
    }

    public List<FilePackage> getFilesSelection() {
        return _filesSelection;
    }

    public class FilePackageInfo {
        public FilePackage file;
        public boolean selected;

        public FilePackageInfo(FilePackage file, boolean selected) {
            this.file = file;
            this.selected = selected;
        }
    }

    private final class FilePackageTableModel extends AbstractTableModel {

        private static final long serialVersionUID = -8689494570949104117L;

        private final FilePackageInfo[] _fileInfos;

        public FilePackageTableModel(List<FilePackage> filePackages) {
            _fileInfos = new FilePackageInfo[filePackages.size()];
            for (int i = 0; i < _fileInfos.length; i++) {
                _fileInfos[i] = new FilePackageInfo(filePackages.get(i), true);
            }
        }

        @Override
        public int getRowCount() {
            return _fileInfos.length;
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
            case 0:
                //checkbox
                return Boolean.class;
            case 1:
                //icon
                return Icon.class;
            case 2:
                //path
                return String.class;
            case 3:
                //type
                return String.class;
            case 4:
                //extension
                return String.class;
            default:
                return null;
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            String fileName = readFilename(_fileInfos[rowIndex].file);
            String extension = FilenameUtils.getExtension(fileName);
            switch (columnIndex) {
            case 0:

                //checkbox
                return _fileInfos[rowIndex].selected;
            case 1:
                //icon
                return IconManager.instance().getIconForExtension(extension);
            case 2:
                //file name
                return fileName;
            case 3:
                //(human readable type)
                return guessHumanType(fileName, extension);
            case 4:
                //extension
                return extension;
            default:
                return null;
            }
        }

        private String readFilename(FilePackage filePackage) {
            DownloadLink dl = filePackage.getChildren().get(0);
            if (dl.getStringProperty("convertto", "").equals("AUDIOMP3")) {
                name = FilenameUtils.getBaseName(dl.getName());
                _label.setText(name);
                return FilenameUtils.getBaseName(dl.getName()) + ".mp3";
            }

            return dl.getName();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                _fileInfos[rowIndex].selected = (Boolean) aValue;
                fireTableDataChanged();
            }

            checkboxToggleAllSetSelectedNoTrigger(isAllSelected());
            _buttonOK.setEnabled(!isNoneSelected());
        }

        public void setAllSelected(boolean selected) {
            for (int i = 0; i < _fileInfos.length; i++) {
                _fileInfos[i].selected = selected;
            }
            fireTableDataChanged();
        }

        public boolean isAllSelected() {
            for (int i = 0; i < _fileInfos.length; i++) {
                if (!_fileInfos[i].selected) {
                    return false;
                }
            }
            return true;
        }

        public boolean isNoneSelected() {
            for (int i = 0; i < _fileInfos.length; i++) {
                if (_fileInfos[i].selected) {
                    return false;
                }
            }
            return true;
        }

        public FilePackageInfo[] getFileInfos() {
            return _fileInfos;
        }

        public String guessHumanType(String filename, String extension) {
            try {
                // first if it's a video or audio
                StringBuilder humanType = new StringBuilder();

                MediaType mediaType = NamedMediaType.getFromExtension(extension).getMediaType();

                if (mediaType.equals(MediaType.getAudioMediaType())) {
                    humanType.append(I18n.tr("Audio"));
                } else if (mediaType.equals(MediaType.getVideoMediaType())) {
                    humanType.append(I18n.tr("Video"));
                }

                humanType.append(" ");

                // now the quality
                if (filename.contains("High Quality") || filename.contains("(720p_") || filename.contains("(1080p_")) {
                    humanType.append(I18n.tr("(High Quality)"));
                } else if (filename.contains("(AAC)") || filename.contains("(480p_")) {
                    humanType.append(I18n.tr("(Medium Quality"));
                } else {
                    humanType.append(I18n.tr("(Poor Quality)"));
                }

                return humanType.toString();
            } catch (Throwable t) {
                return I18n.tr("Unknown");
            }
        }
    }

}
