package com.frostwire.gui.bittorrent;

import java.io.File;

import org.gudy.azureus2.core3.download.DownloadManager;
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

    private DownloadManager _downloadManager;
    private boolean _torrentInGlobalManager;

    public BTDownloadCreator(File torrentFile, File saveDir, boolean initialSeed) throws TOTorrentException {
        _globalManager = AzureusStarter.getAzureusCore().getGlobalManager();
        _torrentFile = torrentFile;
        _saveDir = saveDir;
        _initialSeed = initialSeed;

        saveDir = _saveDir == null ? SharingSettings.TORRENT_DATA_DIR_SETTING.getValue() : _saveDir;
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        TOTorrent torrent = TorrentUtils.readFromFile(_torrentFile, false);

        _downloadManager = _globalManager.getDownloadManager(torrent);
        if (_downloadManager == null) {
            _downloadManager = _globalManager.addDownloadManager(_torrentFile.getAbsolutePath(), null, saveDir.getAbsolutePath(),
                    DownloadManager.STATE_WAITING, true, _initialSeed, null);
            _torrentInGlobalManager = false;
        } else {
            _torrentInGlobalManager = true;
        }
    }

    public BTDownloadCreator(File torrentFile) throws TOTorrentException {
        this(torrentFile, null, false);
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
