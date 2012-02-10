package com.limegroup.gnutella.gui.themes.fueled;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.border.Border;

import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.watermark.SubstanceWatermark;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;

import com.limegroup.gnutella.gui.themes.ThemeMediator;

public class FueledSkinWatermark implements SubstanceWatermark {

    private static final int WATERMARK_WIDTH = 100;
    private static final int WATERMARK_HEIGHT = 100;

    private Image watermarkDarkDarkImage = null;
    private Image watermarkDarkImage = null;
    private Image watermarkLightImage = null;

    private int amount = 5;
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

        int darkDarkNoise = Integer.MAX_VALUE;
        int darkNoise = Integer.MAX_VALUE;
        int lightNoise = Integer.MAX_VALUE;

        Border border = null;

        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            darkDarkNoise = hasClientProperty(jc, FueledCustomUI.CLIENT_PROPERTY_DARK_DARK_NOISE, 0);
            darkNoise = hasClientProperty(jc, FueledCustomUI.CLIENT_PROPERTY_DARK_NOISE, 0);
            lightNoise = hasClientProperty(jc, FueledCustomUI.CLIENT_PROPERTY_LIGHT_NOISE, 0);
            if (darkDarkNoise == Integer.MAX_VALUE && darkNoise == Integer.MAX_VALUE && lightNoise == Integer.MAX_VALUE) {
                return;
            }

            border = jc.getBorder();
        }

        int dx = c.getLocationOnScreen().x;
        int dy = c.getLocationOnScreen().y;

        boolean clipped = false;

        if (border instanceof FueledTitledBorder) {
            clipped = true;
        }

        if (clipped) {
            drawImage(graphics, getIndexedImage(1, lightNoise, darkNoise, darkDarkNoise), x, y, width, height, dx, dy);
            RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, width, height, 16, 16);
            graphics.setClip(shape);
            drawImage(graphics, getIndexedImage(0, lightNoise, darkNoise, darkDarkNoise), x, y, width, height, dx, dy);
        } else {
            drawImage(graphics, getIndexedImage(0, lightNoise, darkNoise, darkDarkNoise), x, y, width, height, dx, dy);
        }
    }

    private void drawImage(Graphics graphics, Image image, int x, int y, int width, int height, int dx, int dy) {
        if (image == null) {
            return;
        }

        int nx = width / WATERMARK_WIDTH;
        int ny = height / WATERMARK_HEIGHT;

        for (int i = 0; i <= nx; i++) {
            for (int j = 0; j <= ny; j++) {
                graphics.drawImage(image, x + i * WATERMARK_WIDTH, y + j * WATERMARK_HEIGHT, null);
            }
        }

        //graphics.drawImage(image, x, y, x + width, y + height, x + dx, y + dy, x + dx + width, y + dy + height, null);
    }

    private Image getIndexedImage(int index, int lightNoise, int darkNoise, int darkDarkNoise) {
        Image image = null;
        int[] arr = new int[] { lightNoise, darkNoise, darkDarkNoise };
        Arrays.sort(arr);
        int mark = arr[index];
        //System.out.println(String.format("%d, %d, %d", lightNoise, darkNoise, darkDarkNoise));
        //System.out.println(String.format("%d, %d, %d, mark=%d", arr[0], arr[1], arr[2], mark));
        //System.out.println("-------------");
        if (mark == Integer.MAX_VALUE) {
            image = null;
        }
        if (lightNoise == mark) {
            image = watermarkLightImage;
        } else if (darkNoise == mark) {
            image = watermarkDarkImage;
        } else if (darkDarkNoise == mark) {
            image = watermarkDarkDarkImage;
        }
        return image;
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

        int screenWidth = WATERMARK_WIDTH;// virtualBounds.width;
        int screenHeight = WATERMARK_HEIGHT;// virtualBounds.height;

        this.watermarkDarkDarkImage = SubstanceCoreUtilities.getBlankImage(screenWidth, screenHeight);

        Graphics2D graphics = (Graphics2D) this.watermarkDarkDarkImage.getGraphics().create();

        boolean status = this.drawWatermarkImage(skin, graphics, 0, 0, screenWidth, screenHeight, false, ThemeMediator.CURRENT_THEME.getCustomUI().getDarkDarkNoise());
        graphics.dispose();

        this.watermarkDarkImage = SubstanceCoreUtilities.getBlankImage(screenWidth, screenHeight);

        graphics = (Graphics2D) this.watermarkDarkImage.getGraphics().create();

        status = status & this.drawWatermarkImage(skin, graphics, 0, 0, screenWidth, screenHeight, false, ThemeMediator.CURRENT_THEME.getCustomUI().getDarkNoise());
        graphics.dispose();

        this.watermarkLightImage = SubstanceCoreUtilities.getBlankImage(screenWidth, screenHeight);

        graphics = (Graphics2D) this.watermarkLightImage.getGraphics().create();

        status = status & this.drawWatermarkImage(skin, graphics, 0, 0, screenWidth, screenHeight, false, ThemeMediator.CURRENT_THEME.getCustomUI().getLightNoise());
        graphics.dispose();
        return status;
    }

    @Override
    public void previewWatermark(Graphics g, SubstanceSkin skin, int x, int y, int width, int height) {
        //this.drawWatermarkImage(skin, (Graphics2D) g, x, y, width, height, true);
    }

    @Override
    public void dispose() {
        this.watermarkDarkDarkImage = null;
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
        //SubstanceColorScheme scheme = skin.getWatermarkColorScheme();
        //if (isPreview) {
        graphics.drawImage(getNoiseImage(skin, width, height, true, color), x, y, null);
        //} else {
        //    int alpha = scheme.isDark() ? 200 : 140;
        //    graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255.0f));
        //    graphics.drawImage(getNoiseImage(skin, width, height, false, color), x, y, null);
        //}
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

    private int hasClientProperty(JComponent c, String propertyKey, int depth) {
        Boolean b = (Boolean) c.getClientProperty(propertyKey);
        if (b != null) {
            return b.booleanValue() ? depth : Integer.MAX_VALUE;
        } else if (c.getParent() instanceof JComponent) {
            int d = hasClientProperty((JComponent) c.getParent(), propertyKey, depth);
            return d != Integer.MAX_VALUE ? d + 1 : Integer.MAX_VALUE;
        } else {
            return Integer.MAX_VALUE;
        }
    }
}
