package com.frostwire.bittorrent;

import java.io.File;

import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerInitialisationAdapter;
import org.gudy.azureus2.core3.download.impl.DownloadManagerAdapter;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.util.TorrentUtils;

import com.limegroup.gnutella.SaveLocationException;
import com.limegroup.gnutella.settings.SharingSettings;

public class BTDownloaderFactory {

    private final GlobalManager _globalManager;
    private final File _file;
    private final boolean[] _filesSelection;
    private final boolean _initialSeed;
    private final File _saveDir;
    
    public BTDownloaderFactory(GlobalManager globalManager, File file, boolean[] filesSelection, boolean initialSeed, File saveDir) {
        _globalManager = globalManager;
        _file = file;
        _filesSelection = filesSelection;
        _initialSeed = initialSeed;
        _saveDir = saveDir;
    }

    public File getSaveFile() {
        return _file;
    }

    public void setSaveFile(File newFile) {
    }

    public BTDownloader createDownloader(boolean overwrite) throws SaveLocationException, TOTorrentException {

        File saveDir = _saveDir == null ? SharingSettings.TORRENT_DATA_DIR_SETTING.getValue() : _saveDir;
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        TOTorrent torrent = TorrentUtils.readFromFile(_file, false);

        DownloadManager manager;

        if ((manager = _globalManager.getDownloadManager(torrent)) == null) {
            if (_filesSelection == null) {
                manager = _globalManager.addDownloadManager(_file.getAbsolutePath(), null, saveDir.getAbsolutePath(), DownloadManager.STATE_WAITING, true, _initialSeed, null);
            } else {
                manager = _globalManager.addDownloadManager(_file.getAbsolutePath(), torrent.getHash(), saveDir.getAbsolutePath(), null,
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
        }

        return createDownloader(manager);
    }

    public BTDownloader createDownloader(DownloadManager downloadManager) {
        final BTDownloader btDownloader = new BTDownloaderImpl(downloadManager);

        downloadManager.addListener(new DownloadManagerAdapter() {
            @Override
            public void stateChanged(DownloadManager manager, int state) {
                if (state == DownloadManager.STATE_READY) {
                    manager.startDownload();
                } else if (state == DownloadManager.STATE_WAITING) {
                    // manager.initialize();
                }

                if (!SharingSettings.SEED_FINISHED_TORRENTS.getValue()) {
                    if (manager.getAssumedComplete()) {
                        btDownloader.pause();
                    }
                }
            }
        });

        if (downloadManager.getState() != DownloadManager.STATE_STOPPED) {
            //downloadManager.setStateWaiting();
            downloadManager.initialize();
        }

        return btDownloader;
    }
}
