package com.limegroup.bittorrent;

import com.limegroup.gnutella.downloader.CoreDownloader;

/**
 * Public interface for the facade that BitTorrent downloaders use
 * to connect to LimeWire's core download system.
 */
public interface BTDownloader extends CoreDownloader {

    /** Initializes the download with the given meta information. */
    public void initBtMetaInfo(BTMetaInfo btMetaInfo);
    
    /** Adds another torrent event listener */
    public void addTorrentEventListener(TorrentEventListener listener);
    
    public int getNumPeers();

	public int getNumSeeds();
	
	public BTUploader createUploader();
}