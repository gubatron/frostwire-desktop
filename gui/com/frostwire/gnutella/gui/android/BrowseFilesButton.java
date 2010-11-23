package com.frostwire.gnutella.gui.android;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JRadioButton;

public class BrowseFilesButton extends JRadioButton {
    
    private static final long serialVersionUID = 3261658194515096352L;
    
    private Font _font1;
    private Font _font2;
    
    private Image _textImage;
    private ImageIcon _innerImage;
    private ImageIcon _innetImagePressed;
    
    public BrowseFilesButton() {
        setupUI();
    }
    
    @Override
    public String getText() {
        return null;// to nullify the text paint area.
    }
    
    @Override
    public void setText(String text) {
        Icon icon = getIcon();
        _textImage = buildTextImage(text);
        if (icon != null && icon instanceof ImageIcon && ((ImageIcon) icon).getImage() instanceof BufferedImage) {
            //BufferedImage image = (BufferedImageshow f) ((ImageIcon) icon).getImage();
        }
    }
    
    private void setupUI() {
        //////////visible effect trick
        setBorder(null);
        setBackground(null);
        setFocusable(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        ////////////////////////////////
    }
    
    private Image buildTextImage(String text) {
        
        int w = getWidth();
        int h = getHeight();
        Font font = new Font("Lucida Bright", Font.ITALIC, 72);
        
        BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
        //start off all white:
        g.setPaint(Color.WHITE);
        g.fillRect(0, 0, w, h);
        
        //draw "shadow" text: to be blurred next
        TextLayout textLayout = new TextLayout(text, font, g.getFontRenderContext());
        g.setPaint(new Color(128,128,255));
        textLayout.draw(g, 15, 105);
        g.dispose();
        
        //blur the shadow: result is sorted in image2
        float ninth = 1.0f / 9.0f;
        float[] kernel = {ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth};
        ConvolveOp op = new ConvolveOp(new Kernel(3, 3, kernel), ConvolveOp.EDGE_NO_OP, null);
        BufferedImage image2 = op.filter(image,null);
        
        //write "original" text on top of shadow
        Graphics2D g2 = image2.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setPaint(Color.BLACK);
        textLayout.draw(g2, 10, 100);
        
        return image2;
    }
    
    private BufferedImage joinImages(BufferedImage image1, BufferedImage image2) {
        int w1 = image1.getWidth();
        int h1 = image1.getHeight();
        int w2 = image2.getWidth();
        int h2 = image2.getHeight();
        //FIXME
        return null;
    }
}
