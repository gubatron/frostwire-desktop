package com.frostwire.gui.mplayer;

import sun.awt.X11.XComponentPeer;

public class MPlayerWindow_Linux extends MPlayerWindow {

	private static final long serialVersionUID = -4373778544356324171L;

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
}
