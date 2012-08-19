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

package com.frostwire.gui.tabs;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.frostwire.gui.mplayer.MPlayerComponent;
import com.frostwire.gui.mplayer.MPlayerComponentFactory;
import com.limegroup.gnutella.gui.I18n;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
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
