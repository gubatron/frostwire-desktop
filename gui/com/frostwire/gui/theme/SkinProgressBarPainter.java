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
import java.awt.Paint;
import java.awt.Shape;

import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class SkinProgressBarPainter extends AbstractSkinPainter {

    private final boolean enabled;
    private final boolean indeterminate;
    private final int padding;
    private final int paddingTwice;

    public SkinProgressBarPainter(boolean enabled, boolean indeterminate) {
        this.enabled = enabled;
        this.indeterminate = indeterminate;

        if (enabled) {
            this.padding = (Integer) UIManager.get("ProgressBar[Enabled+Indeterminate].progressPadding");
        } else {
            this.padding = (Integer) UIManager.get("ProgressBar[Disabled+Indeterminate].progressPadding");
        }

        this.paddingTwice = padding * 2;
    }

    @Override
    protected void doPaint(Graphics2D g, JComponent c, int width, int height, Object[] extendedCacheKeys) {
        if (indeterminate) {
            paintIndeterminateBar(g, width, height);
        } else {
            paintBar(g, c, width, height);
        }
    }

    private void paintBar(Graphics2D g, JComponent c, int width, int height) {
        Shape s = shapeGenerator.createRectangle(padding, padding, width - paddingTwice, height - paddingTwice);
        g.setPaint(getProgressBarPaint(s));
        g.fill(s);
    }

    private void paintIndeterminateBar(Graphics2D g, int width, int height) {
        Shape s = shapeGenerator.createProgressBarIndeterminatePattern(0, 0, width, height);
        g.setPaint(getProgressBarIndeterminatePaint(s));
        g.fill(s);
    }

    private Paint getProgressBarPaint(Shape s) {
        Color[] colors = enabled ? SkinColors.PROGRESS_BAR_ENABLED_GRADIENT_COLORS : SkinColors.PROGRESS_BAR_DISABLED_GRADIENT_COLORS;
        return createVerticalGradient(s, colors);
    }

    private Paint getProgressBarIndeterminatePaint(Shape s) {
        Color[] colors = enabled ? SkinColors.PROGRESS_BAR_ENABLED_INDERTERMINATE_GRADIENT_COLORS : SkinColors.PROGRESS_BAR_DISABLED_INDERTERMINATE_GRADIENT_COLORS;
        return createVerticalGradient(s, colors);
    }
}
