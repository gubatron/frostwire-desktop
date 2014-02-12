package com.frostwire.gui.bittorrent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.frostwire.gui.theme.ThemeMediator;
import com.frostwire.torrent.PaymentOptions;
import com.limegroup.gnutella.gui.I18n;

public class PaymentOptionsPanel extends JPanel {

    //"bitcoin :14F6JPXK2fR5b4gZp3134qLRGgYtvabMWL", 
    //"litecoin:LiYp3Dg11N5BgV8qKW42ubSZXFmjDByjoV", 

    public PaymentOptionsPanel() {
        initBorder();
        add(new JLabel("Payment Options panel"));
    }

    private void initBorder() {
        Border titleBorder = BorderFactory.createTitledBorder(I18n
                .tr("Name your price"));
        Border lineBorder = BorderFactory.createLineBorder(ThemeMediator.LIGHT_BORDER_COLOR);
        Border border = BorderFactory.createCompoundBorder(lineBorder, titleBorder);
        setBorder(border);
    }

    public PaymentOptions getPaymentOptions() {
        return null;
    }

    public boolean hasPaymentOptions() {
        return false;
    }

}
