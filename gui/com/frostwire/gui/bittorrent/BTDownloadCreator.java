package com.frostwire.gui.bittorrent;

import java.io.File;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerInitialisationAdapter;
import org.gudy.azureus2.core3.download.impl.DownloadManagerAdapter;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.util.TorrentUtils;
import org.limewire.util.OSUtils;

import com.frostwire.AzureusStarter;
import com.limegroup.gnutella.SaveLocationException;
import com.limegroup.gnutella.gui.iTunesMediator;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.settings.iTunesSettings;

public class BTDownloadCreator {

    private final GlobalManager _globalManager;
    private final File _torrentFile;
    private final File _saveDir;
    private final boolean _initialSeed;
    private final boolean[] _filesSelection;

    private DownloadManager _downloadManager;
    private boolean _torrentInGlobalManager;

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

        TOTorrent torrent = TorrentUtils.readFromFile(_torrentFile, false);

        _downloadManager = _globalManager.getDownloadManager(torrent);
        if (_downloadManager == null) {
            if (_filesSelection == null) {
                _downloadManager = _globalManager.addDownloadManager(_torrentFile.getAbsolutePath(), null, saveDir.getAbsolutePath(),
                        DownloadManager.STATE_WAITING, true, _initialSeed, null);
            } else {
                _downloadManager = _globalManager.addDownloadManager(_torrentFile.getAbsolutePath(), torrent.getHash(), saveDir.getAbsolutePath(), null,
                        DownloadManager.STATE_WAITING, true, false, new DownloadManagerInitialisationAdapter() {
                            public void initialised(DownloadManager dm) {
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
                        });
            }
            _torrentInGlobalManager = false;
        } else {
            _torrentInGlobalManager = true;
        }
    }

    public BTDownloadCreator(File torrentFile, boolean[] filesSelection) throws TOTorrentException {
        this(torrentFile, null, false, filesSelection);
    }

    public boolean isTorrentInGlobalManager() {
        return _torrentInGlobalManager;
    }

    public BTDownload createDownload() throws SaveLocationException, TOTorrentException {

        _downloadManager.addListener(new DownloadManagerAdapter() {
            @Override
            public void stateChanged(DownloadManager manager, int state) {
                if (state == DownloadManager.STATE_READY) {
                    manager.startDownload();
                }

                if (!SharingSettings.SEED_FINISHED_TORRENTS.getValue()) {
                    if (manager.getAssumedComplete()) {
                        if (TorrentUtil.isStopable(manager)) {
                            TorrentUtil.stop(manager);
                        }
                    }
                }

                if (manager.getAssumedComplete() && iTunesSettings.ITUNES_SUPPORT_ENABLED.getValue()
                        && !iTunesMediator.instance().isScanned(manager.getSaveLocation())) {
                    if ((OSUtils.isMacOSX() || OSUtils.isWindows())) {
                        iTunesMediator.instance().scanForSongs(manager.getSaveLocation());
                    }
                }
            }
        });

        if (_downloadManager.getState() != DownloadManager.STATE_STOPPED) {
            _downloadManager.initialize();
        }

        return new BTDownloaderImpl(_downloadManager);
    }
}
