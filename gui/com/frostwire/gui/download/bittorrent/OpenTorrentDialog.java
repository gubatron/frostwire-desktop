package com.frostwire.gui.download.bittorrent;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.util.TorrentUtils;

import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.tables.SizeHolder;

public class OpenTorrentDialog extends JDialog {

    private static final long serialVersionUID = 4312306965758592618L;

    private JLabel _label;
    private JTable _table;
    private JScrollPane _scrollPane;
    private JButton _buttonOK;
    private JButton _buttonCancel;

    private final TOTorrent _torrent;
    private final String _name;

    public OpenTorrentDialog(JFrame frame, File torrentFile) throws TOTorrentException {
        super(frame, I18n.tr("Select files to download"));

        _torrent = TorrentUtils.readFromFile(torrentFile, false);
        _name = torrentFile.getName();

        setupUI();
    }

    protected void setupUI() {
        setResizable(false);

        getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints c;

        // title
        _label = new JLabel(_name);
        c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(_label, c);

        // table
        _table = new JTable();
        _table.setPreferredScrollableViewportSize(new Dimension(600, 300));
        _table.setDefaultRenderer(TorrentFileInfo.class, new TorrentFileInfoRenderer());
        _table.setDefaultEditor(TorrentFileInfo.class, new TorrentFileInfoEditor());
        _table.setModel(new TorrentTableModel(_torrent));
        _table.getColumnModel().getColumn(0).setHeaderValue(I18n.tr("File"));
        _table.getColumnModel().getColumn(1).setHeaderValue(I18n.tr("Size"));
        _table.getColumnModel().getColumn(0).setPreferredWidth(500);
        _table.getColumnModel().getColumn(1).setPreferredWidth(60);
        _scrollPane = new JScrollPane(_table);
        _table.setFillsViewportHeight(true);
        c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        getContentPane().add(_scrollPane, c);

        // ok button
        _buttonOK = new JButton(I18n.tr("OK"));
        _buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonOK_actionPerformed(e);
            }
        });
        c = new GridBagConstraints();
        c.insets = new Insets(4, 270, 8, 4);
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.EAST;
        c.ipadx = 40;
        getContentPane().add(_buttonOK, c);

        // cancel button
        _buttonCancel = new JButton(I18n.tr("Cancel"));
        _buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonCancel_actionPerformed(e);
            }
        });
        c = new GridBagConstraints();
        c.insets = new Insets(4, 0, 8, 6);
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.EAST;
        c.ipadx = 18;
        getContentPane().add(_buttonCancel, c);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        getRootPane().setDefaultButton(_buttonOK);
        GUIUtils.addHideAction((JComponent) getContentPane());
    }

    protected void buttonOK_actionPerformed(ActionEvent e) {
        GUIUtils.getDisposeAction().actionPerformed(e);
    }

    protected void buttonCancel_actionPerformed(ActionEvent e) {
        GUIUtils.getDisposeAction().actionPerformed(e);
    }

    private static final class TorrentFileInfo {
        public TOTorrentFile torrentFile;
        public boolean selected;

        public TorrentFileInfo(TOTorrentFile torrentFile, boolean selected) {
            this.torrentFile = torrentFile;
            this.selected = selected;
        }
    }

    private static final class TorrentTableModel extends AbstractTableModel {

        /**
         * 
         */
        private static final long serialVersionUID = -8689494570949104116L;

        private final TOTorrent _torrent;
        private final TorrentFileInfo[] _fileInfos;

        public TorrentTableModel(TOTorrent torrent) {
            _torrent = torrent;
            _fileInfos = new TorrentFileInfo[torrent.getFiles().length];
            for (int i = 0; i < _fileInfos.length; i++) {
                _fileInfos[i] = new TorrentFileInfo(torrent.getFiles()[i], true);
            }
        }

        @Override
        public int getRowCount() {
            return _torrent.getFiles().length;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
            case 0:
                return TorrentFileInfo.class;
            case 1:
                return SizeHolder.class;
            default:
                return null;
            }
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
            case 0:
                return _fileInfos[rowIndex];
            case 1:
                return _fileInfos[rowIndex].torrentFile.getLength();
            default:
                return null;
            }
        }

    }

    private static final class TorrentFileInfoRenderer extends JCheckBox implements TableCellRenderer, UIResource {
        /**
         * 
         */
        private static final long serialVersionUID = 4259030916392976129L;

        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        
        private TorrentFileInfo _torrentFileInfo;

        public TorrentFileInfoRenderer() {
            //setHorizontalAlignment(JLabel.CENTER);
            setBorderPainted(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            _torrentFileInfo = (TorrentFileInfo) value;
            
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            setSelected(_torrentFileInfo.selected);
            setText(_torrentFileInfo.torrentFile.getRelativePath());
            setToolTipText(_torrentFileInfo.torrentFile.getRelativePath());

            if (hasFocus) {
                setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            } else {
                setBorder(noFocusBorder);
            }

            return this;
        }
    }
    
    private static final class TorrentFileInfoEditor extends AbstractCellEditor implements TableCellEditor, UIResource {
        
        /**
         * 
         */
        private static final long serialVersionUID = 6643473692814684405L;

        private JCheckBox _checkBox;
        
        private TorrentFileInfo _torrentFileInfo;

        public TorrentFileInfoEditor() {
            _checkBox = new JCheckBox();
            _checkBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    checkBox_itemStateChanged(e);
                }
            });
            _checkBox.setBorderPainted(true);
        }

        @Override
        public Object getCellEditorValue() {
            return _torrentFileInfo;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            _torrentFileInfo = (TorrentFileInfo) value;
            
            if (isSelected) {
                _checkBox.setForeground(table.getSelectionForeground());
                _checkBox.setBackground(table.getSelectionBackground());
            } else {
                _checkBox.setForeground(table.getForeground());
                _checkBox.setBackground(table.getBackground());
            }
            _checkBox.setSelected(_torrentFileInfo.selected);
            _checkBox.setText(_torrentFileInfo.torrentFile.getRelativePath());
            _checkBox.setToolTipText(_torrentFileInfo.torrentFile.getRelativePath());

            _checkBox.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            
            return _checkBox;
        }
        
        protected void checkBox_itemStateChanged(ItemEvent e) {
            if (_torrentFileInfo != null) {
                _torrentFileInfo.selected = _checkBox.isSelected();
                fireEditingStopped();
            }
        }
    }
}
