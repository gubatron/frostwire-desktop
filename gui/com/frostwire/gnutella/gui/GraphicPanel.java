package com.frostwire.gnutella.gui;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class GraphicPanel extends JPanel {
    
    /**
     * 
     */
    private static final long serialVersionUID = 9079271524527772928L;
    
    private GradientPaint _gradient;
    private Image _image;
    
    public GraphicPanel() {
    }
    
    public GradientPaint getGradient() {
        return _gradient;
    }
    
    public void setGradient(GradientPaint gradient) {
        _gradient = gradient;
    }
    
    public Image getImage() {
        return _image;
    }
    
    public void setImage(Image image) {
        _image = image;
    }    

    @Override
    protected void paintComponent(Graphics g) {
        
        if (_gradient != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(_gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        
        if (_image != null) {
            g.drawImage(_image, 0, 0, null);
        }
    }
}
