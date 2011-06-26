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
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.util.TorrentUtils;

import sun.swing.table.DefaultTableCellHeaderRenderer;

import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledTextField;
import com.limegroup.gnutella.gui.tables.SizeHolder;

public class PartialFilesDialog extends JDialog {

    private static final long serialVersionUID = 4312306965758592618L;

    private LabeledTextField _filter;
    
    private JLabel _label;
    private JTable _table;
    private JScrollPane _scrollPane;
    private JButton _buttonOK;
    private JButton _buttonCancel;

    private final TOTorrent _torrent;
    private final String _name;
    private final TorrentTableModel _model;
    
    private boolean[] _filesSelection;

	private JCheckBox _checkBoxToggleAll;

    public PartialFilesDialog(JFrame frame, File torrentFile) throws TOTorrentException {
        super(frame, I18n.tr("Select files to download"));

        _torrent = TorrentUtils.readFromFile(torrentFile, false);
        _name = torrentFile.getName();
        _model = new TorrentTableModel(_torrent);

        setupUI();
        setLocationRelativeTo(frame);
    }

    protected void setupUI() {
        setResizable(false);

        getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints c;

        // title
        _label = new JLabel(_name.replace("_", " ").replace(".torrent", "").replace("&quot;", "\""));
        _label.setFont(new Font("Dialog",Font.BOLD,18));
        c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(_label, c);
        
        // filter
        _filter = new LabeledTextField("Filter files", 30);
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(5,5,5,5);
        getContentPane().add(_filter, c);

        _checkBoxToggleAll = new JCheckBox(I18n.tr("Select/Unselect all files"),true);
        _checkBoxToggleAll.addItemListener(new ItemListener() {			
			@Override
			public void itemStateChanged(ItemEvent e) {
				onCheckBoxToggleAll(e);
			}
		});

        c = new GridBagConstraints();
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(5,5,5,5);
        getContentPane().add(_checkBoxToggleAll,c);        

        // table
        _table = new JTable();
        _table.setPreferredScrollableViewportSize(new Dimension(600, 300));
        _table.setModel(_model);
        _table.getColumnModel().getColumn(0).setHeaderValue("");
        DefaultTableCellHeaderRenderer defaultTableCellHeaderRenderer = new DefaultTableCellHeaderRenderer();
        defaultTableCellHeaderRenderer.setText("");

        _table.getColumnModel().getColumn(0).setHeaderRenderer(defaultTableCellHeaderRenderer);
        _table.getColumnModel().getColumn(1).setHeaderValue(I18n.tr("File"));
        _table.getColumnModel().getColumn(2).setHeaderValue(I18n.tr("Size"));
        _table.getColumnModel().getColumn(0).setPreferredWidth(10);
        _table.getColumnModel().getColumn(1).setPreferredWidth(500);
        _table.getColumnModel().getColumn(2).setPreferredWidth(60);

        _scrollPane = new JScrollPane(_table);
        _table.setFillsViewportHeight(true);
        c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        getContentPane().add(_scrollPane, c);

        // ok button
        _buttonOK = new JButton(I18n.tr("Download Selected Files Only"));
        _buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonOK_actionPerformed(e);
            }
        });
        c = new GridBagConstraints();
        c.insets = new Insets(4, 430, 8, 4);
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.EAST;
        c.ipadx = 20;
        c.weightx=1.0;
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
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.EAST;
        c.ipadx = 18;
        getContentPane().add(_buttonCancel, c);

        pack();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        getRootPane().setDefaultButton(_buttonOK);
        GUIUtils.addHideAction((JComponent) getContentPane());
    }

    protected void onCheckBoxToggleAll(ItemEvent e) {
    	_model.setAllSelected(_checkBoxToggleAll.isSelected());
    	_buttonOK.setEnabled(_checkBoxToggleAll.isSelected());
	}

    protected void buttonOK_actionPerformed(ActionEvent e) {
        
        TorrentFileInfo[] fileInfos = _model.getFileInfos();
        _filesSelection = new boolean[fileInfos.length];
        for (int i = 0; i < _filesSelection.length; i++) {
            _filesSelection[i] = fileInfos[i].selected;
        }
        
        GUIUtils.getDisposeAction().actionPerformed(e);
    }

    protected void buttonCancel_actionPerformed(ActionEvent e) {
        GUIUtils.getDisposeAction().actionPerformed(e);
    }

    public boolean[] getFilesSelection() {
        return _filesSelection;
    }

    private final class TorrentTableModel extends AbstractTableModel {

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
            return 3;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
            case 0:
                return Boolean.class;
            case 1:
                return String.class;
            case 2:
                return SizeHolder.class;
            default:
                return null;
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
            case 0:
                return _fileInfos[rowIndex].selected;
            case 1:
                return _fileInfos[rowIndex].torrentFile.getRelativePath();
            case 2:
                return new SizeHolder(_fileInfos[rowIndex].torrentFile.getLength());
            default:
                return null;
            }
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
        
        public TorrentFileInfo[] getFileInfos() {
            return _fileInfos;
        }
    }

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
}
