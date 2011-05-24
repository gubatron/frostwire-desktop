package com.limegroup.gnutella.downloader;

import com.frostwire.bittorrent.BTDownloader;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.limegroup.gnutella.DownloadCallback;
import com.limegroup.gnutella.Downloader;

/**
 * Once an in-network download finishes, the UpdateHandler is notified.
 */
@Singleton
public class InNetworkCallback implements DownloadCallback {
    
    @Inject
    public InNetworkCallback() {
    }
    
    public void addDownload(Downloader d) {
    }
    
    public void addDownload(BTDownloader d) {
    }

    public void removeDownload(Downloader d) {
        InNetworkDownloader downloader = (InNetworkDownloader) d;
    }

    public void downloadsComplete() {
    }

    public void showDownloads() {
    }

    // always discard corruption.
    public void promptAboutCorruptDownload(Downloader dloader) {
        dloader.discardCorruptDownload(true);
    }

    public String getHostValue(String key) {
        return null;
    }
}