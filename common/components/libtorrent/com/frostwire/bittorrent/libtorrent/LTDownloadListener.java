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

import com.frostwire.jlibtorrent.TorrentAlertAdapter;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.SaveResumeDataAlert;

/**
 * @author gubatron
 * @author aldenml
 */
final class LTDownloadListener extends TorrentAlertAdapter {

    private static final long SAVE_RESUME_DATA_INTERVAL_MILLIS = 10000;

    private final LTDownload dl;

    private long lastTimeSavedResumeData;

    public LTDownloadListener(LTDownload dl) {
        super(dl.getTorrentHandle());
        this.dl = dl;

        this.lastTimeSavedResumeData = System.currentTimeMillis() + SAVE_RESUME_DATA_INTERVAL_MILLIS;
    }

    @Override
    public void onAlert(Alert<?> alert) {
        super.onAlert(alert);

        long now = System.currentTimeMillis();
        if (now - lastTimeSavedResumeData > SAVE_RESUME_DATA_INTERVAL_MILLIS) {
            lastTimeSavedResumeData = now;
            th.saveResumeData();
        }
    }

    @Override
    public void onSaveResumeData(SaveResumeDataAlert alert) {
//        entry d = alert.getResume_data();
//        byte[] arr = LibTorrent.vector2bytes(d.bencode());
//        System.out.println("Bencoded entry: " + arr.length);
    }
}
