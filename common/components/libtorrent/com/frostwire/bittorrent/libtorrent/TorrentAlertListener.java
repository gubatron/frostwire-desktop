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

import com.frostwire.jlibtorrent.LibTorrent;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.swig.alert;
import com.frostwire.jlibtorrent.swig.entry;
import com.frostwire.jlibtorrent.swig.save_resume_data_alert;
import com.frostwire.jlibtorrent.swig.torrent_alert;

/**
 * @author gubatron
 * @author aldenml
 */
final class TorrentAlertListener implements AlertListener {

    private static final long SAVE_RESUME_DATA_INTERVAL_MILLIS = 10000;

    private final LTDownload dl;
    private final TorrentHandle th;

    private long lastTimeSavedResumeData;

    public TorrentAlertListener(LTDownload dl) {
        this.dl = dl;
        this.th = dl.getTorrentHandle();

        this.lastTimeSavedResumeData = System.currentTimeMillis() + SAVE_RESUME_DATA_INTERVAL_MILLIS;
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
        if (a instanceof save_resume_data_alert) {
            onSaveResumeData((save_resume_data_alert) a);
            return; // I will explain why this return later
        }

        long now = System.currentTimeMillis();
        if (now - lastTimeSavedResumeData > SAVE_RESUME_DATA_INTERVAL_MILLIS) {
            lastTimeSavedResumeData = now;
            th.saveResumeData();
        }
    }

    private void onSaveResumeData(save_resume_data_alert a) {
        entry d = a.getResume_data();
        byte[] arr = LibTorrent.vector2bytes(d.bencode());
        System.out.println("Bencoded entry: " + arr.length);
    }
}
