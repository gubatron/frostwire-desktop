package com.frostwire.gui;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.KeyEvent;

/**
 * Tracks mouse idleness when track() is invoked by checking if the mouse
 * changed position from the last time track() was invoked.
 * 
 * Keyboard tracking is done with a KeyEventDispatcher.
 * @author gubatron
 *
 */
public final class InputIdleTracker {

    private Point lastLocation;
    private long lastTimeMillis;

    private static final InputIdleTracker instance = new InputIdleTracker();

    public static InputIdleTracker instance() {
        return instance;
    }

    private InputIdleTracker() {
        this.lastLocation = new Point(0, 0);
        this.lastTimeMillis = Long.MAX_VALUE;
        
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
            	InputIdleTracker.this.lastTimeMillis = System.currentTimeMillis();
            	return false;
            }
        });
    }

    public long idleTimeMillis() {
        long d = System.currentTimeMillis() - lastTimeMillis;
        return d > 0 ? d : 0;
    }
    
    public void trackMouse() {
        Point location = MouseInfo.getPointerInfo().getLocation();

        if (location != null && !location.equals(lastLocation)) {
            lastLocation = location;
            lastTimeMillis = System.currentTimeMillis();
        }
    }
}
