package com.frostwire.gui.components.slides;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.frostwire.gui.components.Slide;

public class SlideControlsOverlay extends JPanel {
    private SlidePanelController controller;

    public SlideControlsOverlay(SlidePanelController controller) {
        this.controller = controller;
        setupUI();
    }

    private void setupUI() {
        setOpaque(false);
        setLayout(new FlowLayout());
        setBackground(new Color(0,0,0));
        setupButtons();
    }

    private void setupButtons() {
        Slide slide = controller.getSlide();
        if (slide.hasFlag(Slide.SLIDE_DOWNLOAD_METHOD_HTTP) ||
            slide.hasFlag(Slide.SLIDE_DOWNLOAD_METHOD_TORRENT)) {
            
            if (slide.hasFlag(Slide.POST_DOWNLOAD_EXECUTE)) {
                //add install button
                add(new JButton("INSTALL"));
            } else {
                //add download button
                add(new JButton("DOWNLOAD"));
            }
        }
        
        if (slide.hasFlag(Slide.SHOW_VIDEO_PREVIEW_BUTTON)) {
            //add video preview button
            add(new JButton("PLAY VIDEO"));
        }
        
        if (slide.hasFlag(Slide.SHOW_AUDIO_PREVIEW_BUTTON)) {
            //add audio preview button
            add(new JButton("PLAY AUDIO"));
        }
    }
    
    @Override
    public void paint(Graphics g) {
        
        //super.paintComponents(g);
        //super.paint(g);
        
        Color background = getBackground();
        Graphics2D g2d = (Graphics2D) g;
        
        Composite c = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 0.7f));
        g.setColor(background);
        g.fillRect(0, 0, getWidth(), getHeight());
        g2d.setComposite(c);
        
        super.paint(g);
    }
    
    
}