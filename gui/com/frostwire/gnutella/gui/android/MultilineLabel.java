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
     
        try {
            
            if (_originalText.indexOf("libbfd") != -1) {
                int x = 0;
            }
            
            FontMetrics metrics = g.getFontMetrics(getFont());
            
            int width = getWidth() - metrics.stringWidth(" ") - 4;
            
            char[] arr = _originalText.toCharArray();
            
            int len = 0;
            while (len < arr.length && metrics.charsWidth(arr, 0, len) < width) {
                len++;
            }
            
            if (len == _originalText.length()) { // one line
                _calculatedText = "<html>" + makeParagraph(fillText(metrics, _originalText, width)) + "<html>";
            } else { // perform two lines layout
                int index = _originalText.lastIndexOf(" ");
                if (index == -1) { // no suitable blank space to break at
                    index = len;
                }
                String line1 = _originalText.substring(0, index);
                String line2 = _originalText.substring(index);
                if (line2.length() >= len - 1) { // perform ellipsis
                    len = 0;
                    while (metrics.stringWidth(line2.substring(0, len) + "... ") < width) {
                        len++;
                    }
                    line2 = line2.substring(0, len) + "...";
                }
                
                _calculatedText = "<html>" + makeParagraph(fillText(metrics, line1, width)) + makeParagraph(fillText(metrics, line2, width)) + "<html>";
            }
        } catch (Exception e) { // poor logic in some place
            _calculatedText = _originalText;
        }
        
        setText(_calculatedText);
    }
    
    private String fillText(FontMetrics metrics, String text, int width) {
        boolean flag = true;
        while (metrics.stringWidth(text + " ") < width) {
            if (flag) {
                text = text + " ";
            } else {
                text = " " + text;
            }
            
            flag = !flag;
        }
        
        return text.replace(" ", "&nbsp;");
    }
    
    private String makeParagraph(String text) {
        return "<p>" + text + "</p>";
    }
}
