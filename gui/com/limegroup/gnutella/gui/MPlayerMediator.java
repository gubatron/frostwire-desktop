package com.limegroup.gnutella.gui;

import com.frostwire.gui.mplayer.MPlayerWindow;
import com.frostwire.gui.player.MediaPlayer;

public class MPlayerMediator {

    private static MPlayerMediator instance;
    private final MPlayerWindow mplayerWindow;

    private MPlayerMediator() {
    	mplayerWindow = MPlayerWindow.createMPlayerWindow();
    }
    
    public MPlayerWindow getMPlayerWindow() {
    	return mplayerWindow;
    }
    
    public MediaPlayer getMediaPlayer() {
    	if (mplayerWindow == null) {
    		return null;
    	}
    	return mplayerWindow.getMediaPlayer();
    }
    
    public static MPlayerMediator instance() {
        if (instance == null) {
        	try {
                GUIMediator.safeInvokeAndWait(new Runnable() {
                	@Override
                	public void run() {
                		instance = new MPlayerMediator();
                	}
            	});
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
        return instance;
    }
    
    public long getCanvasComponentHwnd() {
    	return mplayerWindow.getCanvasComponentHwnd();
    }
    
    public void showPlayerWindow(final boolean visible) {
    	try {
    	    GUIMediator.safeInvokeAndWait(new Runnable() {
    	        public void run() {
                    if (mplayerWindow != null) {
                        mplayerWindow.setVisible(visible);
                    }
                }
    	    });
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void toggleFullScreen() {
    	try {
    	    GUIMediator.safeInvokeAndWait(new Runnable() {
                public void run() {
                    mplayerWindow.toggleFullScreen();
                }
            });
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
