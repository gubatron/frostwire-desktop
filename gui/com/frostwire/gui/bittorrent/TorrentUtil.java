/*
 * Created on 9 Jul 2007
 * Created by Allan Crooks
 * Copyright (C) 2007 Aelitis, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * AELITIS, SAS au capital de 46,603.30 euros
 * 8 Allee Lenotre, La Grille Royale, 78600 Le Mesnil le Roi, France.
 */
/*
 * File    : ManagerUtils.java
 * Created : 7 d�c. 2003}
 * By      : Olivier
 *
 * Copyright (C) 2004, 2005, 2006 Aelitis SAS, All rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * AELITIS, SAS au capital de 46,603.30 euros,
 * 8 Allee Lenotre, La Grille Royale, 78600 Le Mesnil le Roi, France.
 */
package com.frostwire.gui.bittorrent;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfoSet;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.global.GlobalManagerDownloadRemovalVetoException;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentAnnounceURLGroup;
import org.gudy.azureus2.core3.torrent.TOTorrentAnnounceURLSet;
import org.gudy.azureus2.core3.util.AERunnable;
import org.gudy.azureus2.core3.util.AsyncDispatcher;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.UrlUtils;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.sharing.ShareManager;
import org.gudy.azureus2.plugins.sharing.ShareResource;
import org.gudy.azureus2.plugins.sharing.ShareResourceDir;
import org.gudy.azureus2.plugins.sharing.ShareResourceFile;
import org.gudy.azureus2.plugins.tracker.Tracker;
import org.gudy.azureus2.plugins.tracker.TrackerTorrent;
import org.gudy.azureus2.pluginsimpl.local.PluginCoreUtils;
import org.limewire.util.FileUtils;
import org.limewire.util.NetworkUtils;
import org.limewire.util.StringUtils;

import com.aelitis.azureus.core.AzureusCoreFactory;
import com.aelitis.azureus.core.networkmanager.impl.tcp.TCPNetworkManager;
import com.aelitis.azureus.ui.UIFunctions;
import com.aelitis.azureus.ui.UIFunctionsManager;
import com.aelitis.azureus.ui.selectedcontent.SelectedContent;
import com.frostwire.AzureusStarter;
import com.frostwire.logging.Logger;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.settings.iTunesImportSettings;

public final class TorrentUtil {

    private static final Logger LOG = Logger.getLogger(TorrentUtil.class);

    private static AsyncDispatcher async = new AsyncDispatcher(2000);

    public static boolean shouldStopGroup(Object[] datasources) {
        DownloadManager[] dms = toDMS(datasources);
        if (dms.length == 0) {
            return true;
        }
        for (DownloadManager dm : dms) {
            int state = dm.getState();
            boolean stopped = state == DownloadManager.STATE_STOPPED || state == DownloadManager.STATE_STOPPING;
            if (!stopped) {
                return true;
            }
        }
        return false;
    }

    public static void stopOrStartDataSources(Object[] datasources) {
        DownloadManager[] dms = toDMS(datasources);
        if (dms.length == 0) {
            return;
        }
        boolean doStop = shouldStopGroup(dms);
        if (doStop) {
            stopDataSources(datasources);
        } else {
            queueDataSources(datasources, true);
        }
    }

    public static void stopDataSources(Object[] datasources) {
        DownloadManager[] dms = toDMS(datasources);
        for (DownloadManager dm : dms) {
            stop(dm);
        }
    }

    public static void queueDataSources(Object[] datasources, boolean startStoppedParents) {
        DownloadManager[] dms = toDMS(datasources);
        for (DownloadManager dm : dms) {
            queue(dm);
        }
    }

    public static void removeDownload(DownloadManager downloadManager, boolean deleteTorrent, boolean deleteData) {
        removeDownload(downloadManager, deleteTorrent, deleteData, true);
    }

    public static void removeDownload(DownloadManager downloadManager, boolean deleteTorrent, boolean deleteData, boolean async) {
        if (async) {
            asyncStopDelete(downloadManager, DownloadManager.STATE_STOPPED, deleteTorrent, deleteData, null);
        } else {
            blockingStopDelete(downloadManager, DownloadManager.STATE_STOPPED, deleteTorrent, deleteData, null);
        }
    }

