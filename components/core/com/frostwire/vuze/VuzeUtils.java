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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfoSet;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.global.GlobalManagerDownloadRemovalVetoException;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.util.AERunnable;
import org.gudy.azureus2.core3.util.AsyncDispatcher;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.sharing.ShareManager;
import org.gudy.azureus2.plugins.sharing.ShareResource;
import org.gudy.azureus2.plugins.sharing.ShareResourceDir;
import org.gudy.azureus2.plugins.sharing.ShareResourceFile;
import org.gudy.azureus2.plugins.tracker.Tracker;
import org.gudy.azureus2.plugins.tracker.TrackerTorrent;
import org.gudy.azureus2.pluginsimpl.local.PluginCoreUtils;

import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.ui.UIFunctions;
import com.aelitis.azureus.ui.UIFunctionsManager;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeUtils {

    private VuzeUtils() {
    }

    public static void start(VuzeDownloadManager dm) {
        ManagerUtils.start(dm.getDM());
    }

    public static void stop(VuzeDownloadManager dm) {
        ManagerUtils.stop(dm.getDM());
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

    public static enum InfoSetQuery {
        ALL, SKIPPED, NO_SKIPPED
    }

    /// review this code

    private static AsyncDispatcher async = new AsyncDispatcher(2000);

    public static void removeDownload(VuzeDownloadManager downloadManager, boolean deleteTorrent, boolean deleteData) {
        removeDownload(downloadManager, deleteTorrent, deleteData, true);
    }

    public static void removeDownload(VuzeDownloadManager downloadManager, boolean deleteTorrent, boolean deleteData, boolean async) {
        if (async) {
            asyncStopDelete(downloadManager.getDM(), DownloadManager.STATE_STOPPED, deleteTorrent, deleteData, null);
        } else {
            blockingStopDelete(downloadManager.getDM(), DownloadManager.STATE_STOPPED, deleteTorrent, deleteData, null);
        }
    }

    public static void asyncStopDelete(final DownloadManager dm, final int stateAfterStopped, final boolean bDeleteTorrent, final boolean bDeleteData, final AERunnable deleteFailed) {

        async.dispatch(new AERunnable() {
            public void runSupport() {

                try {
                    // I would move the FLAG_DO_NOT_DELETE_DATA_ON_REMOVE even deeper
                    // but I fear what could possibly go wrong.
                    boolean reallyDeleteData = bDeleteData && !dm.getDownloadState().getFlag(Download.FLAG_DO_NOT_DELETE_DATA_ON_REMOVE);

                    dm.getGlobalManager().removeDownloadManager(dm, bDeleteTorrent, reallyDeleteData);
                } catch (GlobalManagerDownloadRemovalVetoException f) {

                    // see if we can delete a corresponding share as users frequently share
                    // stuff by mistake and then don't understand how to delete the share
                    // properly

                    try {
                        PluginInterface pi = AzureusCoreFactory.getSingleton().getPluginManager().getDefaultPluginInterface();

                        ShareManager sm = pi.getShareManager();

                        Tracker tracker = pi.getTracker();

                        ShareResource[] shares = sm.getShares();

                        TOTorrent torrent = dm.getTorrent();

                        byte[] target_hash = torrent.getHash();

                        for (ShareResource share : shares) {

                            int type = share.getType();

                            byte[] hash;

                            if (type == ShareResource.ST_DIR) {

                                hash = ((ShareResourceDir) share).getItem().getTorrent().getHash();

                            } else if (type == ShareResource.ST_FILE) {

                                hash = ((ShareResourceFile) share).getItem().getTorrent().getHash();

                            } else {

                                hash = null;
                            }

                            if (hash != null) {

                                if (Arrays.equals(target_hash, hash)) {

                                    try {
                                        dm.stopIt(DownloadManager.STATE_STOPPED, false, false);

                                    } catch (Throwable e) {
                                    }

                                    try {
                                        TrackerTorrent tracker_torrent = tracker.getTorrent(PluginCoreUtils.wrap(torrent));

                                        if (tracker_torrent != null) {

                                            tracker_torrent.stop();
                                        }
                                    } catch (Throwable e) {
                                    }

                                    share.delete();

                                    return;
                                }
                            }
                        }

                    } catch (Throwable e) {

                    }

                    if (!f.isSilent()) {
                        UIFunctionsManager.getUIFunctions().forceNotify(UIFunctions.STATUSICON_WARNING, MessageText.getString("globalmanager.download.remove.veto"), f.getMessage(), null, null, -1);

                        //Logger.log(new LogAlert(dm, false, "{globalmanager.download.remove.veto}", f));
                    }
                    if (deleteFailed != null) {
                        deleteFailed.runSupport();
                    }
                } catch (Throwable ex) {
                    Debug.printStackTrace(ex);
                    if (deleteFailed != null) {
                        deleteFailed.runSupport();
                    }
                }

                finalCleanup(dm);
            }
        });
    }

    public static void blockingStopDelete(final DownloadManager dm, final int stateAfterStopped, final boolean bDeleteTorrent, final boolean bDeleteData, final AERunnable deleteFailed) {

        try {
            // I would move the FLAG_DO_NOT_DELETE_DATA_ON_REMOVE even deeper
            // but I fear what could possibly go wrong.
            boolean reallyDeleteData = bDeleteData && !dm.getDownloadState().getFlag(Download.FLAG_DO_NOT_DELETE_DATA_ON_REMOVE);

            dm.getGlobalManager().removeDownloadManager(dm, bDeleteTorrent, reallyDeleteData);
        } catch (GlobalManagerDownloadRemovalVetoException f) {

            // see if we can delete a corresponding share as users frequently share
            // stuff by mistake and then don't understand how to delete the share
            // properly

            try {
                PluginInterface pi = AzureusCoreFactory.getSingleton().getPluginManager().getDefaultPluginInterface();

                ShareManager sm = pi.getShareManager();

                Tracker tracker = pi.getTracker();

                ShareResource[] shares = sm.getShares();

                TOTorrent torrent = dm.getTorrent();

                byte[] target_hash = torrent.getHash();

                for (ShareResource share : shares) {

                    int type = share.getType();

                    byte[] hash;

                    if (type == ShareResource.ST_DIR) {

                        hash = ((ShareResourceDir) share).getItem().getTorrent().getHash();

                    } else if (type == ShareResource.ST_FILE) {

                        hash = ((ShareResourceFile) share).getItem().getTorrent().getHash();

                    } else {

                        hash = null;
                    }

                    if (hash != null) {

                        if (Arrays.equals(target_hash, hash)) {

                            try {
                                dm.stopIt(DownloadManager.STATE_STOPPED, false, false);

                            } catch (Throwable e) {
                            }

                            try {
                                TrackerTorrent tracker_torrent = tracker.getTorrent(PluginCoreUtils.wrap(torrent));

                                if (tracker_torrent != null) {

                                    tracker_torrent.stop();
                                }
                            } catch (Throwable e) {
                            }

                            share.delete();

                            return;
                        }
                    }
                }

            } catch (Throwable e) {

            }

            if (!f.isSilent()) {
                UIFunctionsManager.getUIFunctions().forceNotify(UIFunctions.STATUSICON_WARNING, MessageText.getString("globalmanager.download.remove.veto"), f.getMessage(), null, null, -1);

                // Logger.log(new LogAlert(dm, false,
                // "{globalmanager.download.remove.veto}", f));
            }
            if (deleteFailed != null) {
                deleteFailed.runSupport();
            }
        } catch (Exception ex) {
            Debug.printStackTrace(ex);
            if (deleteFailed != null) {
                deleteFailed.runSupport();
            }
        }

        finalCleanup(dm);
    }

    /**
     * Deletes incomplete and skipped files.
     */
    private static void finalCleanup(DownloadManager dm) {
        //        Set<File> filesToDelete = getSkippedFiles(dm);
        //        filesToDelete.addAll(getIncompleteFiles(dm));
        //
        //        for (File f : filesToDelete) {
        //            try {
        //                if (f.exists() && !f.delete()) {
        //                    System.out.println("Can't delete file: " + f);
        //                }
        //            } catch (Throwable e) {
        //                System.out.println("Can't delete file: " + f);
        //            }
        //        }
        //
        //        FileUtils.deleteEmptyDirectoryRecursive(dm.getSaveLocation());
    }

    public static Set<File> getIncompleteFiles(DownloadManager dm) {
        Set<File> set = new HashSet<File>();

        DiskManagerFileInfoSet infoSet = dm.getDiskManagerFileInfoSet();
        for (DiskManagerFileInfo fileInfo : infoSet.getFiles()) {
            if (getDownloadPercent(fileInfo) < 100) {
                set.add(fileInfo.getFile(false));
            }
        }

        return set;
    }

    public static int getDownloadPercent(DiskManagerFileInfo fileInfo) {
        try {
            long length = fileInfo.getLength();
            if (length == 0 || fileInfo.getDownloaded() == length) {
                return 100;
            } else {
                return (int) (fileInfo.getDownloaded() * 100 / length);
            }
        } catch (Throwable e) {
            System.out.println("Error calculating download percent");
            return 0;
        }
    }

    public static Set<File> getIgnorableFiles() {
        Set<File> set = getIncompleteFiles();
        set.addAll(getSkipedFiles());
        return set;
    }

    public static Set<File> getIncompleteFiles() {
        Set<File> set = new HashSet<File>();

        //        if (!AzureusManager.isCreated()) {
        //            return set;
        //        }
        //
        //        List<?> dms = AzureusManager.instance().getGlobalManager().getDownloadManagers();
        //        for (Object obj : dms) {
        //            DownloadManager dm = (DownloadManager) obj;
        //            set.addAll(getIncompleteFiles(dm));
        //        }

        return set;
    }

    public static Set<File> getSkipedFiles() {
        Set<File> set = new HashSet<File>();

        //        if (!AzureusManager.isCreated()) {
        //            return set;
        //        }
        //
        //        List<?> dms = AzureusManager.instance().getGlobalManager().getDownloadManagers();
        //        for (Object obj : dms) {
        //            DownloadManager dm = (DownloadManager) obj;
        //            set.addAll(getSkippedFiles(dm));
        //        }

        return set;
    }

    public static boolean isComplete(DownloadManager dm) {
        /*
        if (!TorrentUtil.getSkippedFiles(dm).isEmpty()) {
            long downloaded = 0;
            long size = 0;
            for (DiskManagerFileInfo fileInfo : getNoSkippedFileInfoSet(dm)) {
                downloaded += fileInfo.getDownloaded();
                size += fileInfo.getLength();
            }
            return downloaded == size;
        } else {
            return dm.getStats().getDownloadCompleted(true) == 1000;
        }*/
        return dm.getAssumedComplete();
    }

    public static void stop(DownloadManager dm) {
        ManagerUtils.start(dm);
    }
}
