package com.frostwire.gui.player;

import java.util.LinkedList;

public class MPlayerUIEventHandler {
	
	private static MPlayerUIEventHandler instance = null;
	
	public static MPlayerUIEventHandler instance() {
		if ( instance == null ) {
			instance = new MPlayerUIEventHandler();
		}
		return instance;
	}
	
	private MPlayerUIEventHandler() {
		
	}
	
	private LinkedList<MPlayerUIEventListener> listeners = new LinkedList<MPlayerUIEventListener>();
	
	public void addListener( MPlayerUIEventListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener( MPlayerUIEventListener listener) {
		listeners.remove(listener);
	}
	
    public void onVolumeChanged(float volume) {
        for ( MPlayerUIEventListener listener : listeners ) {
        	listener.onUIVolumeChanged(volume);
        }
    }

    public void onSeekToTime(float seconds) {
        for ( MPlayerUIEventListener listener : listeners ) {
        	listener.onUISeekToTime(seconds);
        }
    }
    
    public void onPlayPressed() {
        for ( MPlayerUIEventListener listener : listeners ) {
        	listener.onUIPlayPressed();
        }
    }
    
    public void onPausePressed() {
        for ( MPlayerUIEventListener listener : listeners ) {
        	listener.onUIPausePressed();
        }
    }
    
    public void onFastForwardPressed() {
        for ( MPlayerUIEventListener listener : listeners ) {
        	listener.onUIFastForwardPressed();
        }
    }
    
    public void onRewindPressed() {
        for ( MPlayerUIEventListener listener : listeners ) {
        	listener.onUIRewindPressed();
        }
    }
    
    public void onToggleFullscreenPressed() {
        for ( MPlayerUIEventListener listener : listeners ) {
        	listener.onUIToggleFullscreenPressed();
        }
    }
}
