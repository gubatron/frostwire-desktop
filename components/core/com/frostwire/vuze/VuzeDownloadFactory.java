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
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.download.DownloadManager;

import com.frostwire.search.torrent.TorrentSearchResult;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class VuzeDownloadFactory {

    private VuzeDownloadFactory() {
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

    private static void setupPartialSelection(DownloadManager dm, Set<String> fileSelection) {
        DiskManagerFileInfo[] fileInfos = dm.getDiskManagerFileInfoSet().getFiles();

        try {
            dm.getDownloadState().suppressStateSave(true);

            if (fileSelection == null || fileSelection.isEmpty()) {
                for (DiskManagerFileInfo fileInfo : fileInfos) {
                    fileInfo.setSkipped(false);
                }
            } else {
                for (DiskManagerFileInfo fileInfo : fileInfos) {
                    File f = fileInfo.getFile(true);
                    if (!fileSelection.contains(f)) {
                        fileInfo.setSkipped(true);
                    }
                }
            }
        } finally {
            dm.getDownloadState().suppressStateSave(false);
        }
    }

//    private static void setup(final VuzeDownloadManager vdm, final VuzeDownloadListener listener, boolean initialize) {
//        DownloadManager dm = vdm.getDM();
//        dm.addListener(new DownloadManagerAdapter() {
//
//            private AtomicBoolean finished = new AtomicBoolean(false);
//
//            @Override
//            public void stateChanged(DownloadManager manager, int state) {
//                if (state == DownloadManager.STATE_READY) {
//                    manager.startDownload();
//                }
//
//                if (manager.getAssumedComplete() && finished.compareAndSet(false, true)) {
//                    listener.downloadComplete(vdm);
//                }
//            }
//        });
//
//        if (initialize && dm.getState() != DownloadManager.STATE_STOPPED) {
//            dm.initialize();
//        }
//    }

//    private static DownloadManager findDM(GlobalManager gm, String torrent) throws IOException {
//        DownloadManager dm;
//        InputStream is = null;
//
//        try {
//            is = new FileInputStream(torrent);
//            // using FrostWire fork api for actual reading
//            TOTorrent t = TorrentUtils.readFromBEncodedInputStream(is);
//            dm = gm.getDownloadManager(new HashWrapper(t.getHash()));
//        } finally {
//            IOUtils.closeQuietly(is);
//        }
//
//        return dm;
//    }

    private static Set<String> union(Set<String> s1, Set<String> s2) {
        Set<String> s = new HashSet<String>(s1); // I don't want to modify original sets
        s.addAll(s2);
        return s;
    }

    public static VuzeDownloadManager create(String absolutePath, Object object, String relativePath) {
        // TODO Auto-generated method stub
        return null;
    }

    public static VuzeDownloadManager create(URI uri) {
        // TODO Auto-generated method stub
        return null;
    }

    public static VuzeDownloadManager create(TorrentSearchResult sr) {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
final class BittorrentDownloadCreator {

    private static final String TAG = "FW.BittorrentDownloadCreator";

    private BittorrentDownloadCreator() {
    }

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

    private static void setupPartialSelection(DownloadManager dm, boolean[] fileSelection) {
        DiskManagerFileInfo[] fileInfos = dm.getDiskManagerFileInfoSet().getFiles();

        try {
            dm.getDownloadState().suppressStateSave(true);

            boolean[] toSkip = new boolean[fileInfos.length];
            boolean[] toCompact = new boolean[fileInfos.length];

            int compNum = 0;

            for (int iIndex = 0; iIndex < fileInfos.length; iIndex++) {
                DiskManagerFileInfo fileInfo = fileInfos[iIndex];
                File fDest = fileInfo.getFile(true);

                if (!fileSelection[iIndex]) {
                    toSkip[iIndex] = true;
                    if (!fDest.exists()) {
                        toCompact[iIndex] = true;
                        compNum++;
                    }
                }
            }

            if (compNum > 0) {
                dm.getDiskManagerFileInfoSet().setStorageTypes(toCompact, DiskManagerFileInfo.ST_COMPACT);
            }

            dm.getDiskManagerFileInfoSet().setSkipped(toSkip, true);

        } finally {
            dm.getDownloadState().suppressStateSave(false);
        }
    }

    private static void setup(DownloadManager dm, final boolean notifyFinished) {
        dm.addListener(new DownloadManagerAdapter() {

            private AtomicBoolean finished = new AtomicBoolean(false);

            @Override
            public void stateChanged(DownloadManager manager, int state) {
                if (state == DownloadManager.STATE_READY) {
                    manager.startDownload();
                }

                if (VuzeUtils.isComplete(manager) && finished.compareAndSet(false, true)) {
                    if (!ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_TORRENT_SEED_FINISHED_TORRENTS)) {
                        VuzeUtils.stop(manager);
                    }

                    if (notifyFinished) {
                        TransferManager.instance().incrementDownloadsToReview();
                        Engine.instance().notifyDownloadFinished(manager.getDisplayName(), manager.getSaveLocation().getAbsoluteFile());
                        Librarian.instance().scan(manager.getSaveLocation().getAbsoluteFile());
                    }
                }
            }
        });

        if (dm.getState() != DownloadManager.STATE_STOPPED) {
            dm.initialize();
        }
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

    private static BittorrentDownload findDownload(TransferManager manager, DownloadManager dm) {
        for (BittorrentDownload download : manager.getBittorrentDownloads()) {
            BittorrentDownload btDownload = download;
            if (download instanceof TorrentFetcherDownload) {
                btDownload = ((TorrentFetcherDownload) download).getDelegate();
            }
            if (btDownload != null) {
                if (btDownload instanceof AzureusBittorrentDownload) {
                    if (((AzureusBittorrentDownload) btDownload).getDownloadManager().equals(dm)) {
                        return download;
                    }
                }
            }
        }
        return null;
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
