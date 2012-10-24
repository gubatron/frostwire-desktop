package com.frostwire.gui.mplayer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class MPlayerComponentJava extends JLayeredPane implements MPlayerComponent {

	private static final long serialVersionUID = -5860833860676831251L;
	private Canvas video = null;
	private JPanel controlOverlay = null;
	
	private Container priorParent = null;
	private JFrame    fullscreenWindow = null;
	private boolean   isFullscreen = false;
	
	public MPlayerComponentJava() {
		
		Dimension d = new Dimension(500,500);
        setPreferredSize(d);
        setMinimumSize(d);
        setOpaque(true);
        setVisible(true);
        
        // initialize video canvas
        video = new Canvas();
        //video.setMinimumSize(d);
        video.setSize(d);
        video.setVisible(true);
        video.setBackground(Color.BLACK);
        video.setEnabled(true);
        video.setBounds(0, 0, (int) d.getWidth(), (int) d.getHeight()); //jLayered pane needs you to use setBounds on its components, otherwise they're not shown
        video.setIgnoreRepaint(false);
        
        // initialize control overlay
        Dimension overlayDim = new Dimension( 400, 100 );
        Rectangle overlayRect = new Rectangle(d.width/2 - overlayDim.width/2, d.height/2 - overlayDim.height/2, overlayDim.width, overlayDim.height);
        controlOverlay = new JPanel(new FlowLayout());
        controlOverlay.setVisible(true);
        controlOverlay.setBounds( overlayRect );
        controlOverlay.setBackground(new Color(255, 255, 255,255)); //background color with alpha
        controlOverlay.setOpaque(false);
        JButton button = new JButton("Button on FirstPanel");
        button.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MPlayerComponentJava.this.toggleFullScreen();
			}
        });
        controlOverlay.add(button);
        
        
        // add overlay and video canvas
        add(video, JLayeredPane.DEFAULT_LAYER);
        add(controlOverlay, JLayeredPane.PALETTE_LAYER);
        
        /*
        // add listener for parent changes
        this.addHierarchyListener( new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if ( (e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) == 
						HierarchyEvent.PARENT_CHANGED ) {
					
					if ( MPlayerComponentJava.this.getParent() != null) {
					}
					
					resizeToParent();
				}
			}
        });
        */
        
        // initialize fullscreen window
        fullscreenWindow = new JFrame();
        fullscreenWindow.setUndecorated(true);
        fullscreenWindow.getContentPane().setPreferredSize( Toolkit.getDefaultToolkit().getScreenSize() );
        fullscreenWindow.pack();
        fullscreenWindow.setResizable(false);
        fullscreenWindow.setAlwaysOnTop(true);
        fullscreenWindow.setVisible(false);
        
	}
	
	/*
	private void resizeToParent() {
		if ( getParent() != null ) {
			Dimension d = getParent().getSize();
			setSize(d);
			resetOverlayPosition();
		}
	}
	
	private void resetOverlayPosition() {
		Rectangle overlayRect = getBounds();
		overlayRect.x = getSize().width/2 - overlayRect.width/2;
		overlayRect.y = getSize().height/2 - overlayRect.height/2;
		setBounds( overlayRect );
	}
	*/
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public void toggleFullScreen() {

		if (false == isFullscreen) {
			
			// remember previous parent
			priorParent = getParent();
			priorParent.remove(this);
			
			// add us as the child to window
			fullscreenWindow.getContentPane().add(this);
			
			// show fullscreen window
			fullscreenWindow.setVisible(true);
						
		} else {
			
			// remove from fullscreen
			fullscreenWindow.getContentPane().remove(this);
			
			// add us as child to prior parent
			priorParent.add(this);
			priorParent = null;
			
			// hide fullscreen window
			fullscreenWindow.setVisible(false);
		}
		
		isFullscreen = !isFullscreen;
	}

	@SuppressWarnings("deprecation")
	@Override
	public long getWindowID() {
		long hWnd = 0;
		
		try {
	
			Class<?> cl = Class.forName("sun.awt.windows.WComponentPeer");
	        java.lang.reflect.Field f = cl.getDeclaredField("hwnd");
	        f.setAccessible(true); //little reflection hack to access the hwnd from windows.
	        hWnd = f.getLong(video.getPeer());
    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return hWnd;
	}


}
