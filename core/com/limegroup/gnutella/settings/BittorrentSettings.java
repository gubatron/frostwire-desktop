/*
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

package com.limegroup.gnutella.settings;

import org.limewire.setting.BooleanSetting;
import org.limewire.setting.IntSetting;
import org.limewire.setting.ProbabilisticBooleanSetting;

/**
 * Bittorrent settings
 */
public class BittorrentSettings extends LimeProps {

    private BittorrentSettings() {
        // empty constructor
    }

    /**
     * Setting whether LimeWire should manage the BT settings automatically.
     */
    public static BooleanSetting AUTOMATIC_SETTINGS = FACTORY.createBooleanSetting("BT_AUTOMATIC_SETTINGS", true);

    /**
     * minimum tracker reask delay in seconds that we will use
     */
    public static IntSetting TRACKER_MIN_REASK_INTERVAL = FACTORY.createIntSetting("TRACKER_MIN_REASK_INTERVAL", 5 * 60);

    /**
     * maximum tracker reask delay in seconds
     */
    public static IntSetting TRACKER_MAX_REASK_INTERVAL = FACTORY.createIntSetting("TRACKER_MAX_REASK_INTERVAL", 2 * 60 * 60);

    /** maximum simultaneous torrent downloads */
    public static IntSetting TORRENT_MAX_ACTIVE_DOWNLOADS = FACTORY.createIntSetting("TORRENT_MAX_ACTIVE_DOWNLOADS", 10);

    /**
     * maximum uploads per torrent
     */
    public static IntSetting TORRENT_MAX_UPLOADS = FACTORY.createIntSetting("TORRENT_MAX_UPLOADS", 6);

    /**
     * the number of uploads to allow to random hosts, ignoring tit-for-tat
     */
    public static IntSetting TORRENT_MIN_UPLOADS = FACTORY.createIntSetting("TORRENT_MIN_UPLOADS", 4);

    /**
     * Whether to flush written blocks to disk before verifying them
     */
    public static BooleanSetting TORRENT_FLUSH_VERIRY = FACTORY.createBooleanSetting("TORRENT_FLUSH_VERIFY", false);

    /**
     * Whether to use memory mapped files for disk access.
     */
    public static BooleanSetting TORRENT_USE_MMAP = FACTORY.createBooleanSetting("TORRENT_USE_MMAP", false);

    /**
     * Whether to automatically start torrents for .torrent files downloaded
     * through LimeWire.
     */
    public static BooleanSetting TORRENT_AUTO_START = FACTORY.createBooleanSetting("TORRENT_AUTO_START", true);

    /**
     * Whether to report Disk problems to the bug server
     */
    public static ProbabilisticBooleanSetting REPORT_DISK_PROBLEMS = FACTORY.createRemoteProbabilisticBooleanSetting("REPORT_BT_DISK_PROBLEMS", 0f, "BTSettings.reportDiskProblems", 0f, 1f);

    /**
     * Records what was the last sorting order of the sort column for the transfer manager.
     * false -> Descending
     * true -> Ascending
     */
    public static BooleanSetting BTMEDIATOR_COLUMN_SORT_ORDER = FACTORY.createBooleanSetting("BTMEDIATOR_COLUMN_SORT_ORDER", true);

    /**
     * Records what was the last column you used to sort the transfers table.
     */
    public static IntSetting BTMEDIATOR_COLUMN_SORT_INDEX = FACTORY.createIntSetting("BTMEDIATOR_COLUMN_SORT_INDEX", -1);
}
