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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerInitialisationAdapter;
import org.gudy.azureus2.core3.download.impl.DownloadManagerAdapter;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.util.HashWrapper;

import com.frostwire.logging.Logger;
import com.frostwire.torrent.TOTorrent;
import com.frostwire.torrent.TOTorrentException;
import com.frostwire.torrent.TorrentUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeDownloadFactory {

    private static final Logger LOG = Logger.getLogger(VuzeDownloadFactory.class);

    private VuzeDownloadFactory() {
    }

    public static VuzeDownloadManager create(String torrent, final Set<String> selection, String saveDir, VuzeDownloadListener listener) throws IOException {
        // this args checking is critical
        if (torrent == null) {
            throw new IllegalArgumentException("Torrent file path can't be null");
        }
        if (saveDir == null) {
            throw new IllegalArgumentException("Torrent data save dir can't be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Download manager listener can't be null");
        }

        GlobalManager gm = VuzeManager.getInstance().getGlobalManager();

        DownloadManager dm = findDM(gm, torrent);
        VuzeDownloadManager vdm = null;

        if (dm == null) { // new download
            dm = gm.addDownloadManager(torrent, null, saveDir, null, DownloadManager.STATE_WAITING, true, false, new DownloadManagerInitialisationAdapter() {
                @Override
                public void initialised(DownloadManager manager, boolean for_seeding) {
                    setupPartialSelection(manager, selection);
                }

                @Override
                public int getActions() {
                    return ACT_NONE;
                }
            });

            vdm = new VuzeDownloadManager(dm);
            setupListener(vdm, listener);

        } else { // modify the existing one
            setupPartialSelection(dm, selection);

            vdm = VuzeDownloadManager.refreshData(dm);

            if (dm.getState() == DownloadManager.STATE_STOPPED) {
                dm.initialize();
            }
        }

        return vdm;
    }

    private static DownloadManager findDM(GlobalManager gm, String torrent) throws IOException {
        InputStream is = null;

        try {
            // using fork api for actual reading

            is = new FileInputStream(torrent);

            TOTorrent t = TorrentUtils.readFromBEncodedInputStream(is);

            return gm.getDownloadManager(new HashWrapper(t.getHash()));

        } catch (TOTorrentException e) {
            throw new IOException("Unable to read the torrent", e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private static void setupListener(final VuzeDownloadManager dm, final VuzeDownloadListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Download manager listener can't be null");
        }

        dm.getDM().addListener(new DownloadManagerAdapter() {
            @Override
            public void stateChanged(DownloadManager manager, int state) {
                if (state == DownloadManager.STATE_READY) {
                    manager.startDownload();
                } else {
                    if (listener != null) {
                        try {
                            listener.stateChanged(dm, state);
                        } catch (Throwable e) {
                            LOG.error("Error calling download manager listener", e);
                        }
                    }
                }
            }

            @Override
            public void downloadComplete(DownloadManager manager) {
                if (listener != null) {
                    try {
                        listener.downloadComplete(dm);
                    } catch (Throwable e) {
                        LOG.error("Error calling download manager listener", e);
                    }
                }
            }
        });

        if (dm.getDM().getState() != DownloadManager.STATE_STOPPED) {
            dm.getDM().initialize();
        }
    }

    //    public static VuzeDownloadManager create(String torrent, final Set<String> fileSelection, String saveDir, VuzeDownloadListener listener) throws IOException {
    //        GlobalManager gm = VuzeManager.getInstance().getGlobalManager();
    //        DownloadManager dm = null;
    //        VuzeDownloadManager vdm = null;
    //        boolean initialize = false;
    //
    //        dm = findDM(gm, torrent);
    //
    //        if (dm == null) { // new download
    //            dm = gm.addDownloadManager(torrent, null, saveDir, null, DownloadManager.STATE_WAITING, true, false, new DownloadManagerInitialisationAdapter() {
    //                @Override
    //                public void initialised(DownloadManager manager, boolean for_seeding) {
    //                    setupPartialSelection(manager, fileSelection);
    //                }
    //
    //                @Override
    //                public int getActions() {
    //                    return 0;
    //                }
    //            });
    //
    //            initialize = true;
    //        } else { // download already there
    //            setupPartialSelection(dm, union(fileSelection, VuzeUtils.getSkippedPaths(dm)));
    //
    //            initialize = false;
    //        }
    //
    //        vdm = new VuzeDownloadManager(dm);
    //        setup(vdm, listener, initialize);
    //
    //        return vdm;
    //    }

    // this method modify the partial selection by only adding new paths.
    private static void setupPartialSelection(DownloadManager dm, Set<String> paths) {
        DiskManagerFileInfo[] infs = dm.getDiskManagerFileInfoSet().getFiles();

        try {
            dm.getDownloadState().suppressStateSave(true);

            if (paths == null || paths.isEmpty()) {
                for (DiskManagerFileInfo inf : infs) {
                    if (inf.isSkipped()) { // I don't want to trigger any internal logic
                        inf.setSkipped(false);
                    }
                }
            } else {
                for (DiskManagerFileInfo inf : infs) {
                    String path = inf.getFile(false).getPath();
                    if (inf.isSkipped() && paths.contains(path)) {
                        inf.setSkipped(false);
                    }
                }
            }
        } finally {
            dm.getDownloadState().suppressStateSave(false);
        }
    }

    public static VuzeDownloadManager create(URI uri) {
        // TODO Auto-generated method stub
        return null;
    }

    /*

    public static BittorrentDownload create(TransferManager manager, URI uri) throws TOTorrentException {
        if (uri.getScheme().equalsIgnoreCase("file")) {
            return create(manager, uri.getPath(), null, null);
        } else if (uri.getScheme().equalsIgnoreCase("http")) {
            return new TorrentFetcherDownload(manager, new TorrentUrlInfo(uri.toString()));
        } else {
            return new InvalidBittorrentDownload(R.string.torrent_scheme_download_not_supported);
        }
    }

    public static BittorrentDownload create(TransferManager manager, TorrentSearchResult sr) throws TOTorrentException {
        GlobalManager gm = AzureusManager.instance().getGlobalManager();

        if (StringUtils.isNullOrEmpty(sr.getHash())) {
            return new TorrentFetcherDownload(manager, new TorrentSearchResultInfo(sr));
        } else {
            Log.d(TAG, "About to create download for hash: " + sr.getHash());
            DownloadManager dm = gm.getDownloadManager(new HashWrapper(ByteUtils.decodeHex(sr.getHash())));
            if (dm == null) {// new download, I need to download the torrent
                Log.d(TAG, "Creating new TorrentFetcherDownload for hash: " + sr.getHash());
                return new TorrentFetcherDownload(manager, new TorrentSearchResultInfo(sr));
            } else {
                if (sr instanceof TorrentCrawledSearchResult) {
                    return create(manager, dm.getTorrentFileName(), dm.getTorrent().getHash(), sr.getFilename());
                } else {
                    return create(manager, dm.getTorrentFileName(), dm.getTorrent().getHash(), null);
                }
            }
        }
    }

    static BittorrentDownload create(TransferManager manager, DownloadManager dm) {
        setup(dm, false);

        return new AzureusBittorrentDownload(manager, new VuzeDownloadManager(dm));
    }

    public static BittorrentDownload create(TransferManager manager, String torrentFile, byte[] hash, String relativePartialPath) throws TOTorrentException {
        GlobalManager gm = AzureusManager.instance().getGlobalManager();
        TOTorrent torrent = null;
        DownloadManager dm = null;

        if (hash == null) {
            torrent = TorrentUtils.readFromFile(new File(torrentFile), false);
            hash = torrent.getHash();
        }

        if (hash != null) {
            dm = gm.getDownloadManager(new HashWrapper(hash));
        }

        if (dm == null) {
            boolean[] fileSelection = null;
            if (relativePartialPath != null) {
                if (torrent == null) {
                    torrent = TorrentUtils.readFromFile(new File(torrentFile), false);
                }
                fileSelection = buildFileSelection(torrent, relativePartialPath);
            }
            dm = createDownloadManager(manager, torrentFile, fileSelection);
            setup(dm, true);

        } else { //the download manager was there...

            boolean[] fileSelection = null;

            if (relativePartialPath != null) { //I want to download partial files.

                fileSelection = buildFileSelection(dm, relativePartialPath);
                boolean[] prevSelection = getFileSelection(dm);

                //he was already downloading the whole torrent, you'll get the file eventually when it finishes.
                if (isDownloadingAll(prevSelection)) {
                    return new InvalidBittorrentDownload(R.string.file_is_already_downloading);
                }

                //let the new fileSelection know about the older files that were selected for download
                //(union)
                for (int i = 0; i < fileSelection.length; i++) {
                    if (prevSelection[i]) {
                        fileSelection[i] = true;
                    }
                }

            } else { // I want to download the whole thing
                boolean[] prevSelection = getFileSelection(dm);
                if (isDownloadingAll(prevSelection)) {
                    return new InvalidBittorrentDownload(R.string.file_is_already_downloading);
                }
            }

            BittorrentDownload oldDownload = findDownload(manager, dm);
            setupPartialSelection2(dm, fileSelection);
            if (dm.getState() == DownloadManager.STATE_STOPPED) {
                dm.initialize();
            }

            if (oldDownload instanceof TorrentFetcherDownload) {
                oldDownload = ((TorrentFetcherDownload) oldDownload).getDelegate();
            }
            if (oldDownload instanceof AzureusBittorrentDownload) {
                ((AzureusBittorrentDownload) oldDownload).refreshData();
            }
            return oldDownload;
        }

        return new AzureusBittorrentDownload(manager, new VuzeDownloadManager(dm));
    }

    private static boolean isDownloadingAll(boolean[] fileSelection) {
        for (int i = 0; i < fileSelection.length; i++) {
            if (!fileSelection[i]) {
                return false;
            }
        }
        return true;
    }

    

    private static boolean[] getFileSelection(DownloadManager dm) {
        boolean[] fileSelections = new boolean[dm.getDiskManagerFileInfoSet().getFiles().length];
        for (int i = 0; i < dm.getDiskManagerFileInfoSet().getFiles().length; i++) {
            fileSelections[i] = !dm.getDiskManagerFileInfoSet().getFiles()[i].isSkipped();
        }

        return fileSelections;
    }

    private static boolean[] buildFileSelection(TOTorrent torrent, String relativePath) throws TOTorrentException {
        boolean[] filesSelection = new boolean[torrent.getFiles().length];
        for (int i = 0; i < filesSelection.length; i++) {
            filesSelection[i] = torrent.getFiles()[i].getRelativePath().equals(relativePath);
        }
        return filesSelection;
    }

    private static boolean[] buildFileSelection(DownloadManager dm, String relativePath) {
        DiskManagerFileInfo[] files = dm.getDiskManagerFileInfoSet().getFiles();
        boolean[] fileSelection = new boolean[files.length];
        for (int i = 0; i < files.length; i++) {
            fileSelection[i] = files[i].getFile(false).getAbsolutePath().endsWith(relativePath);
        }

        return fileSelection;
    }

    private static DownloadManager createDownloadManager(TransferManager manager, String torrentFile, final boolean[] fileSelection) {
        GlobalManager globalManager = AzureusManager.instance().getGlobalManager();
        String saveDir = SystemUtils.getTorrentDataDirectory().getAbsolutePath();

        if (fileSelection == null) {
            return globalManager.addDownloadManager(torrentFile, null, saveDir, DownloadManager.STATE_WAITING, true, false, null);
        } else {
            return globalManager.addDownloadManager(torrentFile, null, saveDir, null, DownloadManager.STATE_WAITING, true, false, new DownloadManagerInitialisationAdapter() {

                @Override
                public void initialised(DownloadManager manager, boolean for_seeding) {
                    setupPartialSelection(manager, fileSelection);
                }

                @Override
                public int getActions() {
                    return 0;
                }
            });
        }
    }

    private static void setupPartialSelection2(DownloadManager dm, boolean[] fileSelection) {
        DiskManagerFileInfo[] fileInfos = dm.getDiskManagerFileInfoSet().getFiles();

        try {
            dm.getDownloadState().suppressStateSave(true);

            if (fileSelection == null || fileSelection.length == 0) {
                for (DiskManagerFileInfo fileInfo : fileInfos) {
                    fileInfo.setSkipped(false);
                }
            } else {
                for (int i = 0; i < fileSelection.length; i++) {
                    if (fileSelection[i]) {
                        dm.getDiskManagerFileInfoSet().getFiles()[i].setSkipped(false);
                    }
                }
            }
        } finally {
            dm.getDownloadState().suppressStateSave(false);
        }
    }
    }
     */
}
