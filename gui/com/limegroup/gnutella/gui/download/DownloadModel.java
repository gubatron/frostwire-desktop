package com.limegroup.gnutella.gui.download;

import com.limegroup.bittorrent.BTDownloader;
import com.limegroup.bittorrent.BTDownloaderImpl;
import com.limegroup.bittorrent.TorrentEvent;
import com.limegroup.bittorrent.TorrentEvent.Type;
import com.limegroup.bittorrent.gui.TorrentFileFetcher;
import com.limegroup.gnutella.Downloader;
import com.limegroup.gnutella.Downloader.DownloadStatus;
import com.limegroup.gnutella.downloader.CoreDownloader;
import com.limegroup.gnutella.gui.tables.BasicDataLineModel;

/**
 * This class provides access to the <tt>ArrayList</tt> that stores all of the
 * downloads displayed in the download window.
 */
final class DownloadModel extends BasicDataLineModel<DownloadDataLine, Downloader> {

    /**
     * Initialize the model by setting the class of its DataLines.
     */
    DownloadModel() {
        super(DownloadDataLine.class);
    }

    /**
     * Creates a new DownloadDataLine
     */
    public DownloadDataLine createDataLine() {
        return new DownloadDataLine();
    }    
	
	/**
	 * Returns a count of the active downloads.
	 *
	 * @return the number of active downloads
	 */
	int countActiveDownloads() {
		int size  = getRowCount();
		int count = 0;

		for (int i=0; i<size; i++) {
			DownloadDataLine ud = get(i);
			if(!ud.isInactive()) count++;
		}
		return count;
	}

	/**
	 * Returns the currently connected downloads.
	 *
	 * @return the number of current downloads
	 */
	int getCurrentDownloads() {
		int size  = getRowCount();
		int count = 0;

		for (int i=0; i<size; i++) {
			DownloadDataLine dd = get(i);
			if(dd.isDownloading()) count++;
		}
		return count;
	}

    /**
     * Returns the aggregate amount of bandwidth being consumed by active downloads.
     *  
     * @return the total amount of bandwidth being consumed by active downloads.
     */
    double getActiveDownloadsBandwidth() {
        int size = getRowCount();
        double count = 0.0;

        for (int i=0; i<size; i++) {
            DownloadDataLine dd = get(i);
            if(!dd.isInactive()) {
                //  Speed can be -1 for some states, so max with 0 
                count += Math.max(dd.getSpeed(), 0.0);
            }
        }
        return count;
    }

	/**
	 * Over-ride the default refresh so that we can
	 * set the CLEAR_BUTTON as appropriate.
	 */
	public Object refresh() {
		int size = getRowCount();
		boolean inactiveDownloadPresent = false;
		for(int i=0; i<size; i++) {
			DownloadDataLine ud = get(i);
			ud.update();
			inactiveDownloadPresent |= ud.isInactive();
		}
        fireTableRowsUpdated(0, size);
        return inactiveDownloadPresent ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * Clears all completed downloads from the download list.
	 */
	void clearCompleted() {
		for(int i=getRowCount()-1; i>=0; i--) {
			DownloadDataLine line = get(i);
			if(line.isInactive()) {
                remove(i);
                // we also will 'kill' GAVE_UP downloaders for the user....
                if (line.getState()==DownloadStatus.GAVE_UP)
                    line.cleanup();
            }
		}
	}

	public void remove(int i) {
		DownloadDataLine line = get(i);
		
		Downloader downloader = null;
		
		if (line.getDownloader() instanceof TorrentFileFetcher) {
            downloader = ((TorrentFileFetcher) line.getDownloader()).getDownloader();
        } else if (line.getDownloader() instanceof BTDownloaderImpl) {
            downloader = line.getDownloader();
        }
        
        
        if (downloader != null && downloader instanceof BTDownloaderImpl) {
            ((BTDownloaderImpl) downloader).removeFromDownloadManager();
        }

        super.remove(i);
	}
}








