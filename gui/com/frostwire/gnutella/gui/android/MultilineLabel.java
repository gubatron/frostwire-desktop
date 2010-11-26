package com.frostwire.gnutella.gui.android;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;

public class MultilineLabel extends JLabel {
    
    /**
     * 
     */
    private static final long serialVersionUID = -6265191318434960684L;
    
    private String _originalText;
    private String _calculatedText;
    
    @Override
    public void setText(String text) {
        super.setText(text);
        _originalText = text;
        _calculatedText = null;
    }

    @Override
    public void paint(Graphics g) {
        if (_calculatedText == null) {
            calculateText((Graphics2D) g);
        }
        super.paint(g);
    }

    private void calculateText(Graphics2D g) {
     
        int width = getWidth();
        
        FontMetrics metrics = g.getFontMetrics(getFont());
        
        char[] arr = _originalText.toCharArray();
        
        int len = 0;
        while (len < arr.length && metrics.charsWidth(arr, 0, len) < width) {
            len++;
        }
        
        if (len == _originalText.length()) { // one line
            _calculatedText = "<html><p>" + _originalText + "</p><html>";
        } else { // perform two lines layout
            String line1 = _originalText.substring(0, len - 1);
            String line2 = _originalText.substring(len);
            if (line2.length() > len) { // perform ellipsis
                int line1Width = metrics.stringWidth(line1);
                while (metrics.stringWidth((line2 = line2.substring(0, line2.length() - 1)) + "...") > line1Width);
                line2 = line2 + "...";
            }
            
            _calculatedText = "<html><p>" + line1 + "</p><p>" + line2 + "</p><html>";
        }
        
        setText(_calculatedText);
    }
}
