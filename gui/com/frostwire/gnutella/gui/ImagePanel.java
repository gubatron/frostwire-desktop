package com.frostwire.gnutella.gui;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
    
    /**
     * 
     */
    private static final long serialVersionUID = 9079271524527772928L;
    
    private Image _image;

    public ImagePanel() {
    }

    public ImagePanel(Image image) {
        setImage(image);
    }
    
    public Image getImage() {
        return _image;
    }
    
    public void setImage(Image image) {
        _image = image;
    }    

    @Override
    protected void paintComponent(Graphics g) {
        if (_image != null) {
            g.drawImage(_image, 0, 0, null);
        }
    }
}
