/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.gui.library;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public abstract class DeviceTask implements Runnable {

    private List<OnChangedListener> listeners;

    private int progress;

    private boolean running;

    public DeviceTask() {
        listeners = new ArrayList<DeviceTask.OnChangedListener>();
        running = true;
    }

    public List<OnChangedListener> getOnChangedListeners() {
        return listeners;
    }

    public void addOnChangedListener(OnChangedListener listener) {
        listeners.add(listener);
    }

    public int getProgress() {
        return progress;
    }

    protected void setProgress(int progress) {
        // cast progress to [0..100]
        if (progress < 0 || progress > 100) {
            return;
        }
        progress = (progress < 0) ? 0 : progress;
        progress = (progress > 100) ? 100 : progress;

        this.progress = progress;
        onProgress(progress);
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
    }

    protected void onProgress(int progress) {
        for (int i = 0; i < listeners.size(); i++) { // no thread safe
            OnChangedListener listener = listeners.get(i);
            if (listener != null) {
                listener.onProgress(this, progress);
            }
        }
    }

    protected void onError(Throwable e) {
        for (int i = 0; i < listeners.size(); i++) { // no thread safe
            OnChangedListener listener = listeners.get(i);
            if (listener != null) {
                listener.onError(this, e);
            }
        }
    }

    public interface OnChangedListener {

        public void onProgress(DeviceTask task, int progress);

        public void onError(DeviceTask task, Throwable e);
    }
}
