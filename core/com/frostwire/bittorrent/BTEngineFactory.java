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

package com.frostwire.bittorrent;

import com.frostwire.bittorrent.libtorrent.LTEngine;
import com.limegroup.gnutella.settings.SharingSettings;
import org.limewire.util.CommonUtils;

import java.io.File;

/**
 * @author gubatron
 * @author aldenml
 */
public final class BTEngineFactory {

    private static BTEngine instance;

    private BTEngineFactory() {
    }

    public static BTEngine getInstance() {
        if (instance == null) {
            instance = LTEngine.getInstance();
            setup(instance);
        }
        return instance;
    }

    private static void setup(BTEngine engine) {

        SharingSettings.initTorrentDataDirSetting();
        SharingSettings.initTorrentsDirSetting();

        engine.setHome(buildHome());
    }

    private static File buildHome() {
        File path = new File(CommonUtils.getUserSettingsDir() + File.separator + "libtorrent" + File.separator);
        if (!path.exists()) {
            path.mkdirs();
        }
        return path;
    }
}
// TODO:BITTORRENT
/*
public final class AzureusStarter {




    public static void revertToDefaultConfiguration() {
        COConfigurationManager.resetToDefaults();
        autoAdjustBittorrentSpeed();
    }

    public static void autoAdjustBittorrentSpeed() {
        if (COConfigurationManager.getBooleanParameter("Auto Adjust Transfer Defaults")) {

            int up_limit_bytes_per_sec = 0;//getEstimatedUploadCapacityBytesPerSec().getBytesPerSec();
            //int down_limit_bytes_per_sec    = 0;//getEstimatedDownloadCapacityBytesPerSec().getBytesPerSec();

            int up_kbs = up_limit_bytes_per_sec / 1024;

            final int[][] settings = {

                    { 56, 2, 20, 40 }, // 56 k/bit
                    { 96, 3, 30, 60 }, { 128, 3, 40, 80 }, { 192, 4, 50, 100 }, // currently we don't go lower than this
                    { 256, 4, 60, 200 }, { 512, 5, 70, 300 }, { 1024, 6, 80, 400 }, // 1Mbit
                    { 2 * 1024, 8, 90, 500 }, { 5 * 1024, 10, 100, 600 }, { 10 * 1024, 20, 110, 750 }, // 10Mbit
                    { 20 * 1024, 30, 120, 900 }, { 50 * 1024, 40, 130, 1100 }, { 100 * 1024, 50, 140, 1300 }, { -1, 60, 150, 1500 }, };

            int[] selected = settings[settings.length - 1];

            // note, we start from 3 to avoid over-restricting things when we don't have
            // a reasonable speed estimate

            for (int i = 3; i < settings.length; i++) {

                int[] setting = settings[i];

                int line_kilobit_sec = setting[0];

                // convert to upload kbyte/sec assuming 80% achieved

                int limit = (line_kilobit_sec / 8) * 4 / 5;

                if (up_kbs <= limit) {

                    selected = setting;

                    break;
                }
            }

            int upload_slots = selected[1];
            int connections_torrent = selected[2];
            int connections_global = selected[3];

            if (upload_slots != COConfigurationManager.getIntParameter("Max Uploads")) {

                COConfigurationManager.setParameter("Max Uploads", upload_slots);
                COConfigurationManager.setParameter("Max Uploads Seeding", upload_slots);
            }

            if (connections_torrent != COConfigurationManager.getIntParameter("Max.Peer.Connections.Per.Torrent")) {

                COConfigurationManager.setParameter("Max.Peer.Connections.Per.Torrent", connections_torrent);

                COConfigurationManager.setParameter("Max.Peer.Connections.Per.Torrent.When.Seeding", connections_torrent / 2);
            }

            if (connections_global != COConfigurationManager.getIntParameter("Max.Peer.Connections.Total")) {

                COConfigurationManager.setParameter("Max.Peer.Connections.Total", connections_global);
            }
        }
    }

}
 */
