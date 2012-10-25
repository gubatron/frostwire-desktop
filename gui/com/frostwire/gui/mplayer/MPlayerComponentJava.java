package com.frostwire.gui.mplayer;

import java.awt.BorderLayout;
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

public class MPlayerComponentJava extends Container implements MPlayerComponent {

	private static final long serialVersionUID = -5860833860676831251L;
	
	private Canvas video;
	private JPanel controlsOverlay;
	private JFrame fullscreenWindow;
	private JLayeredPane fullscreenPane;
	
	public MPlayerComponentJava() {
		
		Dimension defaultVideoDim = new Dimension(500,500);
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        
		setLayout( new BorderLayout() );
		
        // initialize video canvas
        video = new Canvas();
        video.setPreferredSize(defaultVideoDim);
        //video.setMinimumSize(new Dimension(0,0));
        //video.setMaximumSize(screenDim);
        video.setVisible(true);
        video.setBackground(Color.BLACK);
        //video.setBounds(0, 0, defaultVideoDim.width, defaultVideoDim.height); //jLayered pane needs you to use setBounds on its components, otherwise they're not shown
        //video.setIgnoreRepaint(false);
        add(video);
        
        // initialize control overlay
        Dimension overlayDim = new Dimension( 400, 100 );
        controlsOverlay = new JPanel(new FlowLayout());
        controlsOverlay.setVisible(true);
        controlsOverlay.setBounds( 0, 0, overlayDim.width, overlayDim.height );
        controlsOverlay.setOpaque(true);
        controlsOverlay.setBackground(Color.darkGray);
        JButton button = new JButton("Button on FirstPanel");
        button.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MPlayerComponentJava.this.toggleFullScreen();
			}
        });
        controlsOverlay.add(button);
        
        // initialize fullscreen window
        fullscreenWindow = new JFrame();
        fullscreenWindow.setLayout(null);
        fullscreenWindow.setUndecorated(true);
        fullscreenWindow.setSize( screenDim );
        fullscreenWindow.setResizable(false);
        fullscreenWindow.setAlwaysOnTop(true);
        fullscreenWindow.setVisible(false);
        
        fullscreenPane = new JLayeredPane();
        fullscreenPane.setBounds(0, 0, screenDim.width, screenDim.height);
        fullscreenPane.add(controlsOverlay, JLayeredPane.PALETTE_LAYER);
        fullscreenWindow.getContentPane().add(fullscreenPane);
    	
        resetOverlayPosition();
    }
	
	private void resetOverlayPosition() {
		
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		
		Rectangle overlayRect = controlsOverlay.getBounds();
		overlayRect.x = (int) (screen.width*0.5 - overlayRect.width/2);
		overlayRect.y = (int) (screen.height*0.75 - overlayRect.height/2);
		controlsOverlay.setBounds( overlayRect );
	}
	
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public void toggleFullScreen() {

		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		
		if (video.getParent() == this) { // entering fullscreen
			
			video.setSize( screenDim );
			fullscreenPane.add(video, JLayeredPane.DEFAULT_LAYER);
			fullscreenWindow.setVisible(true);
			
			
		} else { // leaving fullscreen
			
			fullscreenWindow.setVisible(false);
			
			video.setSize( getSize() );
			add(video);
			validate();
		}
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
