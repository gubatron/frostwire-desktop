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

package com.frostwire.vuze;

import java.util.List;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.util.AERunStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aelitis.azureus.core.AzureusCore;
import com.frostwire.util.Condition;

/**
 * Class to initialize the azureus core.
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeManager {

    private static final Logger LOG = LoggerFactory.getLogger(VuzeManager.class);

    private final AzureusCore core;

    public VuzeManager(AzureusCore core) {
        this.core = core;

        new ActivityMonitor().start();
    }

    public AzureusCore getCore() {
        return core;
    }

    public boolean isDHTSleeping() {
        return AERunStateHandler.isDHTSleeping();
    }

    public void setDHTSleeping(boolean sleeping) {
        if (sleeping != isDHTSleeping()) {
            long rm = AERunStateHandler.getResourceMode();

            if (sleeping) {
                AERunStateHandler.setResourceMode(rm | AERunStateHandler.RS_DHT_SLEEPING);
            } else {
                AERunStateHandler.setResourceMode(rm & ~AERunStateHandler.RS_DHT_SLEEPING);
            }
        }
    }

    private final class ActivityMonitor extends Thread {

        private boolean running;

        public ActivityMonitor() {
            super("Vuze Activity Monitor");

            this.setDaemon(true);

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

                @Override
                public void run() {
                    running = false;
                }
            }));
        }

        @Override
        public void run() {
            running = true;

            while (running) {
                sleep();
                checkActivity();
            }
        }

        private void sleep() {
            try {
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
            }
        }

        private void checkActivity() {
            try {
                System.out.println("checkActivity");
                if (isDownloading()) {
                    setDHTSleeping(false);
                    System.out.println("setDHTSleeping(false)");
                } else {
                    setDHTSleeping(true);
                    System.out.println("setDHTSleeping(true)");
                }
            } catch (Throwable e) {
                LOG.error("Error checking vuze activity", e);
            }
        }

        private boolean isDownloading() {
            List<DownloadManager> dms = core.getGlobalManager().getDownloadManagers();

            boolean downloading = false;

            for (DownloadManager dm : dms) {

                int state = dm.getState();

                if (Condition.in(state, DownloadManager.STATE_ALLOCATING, DownloadManager.STATE_DOWNLOADING, DownloadManager.STATE_WAITING)) {
                    downloading = true;
                }
            }

            return downloading;
        }
    }
}
