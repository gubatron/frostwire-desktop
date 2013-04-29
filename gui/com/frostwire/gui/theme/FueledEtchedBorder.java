package com.frostwire.gui.theme;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.border.Border;

public class FueledEtchedBorder implements Border {

    private final Color color;
    
    private final Color bottomShadeColor;

    public FueledEtchedBorder(Color color) {
        this.color = color;
        this.bottomShadeColor = new Color(255, 255, 255, 150);
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int w = width;
        int h = height;

        Graphics2D g2d = (Graphics2D) g.create();
        //float strokeWidth = SubstanceSizeUtils.getBorderStrokeWidth(SubstanceSizeUtils.getComponentFontSize(c));
        //g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(color);
        //g2d.drawRect(x, y, w - 1, h - 1);
        g2d.drawRoundRect(x, y, w - 1, h - 2, 16, 16);
        g2d.setColor(bottomShadeColor);
        
        g2d.drawLine(x + 7, h - 1, w - 7, h - 1);
        /*
        g2d.translate(x, y);

        //g2d.setColor(getShadowColor(c));

        // this is to prevent clipping of thick outer borders.
        int delta = (int) Math.floor(strokeWidth / 2.0);

        //g2d.draw(new Rectangle2D.Float(delta, delta, w - delta - 2 * strokeWidth, h - delta - 2 * strokeWidth));
        // g2d.drawRect(0, 0, w - 2, h - 2);

        g2d.setColor(color);
        g2d.draw(new Line2D.Float(strokeWidth, h - 3 * strokeWidth, strokeWidth, strokeWidth));
        // g2d.drawLine(1, h - 3, 1, 1);
        g2d.draw(new Line2D.Float(delta + strokeWidth, delta + strokeWidth, w - delta - 3 * strokeWidth, delta + strokeWidth));
        // g2d.drawLine(1, 1, w - 3, 1);

        g2d.draw(new Line2D.Float(delta, h - delta - strokeWidth, w - delta - strokeWidth, h - delta - strokeWidth));
        // g2d.drawLine(0, h - 1, w - 1, h - 1);
        g2d.draw(new Line2D.Float(w - delta - strokeWidth, h - delta - strokeWidth, w - delta - strokeWidth, delta));
        // g2d.drawLine(w - 1, h - 1, w - 1, 0);
*/
        g2d.dispose();

        // this is a fix for defect 248 - in order to paint the TitledBorder
        // text respecting the AA settings of the display, we have to
        // set rendering hints on the passed Graphics object.
        ///RenderingUtils.installDesktopHints((Graphics2D) g, c);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        float borderStrokeWidth = 2;//SubstanceSizeUtils.getBorderStrokeWidth(SubstanceSizeUtils.getComponentFontSize(c));
        int prefSize = (int) (Math.ceil(2.0 * borderStrokeWidth));
        return new Insets(prefSize, prefSize, prefSize, prefSize);
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }
}
