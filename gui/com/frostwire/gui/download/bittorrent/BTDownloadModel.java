package com.frostwire.gui.download.bittorrent;

import java.util.HashSet;

import org.gudy.azureus2.core3.download.DownloadManager;

import com.aelitis.azureus.core.AzureusCore;
import com.frostwire.bittorrent.AzureusStarter;
import com.frostwire.bittorrent.BTDownloader;
import com.frostwire.bittorrent.TorrentUtil;
import com.limegroup.gnutella.gui.tables.BasicDataLineModel;
import com.limegroup.gnutella.settings.iTunesImportSettings;

/**
 * This class provides access to the <tt>ArrayList</tt> that stores all of the
 * downloads displayed in the download window.
 */
public class BTDownloadModel extends BasicDataLineModel<BTDownloadDataLine, BTDownloader> {

    /**
     * 
     */
    private static final long serialVersionUID = 8163563369069283107L;

    private HashSet<String> _hashDownloads;

    /**
     * Initialize the model by setting the class of its DataLines.
     */
    BTDownloadModel() {
        super(BTDownloadDataLine.class);
        _hashDownloads = new HashSet<String>();
    }

    /**
     * Creates a new DownloadDataLine
     */
    public BTDownloadDataLine createDataLine() {
        return new BTDownloadDataLine();
    }

    int getActiveDownloads() {
        int size = getRowCount();
        int count = 0;

        for (int i = 0; i < size; i++) {
            BTDownloader downloader = get(i).getInitializeObject();
            if (!downloader.isCompleted() && downloader.getState() == DownloadManager.STATE_DOWNLOADING) {
                count++;
            }
        }
        return count;
    }

    int getActiveUploads() {
        int size = getRowCount();
        int count = 0;

        for (int i = 0; i < size; i++) {
            BTDownloader downloader = get(i).getInitializeObject();
            if (downloader.isCompleted() && downloader.getState() == DownloadManager.STATE_SEEDING) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the aggregate amount of bandwidth being consumed by active downloads.
     *  
     * @return the total amount of bandwidth being consumed by active downloads.
     */
    double getBandwidth(boolean download) {
        AzureusCore azureusCore = AzureusStarter.getAzureusCore();

        if (azureusCore == null) {
            return 0;
        }

        return (download) ? azureusCore.getGlobalManager().getStats().getDataReceiveRate() : azureusCore.getGlobalManager().getStats().getDataSendRate();
    }

    public double getDownloadsBandwidth() {
        return getBandwidth(true);
    }

    public double getUploadsBandwidth() {
        return getBandwidth(false);
    }

    public int getTotalDownloads() {
        return getRowCount();
    }

    /**
     * Over-ride the default refresh so that we can
     * set the CLEAR_BUTTON as appropriate.
     */
    public Object refresh() {
        int size = getRowCount();
        for (int i = 0; i < size; i++) {
            BTDownloadDataLine ud = get(i);
            ud.update();
        }
        fireTableRowsUpdated(0, size);
        return Boolean.TRUE;
    }

    @Override
    public int add(BTDownloader downloader) {
        _hashDownloads.add(TorrentUtil.hashToString(downloader.getHash()));
        return super.add(downloader);
    }

    @Override
    public void remove(int i) {
        BTDownloadDataLine line = get(i);

        BTDownloader downloader = line.getInitializeObject();

        downloader.remove();

        _hashDownloads.remove(TorrentUtil.hashToString(downloader.getHash()));

        super.remove(i);
    }

    public boolean isDownloading(String hash) {
        return _hashDownloads.contains(hash);
    }
}
