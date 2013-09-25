/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2013, FrostWire(R). All rights reserved.
 
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

package com.frostwire.uxstats;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class UXStats {

    private static final long HOUR_MILLIS = 1000 * 60 * 60;
    private static final int MAX_LOG_SIZE = 10000;

    private final String guid;
    private final List<UXAction> actions;

    private ExecutorService executor;
    private boolean enabled;
    private long time;

    private static final UXStats instance = new UXStats();

    public static UXStats instance() {
        return instance;
    }

    private UXStats() {
        this.guid = UUID.randomUUID().toString();
        this.actions = new LinkedList<UXAction>();

        this.executor = null;
        this.enabled = false;
        this.time = System.currentTimeMillis();
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enable) {
        this.enabled = enable;
    }

    /**
     * Important: This method is not thread-safe. That means it's only
     * meant to be used on the UI thread.
     * 
     * @param action
     */
    public void log(int action) {
        if (actions.size() < MAX_LOG_SIZE) {
            actions.add(new UXAction(action, System.currentTimeMillis()));
        }

        sendData();
    }

    private void sendData() {
        long now = System.currentTimeMillis();
        if (time - System.currentTimeMillis() > HOUR_MILLIS) {
            time = now;

            SendDataRunnable r = new SendDataRunnable();
            if (executor != null) { // remember, not thread safe
                executor.submit(r);
            } else {
                new Thread(r, "UXStats-sendData").start();
            }
        }
    }

    private final class SendDataRunnable implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub

        }
    }
}
