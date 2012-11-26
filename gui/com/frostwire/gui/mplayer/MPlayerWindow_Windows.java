package com.frostwire.gui.mplayer;

import java.awt.peer.ComponentPeer;
import sun.awt.windows.WComponentPeer;

public class MPlayerWindow_Windows extends MPlayerWindow {

	private static final long serialVersionUID = 5711345717783989492L;

	public long getCanvasComponentHwnd() {
        @SuppressWarnings("deprecation")
        ComponentPeer cp = videoCanvas.getPeer();
        if ((cp instanceof WComponentPeer)) {
            return ((WComponentPeer) cp).getHWnd();
        } else {
            return 0;
        }
    }
	
	public long getHwnd() {
		@SuppressWarnings("deprecation")
        ComponentPeer cp = getPeer();
        if ((cp instanceof WComponentPeer)) {
            return ((WComponentPeer) cp).getHWnd();
        } else {
            return 0;
        }
    }
}