    /** tries to get creation time, if it can't it returns -1 */
    private static long getFileCreationTime(File f) {
        long result = -1;
        
        try {
            BasicFileAttributeView fileAttributeView = Files.getFileAttributeView(f.toPath(), BasicFileAttributeView.class);
            FileTime creationTime = fileAttributeView.readAttributes().creationTime();
            result = creationTime.toMillis();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("just printing exception, returning -1");
        }
        
        return result;
    }
    
    /** Deletes incomplete files and the save location from the itunes import settings */
    private static void finalCleanup(DownloadManager downloadManager, long timeDownloadManagerStarted) {
        Set<File> filesToDelete = getSkippedFiles(downloadManager);
        
        for (File f : filesToDelete) {
            try {
                
                if (isSkippedFileComplete(f, downloadManager)) {
                    
                    long fileCreationModificationTime = getFileCreationTime(f);
                    
                    //fallback to modified time
                    if (fileCreationModificationTime == -1) {
                        fileCreationModificationTime = f.lastModified();
                    }

                    //keep files from older sessions, or if you don't know when this
                    //download manager started.
                    if (fileCreationModificationTime < timeDownloadManagerStarted || 
                        timeDownloadManagerStarted == -1) {
                        continue;
                    }
                }
                
                if (f.exists() && !f.delete()) {
                    System.out.println("Can't delete file: " + f);
                }
            } catch (Exception e) {
                System.out.println("Can't delete file: " + f + ", ex: " + e.getMessage());
            }
        }
        FileUtils.deleteEmptyDirectoryRecursive(downloadManager.getSaveLocation());
        iTunesImportSettings.IMPORT_FILES.remove(downloadManager.getSaveLocation());
    }

    /**
     * Check if the given file, even though marked as skipped was downloaded in its entirety,
     * so that we don't delete it by accident during finalCleanup.
     * @param file
     * @param downloadManager
     * @return
     */
    private static boolean isSkippedFileComplete(File file, DownloadManager downloadManager) {
        DiskManagerFileInfoSet infoSet = downloadManager.getDiskManagerFileInfoSet();

        //search for the given file on the disk manager for this download manager
        for (DiskManagerFileInfo fileInfo : infoSet.getFiles()) {
            if (fileInfo.isSkipped()) {
                File f = fileInfo.getFile(false);

                //1. found the path of our file in here.
                if (f.getAbsolutePath().equalsIgnoreCase(file.getAbsolutePath())) {
                    return fileInfo.getLength() == file.length();
                }
            }
        }
        return false;
    }

    public static Set<File> getSkipedFiles() {
        Set<File> set = new HashSet<File>();
        if (AzureusStarter.isAzureusCoreStarted()) {
            List<?> dms = AzureusStarter.getAzureusCore().getGlobalManager().getDownloadManagers();
            for (Object obj : dms) {
                DownloadManager dm = (DownloadManager) obj;
                set.addAll(getSkippedFiles(dm));
            }
        }
        return set;
    }

    public static Set<File> getSkippedFiles(DownloadManager dm) {
        Set<File> set = new HashSet<File>();
        DiskManagerFileInfoSet infoSet = dm.getDiskManagerFileInfoSet();
        for (DiskManagerFileInfo fileInfo : infoSet.getFiles()) {
            try {
                if (fileInfo.isSkipped()) {
                    set.add(fileInfo.getFile(false));
                }
            } catch (Throwable e) {
                LOG.error("Error getting file information", e);
            }
        }
        return set;
    }

    public static Set<DiskManagerFileInfo> getNoSkippedFileInfoSet(DownloadManager dm) {
        Set<DiskManagerFileInfo> set = new HashSet<DiskManagerFileInfo>();
        DiskManagerFileInfoSet infoSet = dm.getDiskManagerFileInfoSet();
        for (DiskManagerFileInfo fileInfo : infoSet.getFiles()) {
            if (!fileInfo.isSkipped()) {
                set.add(fileInfo);
            }
        }
        return set;
    }

    public static DownloadManager getDownloadManager(File f) {
    	if (AzureusStarter.isAzureusCoreStarted()) {
            List<?> dms = AzureusStarter.getAzureusCore().getGlobalManager().getDownloadManagers();
            for (Object obj : dms) {
                DownloadManager dm = (DownloadManager) obj;

                DiskManagerFileInfoSet infoSet = dm.getDiskManagerFileInfoSet();
                for (DiskManagerFileInfo fileInfo : infoSet.getFiles()) {
                    try {
                    	
                    	if (f.equals(fileInfo.getFile(false)) ) {
                    		return dm;
                    	}
                    	
                    } catch (Throwable e) {
                        LOG.error("Error getting file information", e);
                    }
                }
            }
        }
    	return null;
    }
    
