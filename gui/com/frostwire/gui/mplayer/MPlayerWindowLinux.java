package com.frostwire.gui.mplayer;

import org.limewire.util.SystemUtils;

import sun.awt.X11.XComponentPeer;
import sun.awt.X11.XWindow;

public class MPlayerWindowLinux extends MPlayerWindow {

	private static final long serialVersionUID = -4373778544356324171L;
	private static boolean isFullScreen = false;
	
	public MPlayerWindowLinux() {
		System.out.println("MPlayerWindowLinux hwnd: " + getHwnd());
	}
	
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
		//SystemUtils.toggleFullScreen(getHwnd());
		
		setExtendedState( isFullScreen ? NORMAL : MAXIMIZED_BOTH);
		super.toggleFullScreen();
		
		isFullScreen = !isFullScreen;
		
	}
}
