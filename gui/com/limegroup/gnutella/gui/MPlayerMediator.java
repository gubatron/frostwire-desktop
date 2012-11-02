package com.limegroup.gnutella.gui;

import javax.swing.SwingUtilities;

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
    
    public void showPlayerWindow(final boolean visible) {
    	try {
    		if(SwingUtilities.isEventDispatchThread()) {
    			mplayerWindow.setVisible(visible);
    		} else {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						mplayerWindow.setVisible(visible);
					}
				});
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void toggleFullScreen() {
    	try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					mplayerWindow.toggleFullScreen();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
