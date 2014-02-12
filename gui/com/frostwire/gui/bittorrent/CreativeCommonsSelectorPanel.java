package com.frostwire.gui.bittorrent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.frostwire.gui.theme.ThemeMediator;
import com.frostwire.torrent.CreativeCommonsLicense;
import com.limegroup.gnutella.gui.I18n;

public class CreativeCommonsSelectorPanel extends JPanel {
    public CreativeCommonsSelectorPanel() {
        initBorder();
        add(new JLabel("Creative Commons Options panel"));
    }

    private void initBorder() {
        Border titleBorder = BorderFactory.createTitledBorder(I18n
                .tr("Choose your license"));
        Border lineBorder = BorderFactory.createLineBorder(ThemeMediator.LIGHT_BORDER_COLOR);
        Border border = BorderFactory.createCompoundBorder(lineBorder, titleBorder);
        setBorder(border);
    }

    public boolean hasCreativeCommonsLicense() {
        return false;
    }

    public CreativeCommonsLicense getCreativeCommonsLicense() {
        return null;
    }
}