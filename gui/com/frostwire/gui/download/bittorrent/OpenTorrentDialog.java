package com.frostwire.gui.download.bittorrent;

import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTable;

import com.limegroup.gnutella.gui.I18n;

import java.awt.Insets;

public class OpenTorrentDialog extends JDialog {
    
    private static final long serialVersionUID = 4312306965758592618L;
    
    private JLabel _label;
    private JTable _table;
    
    public OpenTorrentDialog() {
        setupUI();
    }
    
    protected void setupUI() {
        getContentPane().setLayout(new GridBagLayout());
        
        GridBagConstraints gbc;
        
        _label = new JLabel(I18n.tr("Torrent name...."));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        getContentPane().add(_label, gbc);
        
        _table = new JTable();
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        getContentPane().add(_table, gbc);
    }
}
