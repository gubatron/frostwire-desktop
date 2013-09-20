package com.frostwire.gui.bittorrent;

import java.io.File;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerInitialisationAdapter;
import org.gudy.azureus2.core3.download.impl.DownloadManagerAdapter;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.util.HashWrapper;
import org.gudy.azureus2.core3.util.TorrentUtils;
import org.limewire.util.ByteUtils;
import org.limewire.util.CommonUtils;
import org.limewire.util.OSUtils;

import com.frostwire.AzureusStarter;
import com.frostwire.gui.library.LibraryMediator;
import com.limegroup.gnutella.SaveLocationException;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.iTunesMediator;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.settings.iTunesSettings;

public class BTDownloadCreator {

    private final GlobalManager _globalManager;
    private final File _torrentFile;
    private final File _saveDir;
    private final boolean _initialSeed;
    private boolean[] _filesSelection;

    private DownloadManager _downloadManager;
    private boolean _torrentInGlobalManager;
    private boolean createDownload;
    private TOTorrent torrent;

    public BTDownloadCreator(File torrentFile, File saveDir, boolean initialSeed, boolean[] filesSelection) throws TOTorrentException {
        _globalManager = AzureusStarter.getAzureusCore().getGlobalManager();
        _torrentFile = torrentFile;
        _saveDir = saveDir;
        _initialSeed = initialSeed;
        _filesSelection = filesSelection;

        saveDir = _saveDir == null ? SharingSettings.TORRENT_DATA_DIR_SETTING.getValue() : _saveDir;
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        torrent = TorrentUtils.readFromFile(_torrentFile, false);
        createDownload = true;

        _downloadManager = _globalManager.getDownloadManager(torrent);
        if (_downloadManager == null) {

            //Complete torrent download.
            if (_filesSelection == null) {
                _downloadManager = _globalManager.addDownloadManager(_torrentFile.getAbsolutePath(), null, saveDir.getAbsolutePath(), DownloadManager.STATE_WAITING, true, _initialSeed, null);
            } else {
                //Partial torrent download.
                addPartialDownload(saveDir);
            }
            _torrentInGlobalManager = false;
        } else { //the download manager was there...
            _torrentInGlobalManager = true;

            if (_filesSelection != null) { //I want to download partial files.
                boolean[] prevSelection = getPreviousFileSelections(_downloadManager);

                if (prevSelection.length != _filesSelection.length) {
                    //wtf
                    System.out.println("BTDownloadCreator::constructor warning: inconsistency between file selection count, from old state to new state. Is this the same torrent?");
                    return;
                }

                //he was already downloading the whole torrent, you'll get the file eventually when it finishes.
                if (isDownloadingEntireContents(prevSelection)) {
                    return;
                }

                //let the new _fileSelection know about the older files that were selected for download
                //(union)
                for (int i = 0; i < _filesSelection.length; i++) {
                    if (prevSelection[i]) {
                        _filesSelection[i] = true;
                    }
                }

            } else { // I want to download the whole thing
                boolean[] prevSelection = getPreviousFileSelections(_downloadManager);
                if (isDownloadingEntireContents(prevSelection)) {
                    //oh, it was already downloading the whole thing
                    createDownload = false;
                    return;
                }

                _filesSelection = prevSelection;
                for (int i = 0; i < _filesSelection.length; i++) {
                    _filesSelection[i] = true;
                }

            }

            //remove it, not async.
            TorrentUtil.removeDownload(_downloadManager, false, false, false);

            addPartialDownload(saveDir);

        }
    }

    public void addPartialDownload(File saveDir) throws TOTorrentException {
        _downloadManager = _globalManager.addDownloadManager(_torrentFile.getAbsolutePath(), torrent.getHash(), saveDir.getAbsolutePath(), null, DownloadManager.STATE_WAITING, true, false, new DownloadManagerInitialisationAdapter() {
            public void initialised(DownloadManager dm) {
                setupPartialDownload(dm);
            }
        });
    }

