package com.frostwire.gui.download.bittorrent;

import org.gudy.azureus2.core3.download.DownloadManager;

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
}
