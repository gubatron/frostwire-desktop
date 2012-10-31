package com.limegroup.gnutella.gui;

import com.frostwire.gui.mplayer.MPlayerWindow;

public class MPlayerMediator {

    private static MPlayerMediator _instance;
    private final MPlayerWindow mplayerWindow;

    private MPlayerMediator() {
    	mplayerWindow = new MPlayerWindow();
    }
    
    public static MPlayerMediator instance() {
        if (_instance == null)
            _instance = new MPlayerMediator();
        return _instance;
    }
    
    public long getCanvasComponentHwnd() {
    	return mplayerWindow.getCanvasComponentHwnd();
    }
    
    public void showPlayerWindow(boolean visible) {
    	mplayerWindow.setVisible(visible);
    }

    public void toggleFullScreen() {
    	mplayerWindow.toggleFullScreen();
    }
}
