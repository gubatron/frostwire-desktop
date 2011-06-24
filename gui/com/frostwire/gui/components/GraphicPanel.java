package com.frostwire.gui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import javax.swing.JPanel;

public class GraphicPanel extends JPanel {
    
    /**
     * 
     */
    private static final long serialVersionUID = 9079271524527772928L;
    
    private GradientPaint _gradient;
    private Image _image;

	private String _text;

	private Image _textImage;
    
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
    
    public void setText(String text) {
    	_textImage = null;    	
    	_text = text;
    	repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {

        if (_gradient != null) {
        	Graphics2D g2 = (Graphics2D) g;
        	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(_gradient);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        
        if (_image != null) {
            g.drawImage(_image, 0, 0, null);
        }
        
        if (_text != null) {
        	if (_textImage == null) {
        		_textImage = buildTextImage(_text);
        	}
        	g.drawImage(_textImage,0,8,null);
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
            int h = metrics.getHeight() + 20;

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
            
            //TODO Parametrize this
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
}
