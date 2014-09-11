/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
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

package com.frostwire.bittorrent.libtorrent;

import com.frostwire.jlibtorrent.Session;
import com.frostwire.jlibtorrent.swig.alert;
import com.frostwire.logging.Logger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author gubatron
 * @author aldenml
 */
public final class LTEngine {

    private static final Logger LOG = Logger.getLogger(LTEngine.class);

    private static final long ALERTS_LOOP_WAIT_MILLIS = 500;

    private final Session session;

    private boolean running;
    private List<AlertListener<?>> listeners;

    public LTEngine() {
        this.session = new Session();

        this.running = true;
        this.listeners = Collections.synchronizedList(new LinkedList<AlertListener<?>>());

        addEngineListener();
        alertsLoop();
    }

    private static class Loader {
        static LTEngine INSTANCE = new LTEngine();
    }

    public static LTEngine getInstance() {
        return Loader.INSTANCE;
    }

    Session getSession() {
        return session;
    }

    void addListener(AlertListener listener) {
        this.listeners.add(listener);
    }

    void removeListener(AlertListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    protected void finalize() throws Throwable {
        this.running = false;
        super.finalize();
    }

    private void addEngineListener() {
        // simple log
        addListener(new AlertListener() {
            @Override
            public boolean accept(alert a) {
                return true;
            }

            @Override
            public void onAlert(alert a) {
                //LOG.info(a.message());
            }
        });
    }

    private void alertsLoop() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (running) {
                    List<alert> alerts = session.waitForAlerts(ALERTS_LOOP_WAIT_MILLIS);

                    for (alert a : alerts) {
                        synchronized (listeners) {
                            for (AlertListener l : listeners) {
                                try {
                                    if (l.accept(a)) {
                                        l.onAlert(a);
                                    }
                                } catch (Throwable e) {
                                    LOG.warn("Error calling alert listener", e);
                                }
                            }
                        }
                    }
                }
            }
        };

        Thread t = new Thread(r, "LTEngine-alertsLoop");
        t.setDaemon(true);
        t.start();
    }
}
