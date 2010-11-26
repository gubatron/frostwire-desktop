package com.frostwire.gnutella.gui.android;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.text.AttributedString;

import javax.smartcardio.ATR;
import javax.swing.JLabel;

public class MultilineLabel extends JLabel {
    
    /**
     * 
     */
    private static final long serialVersionUID = -6265191318434960684L;
    
    private static final String LN = System.getProperty("line.separator");
    
    private String _orignalText;
    
    @Override
    public void setText(String text) {
        super.setText(text);
        _orignalText = text;
    }

    @Override
    public void paint(Graphics g) {
        modifyText((Graphics2D) g);
        super.paint(g);
    }

    private void modifyText(Graphics2D g) {
     
        int width = getWidth();
        String newText = "";
        
        AttributedString attrStr = new AttributedString(_orignalText);
        
        LineBreakMeasurer linebreaker = new LineBreakMeasurer(attrStr.getIterator(), g.getFontRenderContext());

        int begin = 0;
        int end = 0;
        float y = 0.0f;
        while (linebreaker.getPosition() < _orignalText.length()) {
            TextLayout tl = linebreaker.nextLayout(width);

            y += tl.getAscent();
            tl.draw(g, 0, y);
            y += tl.getDescent() + tl.getLeading();
            
            begin = end;
            end = linebreaker.getPosition();
            
            newText += _orignalText.substring(begin, end) + (end == _orignalText.length() - 1 ? "" : LN);
        }
        
        setText(newText);
    }
}
