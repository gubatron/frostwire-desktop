package com.limegroup.gnutella;

import java.io.File;


/**
 *  Callback to notify the GUI of asynchronous backend events.
 *  The methods in this fall into the following categories:
 *
 *  <ul>
 *  <li>Query replies (for displaying results) and query strings 
 *     (for the monitor)
 *  <li>Update in shared file statistics
 *  <li>Change of connection state
 *  <li>New or dead uploads or downloads
 *  <li>New chat requests and chat messages
 *  <li>Error messages
 *  </ul>
 */
public interface ActivityCallback {
   
    /** Add a file to the download window */
    public void addDownloadManager(org.gudy.azureus2.core3.download.DownloadManager d);

    /** 
      * Notifies the GUI that all active downloads have been completed.
      */
    public void downloadsComplete();

    /**
     *  Show active downloads
     */
    public void showDownloads();

    /**
     * The address of the program has changed or we've
     * just accepted our first incoming connection.
     */
    public void handleAddressStateChanged();

    /**
     * Notification that the file manager is beginning loading.
     */
    public void fileManagerLoading();

    /**
     * Notification that an update became available.
     */
    public void updateAvailable(UpdateInformation info);

    /** 
     * Notifies the GUI that all active uploads have been completed.
     */
    public void uploadsComplete();

    /**
     *  Tell the GUI to deiconify.
     */
    public void restoreApplication();

    /**
     * Indicates a component is loading.
     */
    public void componentLoading(String component);

    /**
     * The core passes parsed magnets to the GUI and asks it if it wants
     * to handle them itself.
     * <p>
     * If this is the case the callback should return <code>true</code>, otherwise
     * the core starts the downloads itself.
     * @param magnets
     * @return true if the callback handles the magnet links
     */
    public boolean handleMagnets(MagnetOptions[] magnets);

    /** Try to download the torrent file */
    public void handleTorrent(File torrentFile);

    public void handleTorrentMagnet(String request, boolean partialDownload);
    
    public boolean isRemoteDownloadsAllowed();
}
