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

import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloader;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderCallBackInterface;

import com.frostwire.logging.Logger;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeTorrentDownloader {

    private static final Logger LOG = Logger.getLogger(VuzeTorrentDownloader.class);

    private final TorrentDownloader dl;

    private VuzeTorrentDownloadListener listener;

    public VuzeTorrentDownloader(TorrentDownloader dl) {
        this.dl = dl;
    }

    public VuzeTorrentDownloadListener getListener() {
        return listener;
    }

    public void setListener(VuzeTorrentDownloadListener listener) {
        this.listener = listener;
    }

    private class TorrentDownloaderListener implements TorrentDownloaderCallBackInterface {

        @Override
        public void TorrentDownloaderEvent(int state, TorrentDownloader inf) {
            if (listener != null) {
                try {
                    if (state == TorrentDownloader.STATE_FINISHED) {
                        listener.onFinished(VuzeTorrentDownloader.this);
                    }
                } catch (Throwable e) {
                    LOG.error("Error in client listener", e);
                }
            }
        }
    }
}
