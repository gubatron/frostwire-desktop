package com.frostwire.gui.download.bittorrent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.table.AbstractTableModel;
import javax.swing.JTable;

import com.limegroup.gnutella.gui.I18n;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OpenTorrentDialog extends JDialog {
    
    private static final long serialVersionUID = 4312306965758592618L;
    
    private JLabel _label;
    private JTable _table;
    private JScrollPane _scrollPane;
    private JButton _buttonOK;
    private JButton _buttonCancel;
    
    public OpenTorrentDialog(JFrame frame) {
        super(frame, I18n.tr("Select files to download"));
        setupUI();
    }
    
    protected void setupUI() {
        setSize(400, 400);
        setResizable(false);
        
        getContentPane().setLayout(new GridBagLayout());
        
        GridBagConstraints c;
        
        // title
        _label = new JLabel(I18n.tr("Torrent name...."));
        c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(_label, c);
        
        // table
        _table = new JTable(new FilesTableMode());
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
        c.insets = new Insets(4, 250, 8, 4);
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
        c.insets = new Insets(4, 4, 8, 6);
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.EAST;
        c.ipadx = 20;
        getContentPane().add(_buttonCancel, c);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    protected void buttonOK_actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    protected void buttonCancel_actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        
    }

    private final class FilesTableMode  extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return 2;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return "Hello";
        }
        
    }
    
    public static void main(String[] args) {
        OpenTorrentDialog dlg = new OpenTorrentDialog(null);
        dlg.setVisible(true);
    }
}
