package com.limegroup.gnutella.downloader;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.limegroup.gnutella.DownloadManager;

@Singleton
public class RequeryManagerFactoryImpl implements RequeryManagerFactory {
    
    private final Provider<DownloadManager> downloadManager;

    @Inject
    public RequeryManagerFactoryImpl(Provider<DownloadManager> downloadManager) {
        this.downloadManager = downloadManager;
    }    

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.downloader.RequeryManagerFactory#createRequeryManager(com.limegroup.gnutella.downloader.ManagedDownloader)
     */
    public RequeryManager createRequeryManager(
            RequeryListener requeryListener) {
        return null;
    }
}
