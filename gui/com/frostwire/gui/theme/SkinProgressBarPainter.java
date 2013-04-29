package com.frostwire.gui.theme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;

import javax.swing.JComponent;
import javax.swing.plaf.nimbus.AbstractRegionPainter;

public class SkinProgressBarPainter extends AbstractRegionPainter {

    private final boolean enabled;
    private final boolean indeterminate;

    public SkinProgressBarPainter(boolean enabled, boolean indeterminate) {
        this.enabled = enabled;
        this.indeterminate = indeterminate;
    }

    @Override
    protected PaintContext getPaintContext() {
        return null;
    }

    @Override
    protected void doPaint(Graphics2D g, JComponent c, int width, int height, Object[] extendedCacheKeys) {
        if (indeterminate) {
            paintIndeterminateBar(g, width, height);
        }
    }

    private void paintIndeterminateBar(Graphics2D g, int width, int height) {
        g.setPaint(createVerticalGradient(height, Color.WHITE, Color.BLUE));
        g.fillRect(0, 0, width / 2, height);
        g.setPaint(Color.GRAY);
        g.fillRect(width / 2, 0, width / 2, height);
    }

    protected Paint createVerticalGradient(int hight, Color top, Color bottom) {

        return createGradient(0, 0, 0, hight, new float[] { 0f, 1f }, new Color[] { top, bottom });
    }
    
    protected final LinearGradientPaint createGradient(float x1, float y1, float x2, float y2, float[] midpoints, Color[] colors) {
        if (x1 == x2 && y1 == y2) {
            y2 += .00001f;
        }

        return new LinearGradientPaint(x1, y1, x2, y2, midpoints, colors);
    }
}
