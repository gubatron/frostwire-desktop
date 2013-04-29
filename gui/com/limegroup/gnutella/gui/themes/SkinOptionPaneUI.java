/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
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

package com.limegroup.gnutella.gui.themes;

import java.awt.Container;
import java.awt.GridBagConstraints;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicOptionPaneUI;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class SkinOptionPaneUI extends BasicOptionPaneUI {

    public static ComponentUI createUI(JComponent comp) {
        return ThemeMediator.CURRENT_THEME.createOptionPaneUI(comp);
    }

    @Override
    protected void addMessageComponents(Container container, GridBagConstraints cons, Object msg, int maxll, boolean internallyCreated) {
        super.addMessageComponents(container, cons, msg, maxll, internallyCreated);

        if (msg instanceof JLabel) {
            ThemeMediator.fixComponentFont((JLabel) msg, getMessage());
        }
    }
}
