package com.frostwire.gnutella.gui.android;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class NoDevicePanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -3925479179412157760L;

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        graphics.setColor(new Color(0x2c7fb0));
        graphics.fillRect(0, 0, getWidth(), getHeight());

        //paint green concentric circles of the radar
        int circles = 16;
        int initialRadio = 1;

        int smallerLength = (getWidth() > getHeight()) ? getHeight() : getWidth();

        int radioStep = smallerLength / (circles >> 2);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        graphics.setColor(new Color(0x7adafa));
        for (int i = 0; i < circles; i++) {
            graphics.drawOval(centerX, centerY, initialRadio, initialRadio);
            graphics.drawOval(centerX, centerY, initialRadio + 1, initialRadio + 1);
            graphics.drawOval(centerX, centerY, initialRadio - 1, initialRadio - 1);
            graphics.drawOval(centerX, centerY, initialRadio - 1, initialRadio + 1);
            graphics.drawOval(centerX, centerY, initialRadio + 1, initialRadio - 1);
            initialRadio += radioStep;
            centerX -= radioStep / 2;
            centerY -= radioStep / 2;
        }
    }
}
