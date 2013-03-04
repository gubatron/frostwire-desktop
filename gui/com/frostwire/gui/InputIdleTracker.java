/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.frostwire.gui;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.KeyEvent;

/**
 * Tracks mouse idleness when track() is invoked by checking if the mouse
 * changed position from the last time track() was invoked.
 * 
 * Keyboard tracking is done with a KeyEventDispatcher.
 * 
 * @author gubatron
 * @author aldenml
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
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo != null) {
            Point location = pointerInfo.getLocation();

            if (location != null && !location.equals(lastLocation)) {
                lastLocation = location;
                lastTimeMillis = System.currentTimeMillis();
            }
        }
    }
}