    public static Set<File> getIncompleteFiles() {
        Set<File> set = new HashSet<File>();

        if (AzureusStarter.isAzureusCoreStarted()) {
            List<?> dms = AzureusStarter.getAzureusCore().getGlobalManager().getDownloadManagers();
            for (Object obj : dms) {
                DownloadManager dm = (DownloadManager) obj;

                DiskManagerFileInfoSet infoSet = dm.getDiskManagerFileInfoSet();
                for (DiskManagerFileInfo fileInfo : infoSet.getFiles()) {
                    try {
                        if (getDownloadPercent(fileInfo) < 100) {
                            set.add(fileInfo.getFile(false));
                        }
                    } catch (Throwable e) {
                        LOG.error("Error getting file information", e);
                    }
                }
            }
        }

        return set;
    }

    public static int getDownloadPercent(DiskManagerFileInfo fileInfo) {
        long length = fileInfo.getLength();
        if (length == 0 || fileInfo.getDownloaded() == length) {
            return 100;
        } else {
            return (int) (fileInfo.getDownloaded() * 100 / length);
        }
    }

    public static boolean isStartable(DownloadManager dm) {
        if (dm == null)
            return false;
        int state = dm.getState();
        if (state != DownloadManager.STATE_STOPPED) {
            return false;
        }
        return true;
    }

    public static boolean isStopable(DownloadManager dm) {
        if (dm == null)
            return false;
        int state = dm.getState();
        if (state == DownloadManager.STATE_STOPPED || state == DownloadManager.STATE_STOPPING) {
            return false;
        }
        return true;
    }

    public static boolean isStopped(DownloadManager dm) {
        if (dm == null)
            return false;
        int state = dm.getState();
        if (state == DownloadManager.STATE_STOPPED || state == DownloadManager.STATE_ERROR) {
            return true;
        }
        return false;
    }

    public static boolean isForceStartable(DownloadManager dm) {
        if (dm == null) {
            return false;
        }

        int state = dm.getState();

        if (state != DownloadManager.STATE_STOPPED && state != DownloadManager.STATE_QUEUED && state != DownloadManager.STATE_SEEDING && state != DownloadManager.STATE_DOWNLOADING) {

            return (false);
        }

        return (true);
    }

    public static void asyncStopDelete(final DownloadManager dm, final int stateAfterStopped, final boolean bDeleteTorrent, final boolean bDeleteData, final AERunnable deleteFailed) {

        async.dispatch(new AERunnable() {
            public void runSupport() {
                long timeStarted = dm.getStats().getTimeStarted();
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

                        //Logger.log(new LogAlert(dm, false,
                        //      "{globalmanager.download.remove.veto}", f));
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

                finalCleanup(dm, timeStarted);
            }
        });
    }

