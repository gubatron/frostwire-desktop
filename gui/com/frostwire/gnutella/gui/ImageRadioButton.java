package com.frostwire.gnutella.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JRadioButton;

public class ImageRadioButton extends JRadioButton {
    
    private static final long serialVersionUID = 3261658194515096352L;
    
    private Icon _icon;
    private Icon _pressedIcon;
    private Icon _selectedIcon;
    
    private String _imageText;

    public ImageRadioButton() {
        setupUI();
    }
    
    @Override
    public void setIcon(Icon defaultIcon) {
        super.setIcon(defaultIcon);
        _icon = defaultIcon;
    }
    
    @Override
    public void setPressedIcon(Icon pressedIcon) {
        super.setPressedIcon(pressedIcon);
        _pressedIcon = pressedIcon;
    }
    
    @Override
    public void setSelectedIcon(Icon selectedIcon) {
        super.setSelectedIcon(selectedIcon);
        _selectedIcon = selectedIcon;
    }
    
    @Override
    public String getText() {
        return null;// to nullify the text paint area.
    }
    
    /**
     * This method must be called only after setIcon and setPressedIcon, otherwise there is no
     * image composite effect.
     */
    @Override
    public void setText(String text) {
        if (_imageText != null && _imageText.equals(text)) {
            return;
        }
        _imageText = text;
        BufferedImage textImage = buildTextImage(text);
        setCompositeIcon(textImage);
        setCompositePressedIcon(textImage);
        setCompositeSelectedIcon(textImage);
    }
    
    protected void setupUI() {
        //////////visible effect trick
        setBorder(null);
        setBackground(null);
        setFocusable(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        ////////////////////////////////
    }
    
    private void setCompositeIcon(BufferedImage textImage) {
        BufferedImage image = null;
        Icon icon = _icon;
        
        if (icon != null && icon instanceof ImageIcon && ((ImageIcon) icon).getImage() instanceof BufferedImage) {
            image = (BufferedImage) ((ImageIcon) icon).getImage();
            image = joinImages(image, textImage);
            super.setIcon(new ImageIcon(image));
        }
    }
    
    private void setCompositePressedIcon(BufferedImage textImage) {
        BufferedImage image = null;
        Icon icon = _pressedIcon;
        
        if (icon != null && icon instanceof ImageIcon && ((ImageIcon) icon).getImage() instanceof BufferedImage) {
            image = (BufferedImage) ((ImageIcon) icon).getImage();
            image = joinImages(image, textImage);
            super.setPressedIcon(new ImageIcon(image));
        }
    }
    
    private void setCompositeSelectedIcon(BufferedImage textImage) {
        BufferedImage image = null;
        Icon icon = _selectedIcon;
        
        if (icon != null && icon instanceof ImageIcon && ((ImageIcon) icon).getImage() instanceof BufferedImage) {
            image = (BufferedImage) ((ImageIcon) icon).getImage();
            image = joinImages(image, textImage);
            super.setSelectedIcon(new ImageIcon(image));
        }
    }
    
    private BufferedImage buildTextImage(String text) {
        // TODO: Use parameters for colors.
        
        Font font = getFont();

        Graphics2D graphicsDummy = null;
        Graphics2D graphics1 = null;
        Graphics2D graphics2 = null;
        
        BufferedImage image = null;

        try {

            BufferedImage imageDummy = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            graphicsDummy = imageDummy.createGraphics();
            graphicsDummy.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphicsDummy.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            FontMetrics metrics = graphicsDummy.getFontMetrics(font);
            int w = metrics.stringWidth(text) + 20;
            int h = metrics.getHeight();

            BufferedImage image1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            graphics1 = image1.createGraphics();
            graphics1.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics1.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            //draw "shadow" text: to be blurred next
            TextLayout textLayout = new TextLayout(text, font, graphics1.getFontRenderContext());
            graphics1.setPaint(Color.BLACK);
            textLayout.draw(graphics1, 11, 14);
            graphics1.dispose();

            //blur the shadow: result is sorted in image2
            float ninth = 1.0f / 9.0f;
            float[] kernel = { ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth };
            ConvolveOp op = new ConvolveOp(new Kernel(3, 3, kernel), ConvolveOp.EDGE_NO_OP, null);
            BufferedImage image2 = op.filter(image1, null);

            //write "original" text on top of shadow
            graphics2 = image2.createGraphics();
            graphics2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics2.setPaint(Color.WHITE);
            textLayout.draw(graphics2, 10, 13);
            
            image = image2;
            
        } finally {
            if (graphicsDummy != null) {
                graphicsDummy.dispose();
            }
            if (graphicsDummy != null) {
                graphics1.dispose();
            }
            if (graphicsDummy != null) {
                graphics2.dispose();
            }
        }

        return image;
    }
    
    private BufferedImage joinImages(BufferedImage image1, BufferedImage image2) {
        // TODO: Use relative text position from control.
        
        int w1 = image1.getWidth();
        int h1 = image1.getHeight();
        int w2 = image2.getWidth();
        int h2 = image2.getHeight();
        
        int w = w1 >= w2 ? w1 : w2;
        int h = h1 + h2;
        
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D graphics = image.createGraphics();
        
        try {
            
            graphics.drawImage(image1, (w - w1) / 2,  0, w1, h1, null);
            graphics.drawImage(image2, (w - w2) / 2, h1, w2, h2, null);
            
        } finally {
            if (graphics != null) {
                graphics.dispose();
            }
        }
        
        return image;
    }
}
