package com.frostwire.gui.mplayer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class MPlayerComponentJava extends JLayeredPane implements MPlayerComponent {

	private static final long serialVersionUID = -5860833860676831251L;
	private Canvas video;
	private JPanel controlOverlay;
	
	public MPlayerComponentJava() {
		super();
		
		Dimension d = new Dimension(1024,768);
        setPreferredSize(d);
        setMinimumSize(d);
        setOpaque(true);
        setVisible(true);
        
        // initialize video canvas
        video = new Canvas();
        video.setMinimumSize(d);
        video.setPreferredSize(d);
        video.setVisible(true);
        video.setBackground(Color.BLACK);
        video.setEnabled(true);
        video.setBounds(0, 0, (int) d.getWidth(), (int) d.getHeight()); //jLayered pane needs you to use setBounds on its components, otherwise they're not shown
        video.setIgnoreRepaint(false);
        
        // initialize & hide control overlay
        Dimension overlayDim = new Dimension(400, 100);
        controlOverlay = new JPanel(new FlowLayout());
        controlOverlay.setMinimumSize(overlayDim);
        controlOverlay.setPreferredSize(overlayDim);
        controlOverlay.add(new JButton("Button on First Panel"));
        controlOverlay.setVisible(true);
        controlOverlay.setBounds(200,d.height-overlayDim.height,overlayDim.width,overlayDim.height);
        controlOverlay.setBackground(new Color(255, 255, 255,128)); //background color with alpha
        //controlOverlay.setOpaque(false);
        
        add(video);
        add(controlOverlay);
        invalidate();
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public void toggleFullScreen() {
		// TODO Auto-generated method stub
		
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