    private boolean isDownloadingEntireContents(boolean[] prevSelection) {
        for (int i = 0; i < prevSelection.length; i++) {
            if (!prevSelection[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean[] getPreviousFileSelections(DownloadManager dm) {

        boolean[] fileSelections = new boolean[dm.getDiskManagerFileInfoSet().getFiles().length];
        for (int i = 0; i < dm.getDiskManagerFileInfoSet().getFiles().length; i++) {
            fileSelections[i] = !dm.getDiskManagerFileInfoSet().getFiles()[i].isSkipped();
        }

        return fileSelections;
    }

    private void setupPartialDownload(DownloadManager dm) {
        DiskManagerFileInfo[] fileInfos = dm.getDiskManagerFileInfoSet().getFiles();

        try {
            dm.getDownloadState().suppressStateSave(true);

            boolean[] toSkip = new boolean[fileInfos.length];
            boolean[] toCompact = new boolean[fileInfos.length];

            int comp_num = 0;

            for (int iIndex = 0; iIndex < fileInfos.length; iIndex++) {
                DiskManagerFileInfo fileInfo = fileInfos[iIndex];
                File fDest = fileInfo.getFile(true);

                if (!_filesSelection[iIndex]) {
                    toSkip[iIndex] = true;
                    if (!fDest.exists()) {
                        toCompact[iIndex] = true;
                        comp_num++;
                    }
                }
            }

            if (comp_num > 0) {
                dm.getDiskManagerFileInfoSet().setStorageTypes(toCompact, DiskManagerFileInfo.ST_COMPACT);
            }

            dm.getDiskManagerFileInfoSet().setSkipped(toSkip, true);

        } finally {
            dm.getDownloadState().suppressStateSave(false);
        }
    }

    public BTDownloadCreator(File torrentFile, boolean[] filesSelection) throws TOTorrentException {
        this(torrentFile, null, false, filesSelection);
    }

    public BTDownloadCreator(String hash, String relativePath) throws TOTorrentException {
        _globalManager = AzureusStarter.getAzureusCore().getGlobalManager();
        _torrentFile = null;
        _saveDir = null;
        _initialSeed = false;

        createDownload = false;

        _downloadManager = _globalManager.getDownloadManager(new HashWrapper(ByteUtils.decodeHex(hash)));
        if (_downloadManager == null) {
            throw new IllegalArgumentException("DownloadManager with hash does not exist.");
        } else { //the download manager was there...
            _torrentInGlobalManager = true;

            DownloadManager dm = _downloadManager;

            DiskManagerFileInfo[] fileInfos = dm.getDiskManagerFileInfoSet().getFiles();

            try {
                dm.getDownloadState().suppressStateSave(true);

                for (int iIndex = 0; iIndex < fileInfos.length; iIndex++) {
                    DiskManagerFileInfo fileInfo = fileInfos[iIndex];
                    File fDest = fileInfo.getFile(true);

                    if (fDest.getAbsolutePath().endsWith(relativePath)) {
                        if (fileInfo.isSkipped()) {
                            fileInfo.setSkipped(false);
                        }
                    }
                }

            } finally {
                dm.getDownloadState().suppressStateSave(false);
            }

        }
    }

    public BTDownload createDownload() throws SaveLocationException, TOTorrentException {
        if (_torrentInGlobalManager) {
            if (createDownload) {
                return new DuplicateDownload(createDownload(_downloadManager, false));
            } else {
                return new DuplicateDownload(new BTDownloadImpl(_downloadManager));
            }
        } else {
            return createDownload(_downloadManager, false);
        }
    }

    public static BTDownload createDownload(DownloadManager downloadManager, final boolean triggerFilter) throws SaveLocationException, TOTorrentException {

        downloadManager.addListener(new DownloadManagerAdapter() {
            @Override
            public void stateChanged(DownloadManager manager, int state) {
                if (state == DownloadManager.STATE_READY) {
                    manager.startDownload();
                }

                if (!SharingSettings.SEED_FINISHED_TORRENTS.getValue() || (TorrentUtil.isHandpicked(manager) && !SharingSettings.SEED_HANDPICKED_TORRENT_FILES.getValue())) {
                    if (manager.getAssumedComplete()) {
                        if (TorrentUtil.isStopable(manager)) {
                            TorrentUtil.stop(manager);
                        }
                    }
                }

                if (manager.getAssumedComplete() && iTunesSettings.ITUNES_SUPPORT_ENABLED.getValue() && !iTunesMediator.instance().isScanned(manager.getSaveLocation())) {
                    if ((OSUtils.isMacOSX() || OSUtils.isWindows())) {
                        iTunesMediator.instance().scanForSongs(manager.getSaveLocation());
                    }
                }

                if (manager.getAssumedComplete() && !LibraryMediator.instance().isScanned(manager.hashCode())) {
                    LibraryMediator.instance().scan(manager.hashCode(), manager.getSaveLocation());
                }

                //if you have to hide seeds, do so.
                if (triggerFilter && state == DownloadManager.STATE_SEEDING) {
                    GUIMediator.safeInvokeLater(new Runnable() {
                        public void run() {
                            BTDownloadMediator.instance().updateTableFilters();
                        }
                    });
                }
            }
        });

        if (downloadManager.getState() != DownloadManager.STATE_STOPPED) {
            downloadManager.initialize();
        }

        if (CommonUtils.isPortable()) {
            downloadManager.setPieceCheckingEnabled(false);
        }

        return new BTDownloadImpl(downloadManager);
    }
}
