package com.limegroup.gnutella;

import com.frostwire.gui.bittorrent.BTDownloader;

/**
 * A callback for download information.
 */
public interface DownloadCallback {
    

    /** Add a file to the download window */
    public void addDownloadManager(org.gudy.azureus2.core3.download.DownloadManager d);
    
    public void addDownload(BTDownloader d);

    /** Remove a downloader from the download window. */
    //public void removeDownload(Downloader d);

     /** 
      * Notifies the GUI that all active downloads have been completed.
      */   
    public void downloadsComplete();

	/**
	 *  Show active downloads
	 */
	public void showDownloads();
    
}