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
import java.awt.Shape;

import javax.swing.JComponent;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class SkinScrollBarButtonPainter extends AbstractSkinPainter {

    private final State state;

    public SkinScrollBarButtonPainter(State state) {
        this.state = state;
    }

    @Override
    protected void doPaint(Graphics2D g, JComponent c, int width, int height, Object[] extendedCacheKeys) {
        paintIncreaseButtonApart(g, width, height);
    }

    private void paintIncreaseButtonApart(Graphics2D g, int width, int height) {
        paintArrowButton(g, width / 2.0 - 5, height / 2.0 - 2);
    }

    private void paintArrowButton(Graphics2D g, double x, double y) {
        Shape s = shapeGenerator.createArrowLeft(x, y, 4, 6);

        g.setPaint(getScrollBarButtonArrowColor());
        g.fill(s);
    }

    private Color getScrollBarButtonArrowColor() {
        switch (state) {
        case Disabled:
            return SkinColors.SCROLL_BUTTON_ARROW_DISABLED_COLOR;
        case Enabled:
            return SkinColors.SCROLL_BUTTON_ARROW_ENABLED_COLOR;
        case MouseOver:
            return SkinColors.SCROLL_BUTTON_ARROW_MOUSEOVER_COLOR;
        case Pressed:
            return SkinColors.SCROLL_BUTTON_ARROW_PRESSED_COLOR;
        default:
            throw new IllegalArgumentException("Not supported state");
        }
    }

    public static enum State {
        Disabled, Enabled, MouseOver, Pressed
    }
}
