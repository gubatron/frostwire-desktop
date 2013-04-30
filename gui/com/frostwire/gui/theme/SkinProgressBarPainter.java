package com.frostwire.gui.theme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;

import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.Painter;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.nimbus.AbstractRegionPainter;

public class SkinProgressBarPainter implements Painter<JProgressBar> {

    private final boolean enabled;
    private final boolean indeterminate;

    public SkinProgressBarPainter(boolean enabled, boolean indeterminate) {
        this.enabled = enabled;
        this.indeterminate = indeterminate;
    }

    @Override
    public void paint(Graphics2D g, JProgressBar object, int width, int height) {
        if (indeterminate) {
            paintIndeterminateBar(g, width, height);
        } else {
            paintBar(g, width, height);
        }
    }

    private void paintIndeterminateBar(Graphics2D g, int width, int height) {
        if (enabled) {
            g.setColor(Color.red);
            //g.setPaint(createVerticalGradient(height, SkinColors.PROGRESS_BAR_ENABLED_INDETERMINATE_COLOR1, SkinColors.PROGRESS_BAR_ENABLED_INDETERMINATE_COLOR2));
        } else {
            g.setPaint(createVerticalGradient(height, SkinColors.PROGRESS_BAR_DISABLED_INDETERMINATE_COLOR1, SkinColors.PROGRESS_BAR_DISABLED_INDETERMINATE_COLOR2));
        }
        g.fillRect(0, 0, width / 2, height);

        if (enabled) {
            g.setColor(Color.gray);
            //g.setPaint(createVerticalGradient(height, SkinColors.PROGRESS_BAR_ENABLED_INDETERMINATE_COLOR4, SkinColors.PROGRESS_BAR_ENABLED_INDETERMINATE_COLOR4));
        } else {
            g.setPaint(createVerticalGradient(height, SkinColors.PROGRESS_BAR_DISABLED_INDETERMINATE_COLOR4, SkinColors.PROGRESS_BAR_DISABLED_INDETERMINATE_COLOR4));
        }
        g.fillRect(width / 2, 0, width / 2, height);
    }

    private void paintBar(Graphics2D g, int width, int height) {
        if (enabled) {
            g.setColor(new ColorUIResource(Color.blue));
            //g.setPaint(createVerticalGradient(height, SkinColors.PROGRESS_BAR_ENABLED_COLOR1, SkinColors.PROGRESS_BAR_ENABLED_INDETERMINATE_COLOR2));
        } else {
            g.setPaint(createVerticalGradient(height, SkinColors.PROGRESS_BAR_DISABLED_COLOR1, SkinColors.PROGRESS_BAR_DISABLED_INDETERMINATE_COLOR2));
        }
        g.fillRect(0, 0, width, height);
    }

    private Paint createVerticalGradient(int hight, Color top, Color bottom) {
        return createGradient(0, 0, 0, hight, new float[] { 0f, 1f }, new Color[] { top, bottom });
    }

    private LinearGradientPaint createGradient(float x1, float y1, float x2, float y2, float[] midpoints, Color[] colors) {
        if (x1 == x2 && y1 == y2) {
            y2 += .00001f;
        }

        return new LinearGradientPaint(x1, y1, x2, y2, midpoints, colors);
    }
}
