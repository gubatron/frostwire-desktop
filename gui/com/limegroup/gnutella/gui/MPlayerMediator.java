package com.limegroup.gnutella.gui;

import javax.swing.SwingUtilities;

import com.frostwire.gui.mplayer.MPlayerWindow;
import com.frostwire.gui.player.MediaPlayer;

public class MPlayerMediator {

    private static MPlayerMediator instance;
    private final MPlayerWindow mplayerWindow;

    private MPlayerMediator() {
    	mplayerWindow = new MPlayerWindow();
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
            	}});
        	} catch (Exception e) {
        		
        	}
        }
        return instance;
    }
    
    public long getCanvasComponentHwnd() {
    	return mplayerWindow.getCanvasComponentHwnd();
    }
    
    public void showPlayerWindow(final boolean visible) {
    	try {
    		if(SwingUtilities.isEventDispatchThread()) {
    			//the mplayerWindow might not have been initialized yet since it's
    			//initialized on the UI thread. 
    			if (mplayerWindow != null) {
    				mplayerWindow.setVisible(visible);
    			}
    		} else {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						if (mplayerWindow != null) {
							mplayerWindow.setVisible(visible);
						}
					}
				});
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void toggleFullScreen() {
    	try {
    		if ( !SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						mplayerWindow.toggleFullScreen();
					}
				});
    		} else {
    			mplayerWindow.toggleFullScreen();
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
