package com.limegroup.gnutella.gui.themes.fueled;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

import javax.swing.JComponent;

import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.watermark.SubstanceWatermark;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;

import com.limegroup.gnutella.gui.themes.ThemeMediator;

public class FueledSkinWatermark implements SubstanceWatermark {

    /**
     * Watermark image (screen-sized).
     */
    private Image watermarkDarkImage = null;
    private Image watermarkLightImage = null;

    private int amount = 8;
    private float density = 0.5f;
    private Random randomNumbers = new Random();

    public FueledSkinWatermark() {
    }

    @Override
    public String getDisplayName() {
        return "FueledSkinWatermark";
    }

    @Override
    public void drawWatermarkImage(Graphics graphics, Component c, int x, int y, int width, int height) {
        if (!c.isShowing()) {
            return;
        }

        boolean darkNoise = false;
        boolean lightNoise = false;

        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            darkNoise = hasClientProperty(jc, FueledCustomColors.CLIENT_PROPERTY_DARK_NOISE);
            lightNoise = hasClientProperty(jc, FueledCustomColors.CLIENT_PROPERTY_LIGHT_NOISE);
            if (!darkNoise && !lightNoise) {
                return;
            }
        }

        int dx = c.getLocationOnScreen().x;
        int dy = c.getLocationOnScreen().y;

        if (darkNoise) {
            graphics.drawImage(this.watermarkDarkImage, x, y, x + width, y + height, x + dx, y + dy, x + dx + width, y + dy + height, null);
        } else if (lightNoise) {
            graphics.drawImage(this.watermarkLightImage, x, y, x + width, y + height, x + dx, y + dy, x + dx + width, y + dy + height, null);
        }
    }

    @Override
    public boolean updateWatermarkImage(SubstanceSkin skin) {
        Rectangle virtualBounds = new Rectangle();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gds = ge.getScreenDevices();
        for (GraphicsDevice gd : gds) {
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            virtualBounds = virtualBounds.union(gc.getBounds());
        }

        int screenWidth = virtualBounds.width;
        int screenHeight = virtualBounds.height;
        this.watermarkDarkImage = SubstanceCoreUtilities.getBlankImage(screenWidth, screenHeight);

        Graphics2D graphics = (Graphics2D) this.watermarkDarkImage.getGraphics().create();

        boolean status = this.drawWatermarkImage(skin, graphics, 0, 0, screenWidth, screenHeight, false, ThemeMediator.CURRENT_THEME.getCustomColors().getDarkNoiseColor());
        graphics.dispose();

        this.watermarkLightImage = SubstanceCoreUtilities.getBlankImage(screenWidth, screenHeight);

        graphics = (Graphics2D) this.watermarkLightImage.getGraphics().create();

        status = status & this.drawWatermarkImage(skin, graphics, 0, 0, screenWidth, screenHeight, false, ThemeMediator.CURRENT_THEME.getCustomColors().getLightNoiseColor());
        graphics.dispose();
        return status;
    }

    @Override
    public void previewWatermark(Graphics g, SubstanceSkin skin, int x, int y, int width, int height) {
        //this.drawWatermarkImage(skin, (Graphics2D) g, x, y, width, height, true);
    }

    @Override
    public void dispose() {
        this.watermarkDarkImage = null;
        this.watermarkLightImage = null;
    }

    /**
     * Draws the specified portion of the watermark image.
     * 
     * @param skin
     *            Skin to use for painting the watermark.
     * @param graphics
     *            Graphic context.
     * @param x
     *            the <i>x</i> coordinate of the watermark to be drawn.
     * @param y
     *            The <i>y</i> coordinate of the watermark to be drawn.
     * @param width
     *            The width of the watermark to be drawn.
     * @param height
     *            The height of the watermark to be drawn.
     * @param isPreview
     *            Indication whether the result is a preview image.
     * @return Indication whether the draw succeeded.
     */
    private boolean drawWatermarkImage(SubstanceSkin skin, Graphics2D graphics, int x, int y, int width, int height, boolean isPreview, Color color) {
        SubstanceColorScheme scheme = skin.getWatermarkColorScheme();
        if (isPreview) {
            graphics.drawImage(getNoiseImage(skin, width, height, true, color), x, y, null);
        } else {
            int alpha = scheme.isDark() ? 200 : 140;
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255.0f));
            graphics.drawImage(getNoiseImage(skin, width, height, false, color), x, y, null);
        }
        return true;
    }

    public BufferedImage getNoiseImage(SubstanceSkin skin, int width, int height, boolean isPreview, Color color) {
        BufferedImage dst = SubstanceCoreUtilities.getBlankImage(width, height);

        int[] dstBuffer = ((DataBufferInt) dst.getRaster().getDataBuffer()).getData();
        int pos = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                dstBuffer[pos] = filterRGB(i, j, color.getRGB());
                pos++;
            }
        }

        return dst;
    }

    private int filterRGB(int x, int y, int rgb) {
        if (randomNumbers.nextFloat() <= density) {
            int a = rgb & 0xff000000;
            int r = (rgb >> 16) & 0xff;
            int g = (rgb >> 8) & 0xff;
            int b = rgb & 0xff;

            int n = (int) (randomNumbers.nextGaussian() * amount);
            r = clip(r + n);
            g = clip(g + n);
            b = clip(b + n);

            return a | (r << 16) | (g << 8) | b;
        }
        return rgb;
    }

    private static int clip(int c) {
        if (c < 0)
            return 0;
        if (c > 255)
            return 255;
        return c;
    }

    private boolean hasClientProperty(JComponent c, String propertyKey) {
        Boolean b = (Boolean) c.getClientProperty(propertyKey);
        if (b != null) {
            return b.booleanValue();
        } else if (c.getParent() instanceof JComponent) {
            return hasClientProperty((JComponent) c.getParent(), propertyKey);
        } else {
            return false;
        }
    }
}
