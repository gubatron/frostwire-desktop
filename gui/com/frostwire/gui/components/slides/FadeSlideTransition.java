package com.frostwire.gui.components.slides;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.frostwire.gui.components.ImageSlideshowPanel;

/**
 * TODO: Make this an Interface Implementation
 * @author gubatron
 *
 */
public class FadeSlideTransition {

    private ImageSlideshowPanel _panel;
    private BufferedImage _imageStart;
    private BufferedImage _imageEnd;

    private int _cycles = 100;
    private long _sleepTime = 20;
    private int _counter = 0;

    private boolean _running;

    public FadeSlideTransition(ImageSlideshowPanel panel, BufferedImage imageStart, BufferedImage imageEnd) {
        _panel = panel;
        _imageStart = imageStart;
        _imageEnd = imageEnd;
    }

    public void start() {
        _running = true;
        _counter = 0;

        Thread t = new Thread(new Runnable() {
            public void run() {
                _panel.onTransitionStarted();

                for (_counter = 0; _counter < _cycles; _counter++) {
                    if (!_panel.isShowing()) {
                        break;
                    }
                    try {
                        Thread.sleep(_sleepTime);
                        _panel.repaint();
                        _panel.getToolkit().sync();
                    } catch (Throwable tt) {
                        break;
                    }
                }

                _running = false;
            }
        });
        t.start();
    }

    public synchronized void paint(Graphics g) {
        if (!_running) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g;
        float alpha = _counter / 100f;

        if (alpha < 0)
            alpha = 0;
        if (alpha > 1)
            alpha = 1;

        java.awt.Composite oldComp = g2d.getComposite();

        g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1 - alpha));
        g2d.drawImage(_imageStart, null, 0, 0);

        g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
        g2d.drawImage(_imageEnd, null, 0, 0);

        g2d.setComposite(oldComp);
    }

    public boolean isRunning() {
        return _running;
    }

    public long getEstimatedDuration() {
        return _cycles * _sleepTime + 50; // 50 is a draw time arbitrary correction
    }

    public void stop() {
        _counter = Integer.MAX_VALUE;
        _running = false;
    }
}
