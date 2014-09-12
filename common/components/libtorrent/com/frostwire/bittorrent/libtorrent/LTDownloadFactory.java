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

import com.frostwire.bittorrent.BTDownload;
import com.frostwire.bittorrent.BTDownloadFactory;
import com.frostwire.jlibtorrent.Session;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.swig.alert;
import com.frostwire.jlibtorrent.swig.torrent_alert;

import java.io.File;

/**
 * @author gubatron
 * @author aldenml
 */
public final class LTDownloadFactory extends BTDownloadFactory {

    @Override
    public BTDownload create(File torrent, File saveDir) {
        LTEngine e = LTEngine.getInstance();

        Session s = e.getSession();
        TorrentHandle th = s.addTorrent(torrent, saveDir);
        LTDownload dl = new LTDownload(th);

        e.addListener(new TorrentAlertListener(dl));

        return dl;
    }

    private static class TorrentAlertListener implements AlertListener {

        private final LTDownload dl;
        private final TorrentHandle th;

        public TorrentAlertListener(LTDownload dl) {
            this.dl = dl;
            this.th = dl.getTorrentHandle();
        }

        @Override
        public boolean accept(alert a) {
            if (!(a instanceof torrent_alert)) {
                return false;
            }

            torrent_alert ta = (torrent_alert) a;

            return th.getSwig().op_eq(ta.getHandle());
        }

        @Override
        public void onAlert(alert a) {

        }
    }
}
