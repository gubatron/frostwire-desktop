package com.frostwire.gui.tabs;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.frostwire.gui.mplayer.MPlayerComponent;
import com.frostwire.gui.mplayer.MPlayerComponentFactory;
import com.limegroup.gnutella.gui.I18n;

public final class MediaPlayerTab extends AbstractTab {

    private static JPanel PANEL = new JPanel(new BorderLayout());

    private final MPlayerComponent mplayerComponent;

    public MediaPlayerTab() {
        super(I18n.tr("Player"), I18n.tr("Media Player"), "chat_tab");

        mplayerComponent = MPlayerComponentFactory.instance().createPlayerComponent();
        PANEL.add(mplayerComponent.getComponent());
    }

    public JComponent getComponent() {
        return PANEL;
    }
}