    public static void blockingStopDelete(final DownloadManager dm, final int stateAfterStopped, final boolean bDeleteTorrent, final boolean bDeleteData, final AERunnable deleteFailed) {
        long timeStarted = dm.getStats().getTimeStarted();
        
        try {
            // I would move the FLAG_DO_NOT_DELETE_DATA_ON_REMOVE even deeper
            // but I fear what could possibly go wrong.
            boolean reallyDeleteData = bDeleteData && !dm.getDownloadState().getFlag(Download.FLAG_DO_NOT_DELETE_DATA_ON_REMOVE);

            dm.getGlobalManager().removeDownloadManager(dm, bDeleteTorrent, reallyDeleteData);
        } catch (GlobalManagerDownloadRemovalVetoException f) {

            // see if we can delete a corresponding share as users frequently
            // share
            // stuff by mistake and then don't understand how to delete the
            // share
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

        finalCleanup(dm, timeStarted);
    }

    private static DownloadManager[] toDMS(Object[] objects) {
        int count = 0;
        DownloadManager[] result = new DownloadManager[objects.length];
        for (Object object : objects) {
            if (object instanceof DownloadManager) {
                DownloadManager dm = (DownloadManager) object;
                result[count++] = dm;
            } else if (object instanceof SelectedContent) {
                SelectedContent sc = (SelectedContent) object;
                if (sc.getFileIndex() == -1 && sc.getDownloadManager() != null) {
                    result[count++] = sc.getDownloadManager();
                }
            }
        }
        DownloadManager[] resultTrim = new DownloadManager[count];
        System.arraycopy(result, 0, resultTrim, 0, count);
        return resultTrim;
    }

    public static void stop(DownloadManager dm) {
        stop(dm, DownloadManager.STATE_STOPPED);
    }

    public static void stop(final DownloadManager dm, final int stateAfterStopped) {
        if (dm == null) {
            return;
        }

        int state = dm.getState();

        if (state == DownloadManager.STATE_STOPPED || state == DownloadManager.STATE_STOPPING || state == stateAfterStopped) {
            return;
        }

        asyncStop(dm, stateAfterStopped);
    }

    public static void asyncStop(final DownloadManager dm, final int stateAfterStopped) {
        async.dispatch(new AERunnable() {
            public void runSupport() {
                long timeStarted = dm.getStats().getTimeStarted(); //do this before stopping, otherwise returns -1.
                dm.stopIt(stateAfterStopped, false, false);
                
                if (isHandpicked(dm) && 
                    (!SharingSettings.SEED_FINISHED_TORRENTS.getValue() || !SharingSettings.SEED_HANDPICKED_TORRENT_FILES.getValue()) &&
                    dm.getAssumedComplete()) {
                    finalCleanup(dm, timeStarted);
                }
            }
        });
    }

    public static void queue(DownloadManager dm) {
        if (dm != null) {
            if (dm.getState() == DownloadManager.STATE_STOPPED) {

                dm.setStateQueued();

                /* parg - removed this - why would we want to effectively stop + restart
                 * torrents that are running? This is what happens if the code is left in.
                 * e.g. select two torrents, one stopped and one downloading, then hit "queue"
                 
                 }else if ( dm.getState() == DownloadManager.STATE_DOWNLOADING || 
                        dm.getState() == DownloadManager.STATE_SEEDING) {
                
                    stop(dm,panel,DownloadManager.STATE_QUEUED);
                */
            }
        }
    }

    public static void start(DownloadManager dm) {
        if (dm != null && dm.getState() == DownloadManager.STATE_STOPPED) {

            //dm.setStateWaiting();
            dm.initialize();
        }
    }

    public static String getMagnet(byte[] hash) {
        return "magnet:?xt=urn:btih:" + hashToString(hash);
    }

    public static String getMagnet(String hash) {
        return "magnet:?xt=urn:btih:" + hash;
    }

    public static String getMagnetURLParameters(TOTorrent torrent) {
        StringBuilder sb = new StringBuilder();
        //dn
        if (StringUtils.isNullOrEmpty(torrent.getUTF8Name())) {
            sb.append("dn=" + UrlUtils.encode(new String(torrent.getName())));
        } else {
            sb.append("dn=" + UrlUtils.encode(torrent.getUTF8Name()));
        }

        TOTorrentAnnounceURLGroup announceURLGroup = torrent.getAnnounceURLGroup();
        TOTorrentAnnounceURLSet[] announceURLSets = announceURLGroup.getAnnounceURLSets();

        for (TOTorrentAnnounceURLSet set : announceURLSets) {
            URL[] announceURLs = set.getAnnounceURLs();
            for (URL url : announceURLs) {
                sb.append("&tr=");
                sb.append(UrlUtils.encode(url.toString()));
            }
        }

        if (torrent.getAnnounceURL() != null) {
            sb.append("&tr=");
            sb.append(UrlUtils.encode(torrent.getAnnounceURL().toString()));
        }

        //iipp = internal ip port, for lan
        try {
            String localAddress = NetworkUtils.getLocalAddress().getHostAddress();
            int localPort = TCPNetworkManager.getSingleton().getTCPListeningPortNumber();

            if (localPort != -1) {
                sb.append("&iipp=");
                sb.append(NetworkUtils.convertIPPortToHex(localAddress, localPort));
            }

        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return sb.toString();
    }

    public static String hashToString(byte[] hash) {
        String hex = "";
        for (int i = 0; i < hash.length; i++) {
            String t = Integer.toHexString(hash[i] & 0xFF);
            if (t.length() < 2) {
                t = "0" + t;
            }
            hex += t;
        }

        return hex;
    }

    public static Set<File> getIgnorableFiles() {
        Set<File> set = TorrentUtil.getIncompleteFiles();
        set.addAll(TorrentUtil.getSkipedFiles());
        return set;
    }
    
    public static boolean isHandpicked(DownloadManager dm) {
        Set<File> skippedFiles = getSkippedFiles(dm);
        return skippedFiles.size() > 0;
    }
}
