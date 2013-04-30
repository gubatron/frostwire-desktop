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

package com.frostwire.gui.theme;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.JComponent;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class SkinScrollBarTrackPainter extends AbstractSkinPainter {

    private final State state;

    public SkinScrollBarTrackPainter(State state) {
        this.state = state;
    }

    @Override
    protected void doPaint(Graphics2D g, JComponent c, int width, int height, Object[] extendedCacheKeys) {
        g.setColor(getScrollBarButtonArrowColor());
        g.fillRect(0, 0, width, height);
    }

    private Color getScrollBarButtonArrowColor() {
        switch (state) {
        case Disabled:
            return SkinColors.SCROLL_TRACK_DISABLED_COLOR;
        case Enabled:
            return SkinColors.SCROLL_TRACK_ENABLED_COLOR;
        default:
            throw new IllegalArgumentException("Not supported state");
        }
    }

    public static enum State {
        Disabled, Enabled
    }
}
