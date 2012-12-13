package com.frostwire.gui;

import java.awt.MouseInfo;
import java.awt.Point;

public final class MouseTracker {

    private Point lastLocation;
    private long lastTimeMillis;

    private static final MouseTracker instance = new MouseTracker();

    public static MouseTracker instance() {
        return instance;
    }

    private MouseTracker() {
        this.lastLocation = new Point(0, 0);
        this.lastTimeMillis = Long.MAX_VALUE;
    }

    public long idleTimeMillis() {
        long d = System.currentTimeMillis() - lastTimeMillis;
        return d > 0 ? d : 0;
    }

    public void track() {
        Point location = MouseInfo.getPointerInfo().getLocation();

        if (location != null && !location.equals(lastLocation)) {
            lastLocation = location;
            lastTimeMillis = System.currentTimeMillis();
        }
    }
}
