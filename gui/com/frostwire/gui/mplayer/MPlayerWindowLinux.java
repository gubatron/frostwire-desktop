package com.frostwire.gui.mplayer;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import sun.awt.X11.XComponentPeer;

public class MPlayerWindowLinux extends MPlayerWindow {

	private static final long serialVersionUID = -4373778544356324171L;
	private static boolean isFullScreen = false;
	
	@Override
	public long getCanvasComponentHwnd() {
		@SuppressWarnings("deprecation")
        XComponentPeer cp = (XComponentPeer) videoCanvas.getPeer();
        if ((cp instanceof XComponentPeer)) {
            return ((XComponentPeer) cp).getWindow();
        } else {
            return 0;
        }
	}

	@Override
	public long getHwnd() {
		@SuppressWarnings("deprecation")
        XComponentPeer cp = (XComponentPeer) getPeer();
        if ((cp instanceof XComponentPeer)) {
            return ((XComponentPeer) cp).getWindow();
        } else {
            return 0;
        }
	}
	
	@Override
	public void toggleFullScreen() {
		
		isFullScreen = !isFullScreen;
		
		setExtendedState( isFullScreen ? MAXIMIZED_BOTH : NORMAL);
		super.toggleFullScreen();
	}
	
	// on linux, alpha composite trick not working to get desired background color.  however,
	// changing default paint behavior of window does work.
	@Override
	public void paint(Graphics g) {
    }
}
