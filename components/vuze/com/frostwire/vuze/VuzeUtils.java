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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfoSet;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.util.HashWrapper;

import com.frostwire.logging.Logger;
import com.frostwire.util.DirectoryUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeUtils {

    private static final Logger LOG = Logger.getLogger(VuzeUtils.class);

    private VuzeUtils() {
    }

    static Set<DiskManagerFileInfo> getFileInfoSet(DownloadManager dm, InfoSetQuery q) {

        Set<DiskManagerFileInfo> set = new HashSet<DiskManagerFileInfo>();
        DiskManagerFileInfoSet infoSet = dm.getDiskManagerFileInfoSet();
        for (DiskManagerFileInfo fileInfo : infoSet.getFiles()) {
            switch (q) {
            case SKIPPED:
                if (fileInfo.isSkipped()) {
                    set.add(fileInfo);
                }
                break;
            case NO_SKIPPED:
                if (!fileInfo.isSkipped()) {
                    set.add(fileInfo);
                }
                break;
            case ALL:
            default:
                set.add(fileInfo);
                break;
            }
        }

        return set;
    }

    public static Set<VuzeFileInfo> getFileInfoSet(VuzeDownloadManager dm, InfoSetQuery q) {
        Set<DiskManagerFileInfo> set = getFileInfoSet(dm.getDM(), q);
        Set<VuzeFileInfo> result = new HashSet<VuzeFileInfo>();

        for (DiskManagerFileInfo info : set) {
            result.add(new VuzeFileInfo(info));
        }

        return result;
    }

    public static void remove(VuzeDownloadManager dm, boolean deleteData) {
        TorrentUtil.removeDownloads(new DownloadManager[] { dm.getDM() }, null, deleteData);
    }

    public static void remove(byte[] hash, boolean deleteData) {
        GlobalManager gm = VuzeManager.getInstance().getGlobalManager();
        DownloadManager dm = gm.getDownloadManager(new HashWrapper(hash));
        if (dm != null) {
            TorrentUtil.removeDownloads(new DownloadManager[] { dm }, null, deleteData);
        }
    }

    public static void removeDownload(VuzeDownloadManager dm, boolean deleteTorrent, boolean deleteData) {
        ManagerUtils.asyncStopDelete(dm.getDM(), DownloadManager.STATE_STOPPED, deleteTorrent, deleteData, null);
    }

    public static Set<File> getIgnorableFiles() {
        Set<File> set = new HashSet<File>();

        for (DownloadManager dm : VuzeManager.getInstance().getGlobalManager().getDownloadManagers()) {
            set.addAll(getIgnorableFiles(dm));
        }

        return set;
    }

    private static Set<File> getIgnorableFiles(DownloadManager dm) {
        Set<File> set = new HashSet<File>();

        DiskManagerFileInfoSet infs = dm.getDiskManagerFileInfoSet();
        for (DiskManagerFileInfo inf : infs.getFiles()) {
            long length = inf.getLength();
            if (inf.getDownloaded() < length || inf.isSkipped()) {
                set.add(inf.getFile(false));
            }
        }

        return set;
    }

    /**
     * Deletes incomplete and skipped files.
     */
    static void finalCleanup(DownloadManager dm) {
        Set<File> toDelete = getIgnorableFiles(dm);

        for (File f : toDelete) {
            try {
                if (f.exists() && !f.delete()) {
                    LOG.info("Can't delete file: " + f);
                }
            } catch (Throwable e) {
                LOG.info("Can't delete file: " + f);
            }
        }

        DirectoryUtils.deleteEmptyDirectoryRecursive(dm.getSaveLocation());
    }

    public static enum InfoSetQuery {
        ALL, SKIPPED, NO_SKIPPED
    }
}
