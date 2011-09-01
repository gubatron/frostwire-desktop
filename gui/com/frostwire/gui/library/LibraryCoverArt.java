package com.frostwire.gui.library;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;

public class LibraryCoverArt extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 4302859512245078593L;
    
    public LibraryCoverArt() {
        setBackground(Color.BLUE);
        setPreferredSize(new Dimension(200, 200));
        setMinimumSize(new Dimension(LibraryMediator.MIN_LEFT_SIDE_WIDTH,LibraryMediator.MIN_LEFT_SIDE_WIDTH));
        
        addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				int w = getWidth();
				
				if (w != getHeight()) {
//					setSize(w,w);
				}
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
     
    }
}
