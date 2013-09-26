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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.frostwire.util.HttpClient;
import com.frostwire.util.HttpClientFactory;
import com.frostwire.util.JsonUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class UXStats {

    private static final Logger LOG = LoggerFactory.getLogger(UXStats.class);

    private static final String HTTP_SERVER = "usage.frostwire.com";
    private static final int HTTP_TIMEOUT = 4000;

    private static final long HOUR_MILLIS = 1000 * 60 * 60;
    private static final int MIN_LOG_SIZE = 10;
    private static final int MAX_LOG_SIZE = 10000;

    private final List<UXAction> actions;
    private final HttpClient httpClient;

    private ExecutorService executor;
    private UXStatsContext context;
    private long time;

    private static final UXStats instance = new UXStats();

    public static UXStats instance() {
        return instance;
    }

    private UXStats() {
        this.actions = Collections.synchronizedList(new LinkedList<UXAction>());
        this.httpClient = HttpClientFactory.newDefaultInstance();

        this.executor = null;
        this.context = null;
        this.time = System.currentTimeMillis();
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public UXStatsContext getContext() {
        return context;
    }

    public void setContext(UXStatsContext context) {
        this.context = context;
    }

    /**
     * Important: This method is not thread-safe. That means it's only
     * meant to be used on the UI thread.
     * 
     * @param action
     */
    public void log(int action) {
        if (context != null) {
            if (actions.size() < MAX_LOG_SIZE) {
                actions.add(new UXAction(action, System.currentTimeMillis()));
            }

            if (isReadyToSend()) {
                sendData();
            }
        }
    }

    private boolean isReadyToSend() {
        return actions.size() >= MIN_LOG_SIZE && (time - System.currentTimeMillis() > HOUR_MILLIS);
    }

    private void sendData() {
        time = System.currentTimeMillis();

        SendDataRunnable r = new SendDataRunnable();
        if (executor != null) { // remember, not thread safe
            executor.submit(r);
        } else {
            new Thread(r, "UXStats-sendData").start();
        }
    }

    private String buildData() {
        UXData data = new UXData();
        data.guid = context.guid;
        data.os = context.os;
        data.fwversion = context.fwversion;
        data.time = time;
        data.actions = actions;

        return JsonUtils.toJson(data); // possible concurrent modification exception, not critical
    }

    private final class SendDataRunnable implements Runnable {

        @Override
        public void run() {
            try {
                String data = buildData();
                httpClient.post(HTTP_SERVER, HTTP_TIMEOUT, "FrostWire/UXStats", data);

                actions.clear();
            } catch (Throwable e) {
                LOG.error("Unable to send ux stats", e);
            }
        }
    }
}
