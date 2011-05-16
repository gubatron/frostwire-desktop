package com.frostwire.gui.download.bittorrent;

import java.io.File;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.util.DisplayFormatters;

public class BTDownloaderImpl implements BTDownloader {

    private final DownloadManager _downloadManager;

    public BTDownloaderImpl(DownloadManager downloadManager) {
        _downloadManager = downloadManager;
    }

    public long getSize() {
        return _downloadManager.getSize();
    }

    public String getDisplayName() {
        return _downloadManager.getDisplayName();
    }

    public boolean isResumable() {
        return TorrentUtil.isStartable(_downloadManager);
    }

    public boolean isPausable() {
        return TorrentUtil.isStopable(_downloadManager);
    }

    public boolean isCompleted() {
        return _downloadManager.getAssumedComplete();
    }

    public int getState() {
        return _downloadManager.getState();
    }

    public void remove() {
        TorrentUtil.removeDownload(_downloadManager);
    }

    public void pause() {
        if (isPausable()) {
            TorrentUtil.stop(_downloadManager);
        }
    }

    public void resume() {
        if (isResumable()) {
            TorrentUtil.start(_downloadManager);
        }
    }

    public File getSaveLocation() {
        return _downloadManager.getSaveLocation();
    }

    public int getProgress() {
        return _downloadManager.getStats().getDownloadCompleted(true) / 10;
    }

    public String getStateString() {
        return DisplayFormatters.formatDownloadStatus(_downloadManager);
    }

    public long getBytesReceived() {
        return _downloadManager.getStats().getTotalGoodDataBytesReceived();
    }

    public double getDownloadSpeed() {
        return _downloadManager.getStats().getDataReceiveRate();
    }

    public double getUploadSpeed() {
        return _downloadManager.getStats().getDataSendRate();
    }

    public long getETA() {
        return _downloadManager.getStats().getETA();
    }

    public DownloadManager getDownloadManager() {
        return _downloadManager;
    }
}
