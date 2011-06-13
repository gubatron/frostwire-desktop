package com.limegroup.gnutella;

import com.frostwire.bittorrent.BTDownloader;

/**
 * A callback for download information.
 */
public interface DownloadCallback {
    

    /** Add a file to the download window */
    public void addDownload(Downloader d);
    
    public void addDownload(BTDownloader d);

    /** Remove a downloader from the download window. */
    public void removeDownload(Downloader d);

     /** 
      * Notifies the GUI that all active downloads have been completed.
      */   
    public void downloadsComplete();

	/**
	 *  Show active downloads
	 */
	public void showDownloads();
    
}