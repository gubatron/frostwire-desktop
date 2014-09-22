/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.gui.bittorrent;

import com.frostwire.bittorrent.BTDownload;
import com.frostwire.bittorrent.BTDownloadListener;
import com.frostwire.gui.library.LibraryMediator;
import com.frostwire.torrent.CopyrightLicenseBroker;
import com.frostwire.torrent.PaymentOptions;
import com.frostwire.transfers.TransferState;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.iTunesMediator;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.settings.iTunesSettings;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.limewire.util.OSUtils;

import java.io.File;
import java.util.Date;

/**
 * @author gubatron
 * @author aldenml
 */
public class BittorrentDownload implements com.frostwire.gui.bittorrent.BTDownload {

    private final BTDownload dl;

    private boolean deleteTorrentWhenRemove;
    private boolean deleteDataWhenRemove;

    public BittorrentDownload(BTDownload dl) {
        this.dl = dl;
        this.dl.setListener(new StatusListener());

        dl.resume();
    }

    public BTDownload getDl() {
        return dl;
    }

    @Override
    public long getSize() {
        return dl.getSize();
    }

    @Override
    public long getSize(boolean update) {
        return dl.getSize();
    }

    @Override
    public String getDisplayName() {
        return dl.getName();
    }

    @Override
    public boolean isResumable() {
        return dl.isPaused();
    }

    @Override
    public boolean isPausable() {
        return !dl.isPaused();
    }

    @Override
    public boolean isCompleted() {
        return dl.isFinished();
    }

    @Override
    public TransferState getState() {
        return dl.getState();
    }

    @Override
    public void remove() {
        dl.stop(deleteTorrentWhenRemove, deleteDataWhenRemove);
    }

    @Override
    public void pause() {
        dl.pause();
    }

    @Override
    public File getSaveLocation() {
        return new File(dl.getSavePath());
    }

    @Override
    public void resume() {
        dl.resume();
    }

    @Override
    public int getProgress() {
        return dl.getProgress();
    }

    @Override
    public long getBytesReceived() {
        return dl.getTotalBytesReceived();
    }

    @Override
    public long getBytesSent() {
        return dl.getTotalBytesSent();
    }

    @Override
    public double getDownloadSpeed() {
        return dl.getDownloadSpeed();
    }

    @Override
    public double getUploadSpeed() {
        return dl.getUploadSpeed();
    }

    @Override
    public long getETA() {
        return dl.getETA();
    }

    @Override
    public String getPeersString() {
        return dl.getConnectedPeers() + "/" + dl.getTotalPeers();
    }

    @Override
    public String getSeedsString() {
        return dl.getConnectedSeeds() + "/" + dl.getTotalSeeds();
    }

    @Override
    public boolean isDeleteTorrentWhenRemove() {
        return deleteTorrentWhenRemove;
    }

    @Override
    public void setDeleteTorrentWhenRemove(boolean deleteTorrentWhenRemove) {
        this.deleteTorrentWhenRemove = deleteTorrentWhenRemove;
    }

    @Override
    public boolean isDeleteDataWhenRemove() {
        return deleteDataWhenRemove;
    }

    @Override
    public void setDeleteDataWhenRemove(boolean deleteDataWhenRemove) {
        this.deleteDataWhenRemove = deleteDataWhenRemove;
    }

    @Override
    public String getHash() {
        return dl.getInfoHash();
    }

    @Override
    public String getSeedToPeerRatio() {
        return dl.getTotalSeeds() + "/" + dl.getTotalPeers();
    }

    @Override
    public String getShareRatio() {
        long sent = dl.getTotalBytesSent();
        long received = dl.getTotalBytesReceived();

        if (received < 0) {
            return "0";
        }

        return String.valueOf((double) sent / (double) received);
    }

    @Override
    public boolean isPartialDownload() {
        return false;
    }

    @Override
    public Date getDateCreated() {
        return dl.getDateCreated();
    }

    @Override
    public PaymentOptions getPaymentOptions() {
        return null;
    }

    @Override
    public CopyrightLicenseBroker getCopyrightLicenseBroker() {
        return null;
    }

    private class StatusListener implements BTDownloadListener {

        @Override
        public void finished(BTDownload dl) {
            if (!SharingSettings.SEED_FINISHED_TORRENTS.getValue() || (dl.isPartial() && !SharingSettings.SEED_HANDPICKED_TORRENT_FILES.getValue())) {
                dl.pause();
            }

            File saveLocation = new File(dl.getSavePath());

            if (iTunesSettings.ITUNES_SUPPORT_ENABLED.getValue() && !iTunesMediator.instance().isScanned(saveLocation)) {
                if ((OSUtils.isMacOSX() || OSUtils.isWindows())) {
                    iTunesMediator.instance().scanForSongs(saveLocation);
                }
            }

            if (!LibraryMediator.instance().isScanned(dl.hashCode())) {
                LibraryMediator.instance().scan(dl.hashCode(), saveLocation);
            }

            //if you have to hide seeds, do so.
            GUIMediator.safeInvokeLater(new Runnable() {
                public void run() {
                    BTDownloadMediator.instance().updateTableFilters();
                }
            });
        }
    }

    public String makeMagnetUri() {
        return dl.makeMagnetUri();
    }

    // TODO:BITTORRENT
    public TOTorrent getTOTorrent() {
        return null;
    }
}

// TODO:BITTORRENT
/*
public class BTDownloadImpl implements BTDownload {

    private DownloadManager _downloadManager;
    private boolean _partialDownload;
    private long _size;
    private Set<DiskManagerFileInfo> _fileInfoSet;
    private String _hash;

    private boolean _deleteTorrentWhenRemove;

    private boolean _deleteDataWhenRemove;
	private String _displayName;
    private final CopyrightLicenseBroker licenseBroker;
    private final PaymentOptions paymentOptions;


    public BTDownloadImpl(DownloadManager downloadManager) {
        _deleteTorrentWhenRemove = false;
        _deleteDataWhenRemove = false;

        BTInfoAditionalMetadataHolder holder = new BTInfoAditionalMetadataHolder(downloadManager, _displayName);
        licenseBroker = holder.getLicenseBroker();
        paymentOptions = holder.getPaymentOptions();
    }

    public void updateSize(DownloadManager downloadManager) {
		if (_partialDownload) {
            _fileInfoSet = TorrentUtil.getNoSkippedFileInfoSet(downloadManager);

            if (_fileInfoSet.isEmpty()) {
            	_size = downloadManager.getSize();
            } else {
	            long size = 0;
	            for (DiskManagerFileInfo fileInfo : _fileInfoSet) {
	                size += fileInfo.getLength();
	            }
            _size = size;
            }
        } else {
            _fileInfoSet = null;
            _size = downloadManager.getSize();
        }
	}

    public long getSize() {
        return getSize(false);
    }

    public long getSize(boolean update) {
    	if (update) {
    		updateSize(_downloadManager);
    	}
    	return _size;
    }

    public String getDisplayName() {
        return _displayName;
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

    public TransferState getState() {
        // TODO:BITTORRENT
        return TransferState.ERROR;//  _downloadManager.getState();
    }

    public void remove() {
        TorrentUtil.removeDownload(_downloadManager, _deleteTorrentWhenRemove, _deleteDataWhenRemove);
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
        if (_partialDownload) {
            long downloaded = 0;
            for (DiskManagerFileInfo fileInfo : _fileInfoSet) {
                downloaded += fileInfo.getDownloaded();
            }
            return (int) ((downloaded * 100) / _size);
        } else {
            return _downloadManager.getStats().getDownloadCompleted(true) / 10;
        }
    }

    public String getStateString() {
        return DisplayFormatters.formatDownloadStatus(_downloadManager);
    }

    public long getBytesReceived() {
        return _downloadManager.getStats().getTotalGoodDataBytesReceived();
    }

    public long getBytesSent() {
        return _downloadManager.getStats().getTotalDataBytesSent();
    }

    public double getDownloadSpeed() {
        return _downloadManager.getStats().getDataReceiveRate() / 1000;
    }

    public double getUploadSpeed() {
        return _downloadManager.getStats().getDataSendRate() / 1000;
    }

    public long getETA() {
        return _downloadManager.getStats().getETA();
    }

    public DownloadManager getDownloadManager() {
        return _downloadManager;
    }

    public String getPeersString() {
        long lTotalPeers = -1;
        long lConnectedPeers = 0;
        if (_downloadManager != null) {
            lConnectedPeers = _downloadManager.getNbPeers();

            if (lTotalPeers == -1) {
                TRTrackerScraperResponse response = _downloadManager.getTrackerScrapeResponse();
                if (response != null && response.isValid()) {
                    lTotalPeers = response.getPeers();
                }
            }
        }

        long totalPeers = lTotalPeers;
        if (totalPeers <= 0) {
            DownloadManager dm = _downloadManager;
            if (dm != null) {
                totalPeers = dm.getActivationCount();
            }
        }

        //        long value = lConnectedPeers * 10000000;
        //        if (totalPeers > 0)
        //            value = value + totalPeers;

        int state = _downloadManager.getState();
        boolean started = state == DownloadManager.STATE_SEEDING || state == DownloadManager.STATE_DOWNLOADING;
        boolean hasScrape = lTotalPeers >= 0;

        String tmp;
        if (started) {
            tmp = hasScrape ? (lConnectedPeers > lTotalPeers ? "%1" : "%1 " + "/" + " %2") : "%1";
        } else {
            tmp = hasScrape ? "%2" : "";
        }

        tmp = tmp.replaceAll("%1", String.valueOf(lConnectedPeers));
        tmp = tmp.replaceAll("%2", String.valueOf(totalPeers));

        return tmp;
    }

    public String getSeedsString() {
        long lTotalSeeds = -1;
        //long lTotalPeers = 0;
        long lConnectedSeeds = 0;
        DownloadManager dm = _downloadManager;
        if (dm != null) {
            lConnectedSeeds = dm.getNbSeeds();

            if (lTotalSeeds == -1) {
                TRTrackerScraperResponse response = dm.getTrackerScrapeResponse();
                if (response != null && response.isValid()) {
                    lTotalSeeds = response.getSeeds();
                    //lTotalPeers = response.getPeers();
                }
            }
        }

//        // Allows for 2097151 of each type (connected seeds, seeds, peers)
//        long value = (lConnectedSeeds << 42);
//        if (lTotalSeeds > 0)
//            value += (lTotalSeeds << 21);
//        if (lTotalPeers > 0)
//            value += lTotalPeers;

        //boolean bCompleteTorrent = dm == null ? false : dm.getAssumedComplete();

        int state = dm.getState();
        boolean started = (state == DownloadManager.STATE_SEEDING || state == DownloadManager.STATE_DOWNLOADING);
        boolean hasScrape = lTotalSeeds >= 0;
        String tmp;

        if (started) {
            tmp = hasScrape ? (lConnectedSeeds > lTotalSeeds ? "%1" : "%1 " + "/" + " %2") : "%1";
        } else {
            tmp = hasScrape ? "%2" : "";
        }
        tmp = tmp.replaceAll("%1", String.valueOf(lConnectedSeeds));
        String param2 = "?";
        if (lTotalSeeds != -1) {
            param2 = String.valueOf(lTotalSeeds);
        }
        tmp = tmp.replaceAll("%2", param2);

        return tmp;
    }

    public boolean isDeleteTorrentWhenRemove() {
        return _deleteTorrentWhenRemove;
    }

    public void setDeleteTorrentWhenRemove(boolean deleteTorrentWhenRemove) {
        _deleteTorrentWhenRemove = deleteTorrentWhenRemove;
    }

    public boolean isDeleteDataWhenRemove() {
        return _deleteDataWhenRemove;
    }

    public void setDeleteDataWhenRemove(boolean deleteDataWhenRemove) {
        _deleteDataWhenRemove = deleteDataWhenRemove;
    }

    public String getHash() {
        return _hash;
    }

    public String getSeedToPeerRatio() {
        float ratio = -1;

        DownloadManager dm = _downloadManager;
        if (dm != null) {
            TRTrackerScraperResponse response = dm.getTrackerScrapeResponse();
            int seeds;
            int peers;

            if (response != null && response.isValid()) {
                seeds = Math.max(dm.getNbSeeds(), response.getSeeds());

                int trackerPeerCount = response.getPeers();
                peers = dm.getNbPeers();
                if (peers == 0 || trackerPeerCount > peers) {
                    if (trackerPeerCount <= 0) {
                        peers = dm.getActivationCount();
                    } else {
                        peers = trackerPeerCount;
                    }
                }
            } else {
                seeds = dm.getNbSeeds();
                peers = dm.getNbPeers();
            }

            if (peers < 0 || seeds < 0) {
                ratio = 0;
            } else {
                if (peers == 0) {
                    if (seeds == 0)
                        ratio = 0;
                    else
                        ratio = Float.POSITIVE_INFINITY;
                } else {
                    ratio = (float) seeds / peers;
                }
            }
        }

        if (ratio == -1) {
            return "";
        } else if (ratio == 0) {
            return "??";
        } else {
            return DisplayFormatters.formatDecimal(ratio, 3);
        }
    }

    public String getShareRatio() {
        try {
            DownloadManager dm = _downloadManager;
            DownloadManagerStats stats = dm.getStats();

            int sr = (dm == null) ? 0 : stats.getShareRatio();

            if (sr == Integer.MAX_VALUE) {
                sr = Integer.MAX_VALUE - 1;
            }

            //If getShareRatio returns -1, it means total good downloaded
            //bytes is <= 0 this could also mean the user is re-starting an old torrent
            //that was already on disk, or downloaded with another client.
            if (sr == -1) {
                long downloaded = stats.getTotalGoodDataBytesReceived();
                long uploaded = stats.getTotalDataBytesSent();

                if (downloaded == 0 && uploaded > 0) {
                    sr = (int) ((1000 * uploaded) / dm.getDiskManager().getTotalLength());
                } else {
                    sr = Integer.MAX_VALUE;
                }
            }

            String shareRatio = "";

            if (sr == Integer.MAX_VALUE) {
                shareRatio = Constants.INFINITY_STRING;
            } else {
                shareRatio = DisplayFormatters.formatDecimal((double) sr / 1000, 3);
            }

            return shareRatio;
        } catch (Throwable e) {
            return "";
        }
    }

    public Date getDateCreated() {
        return new Date(_downloadManager.getCreationTime());
    }

    public boolean isPartialDownload() {
        return _partialDownload;
    }

    @Override
	public void updateDownloadManager(DownloadManager downloadManager) {
		_downloadManager = downloadManager;
        _partialDownload = TorrentUtil.isHandpicked(downloadManager);

        updateSize(downloadManager);

        updateName(downloadManager);

        try {
            _hash = TorrentUtil.hashToString(downloadManager.getTorrent().getHash());
        } catch (Exception e) {
        	e.printStackTrace();
            _hash = "";
        }

	}

    private void updateName(DownloadManager downloadManager) {
        if (TorrentUtil.getNoSkippedFileInfoSet(downloadManager).size() == 1) {
            try {
                byte[][] temp = TorrentUtil.getNoSkippedFileInfoSet(downloadManager).toArray(new DiskManagerFileInfo[0])[0].getTorrentFile().getPathComponents();
                _displayName = StringUtils.getUTF8String(temp[temp.length - 1]);
            } catch (Throwable e) {
                _displayName = TorrentUtil.getNoSkippedFileInfoSet(downloadManager).toArray(new DiskManagerFileInfo[0])[0].getFile(false).getName();
            }
        } else {
            _displayName = _downloadManager.getDisplayName();
        }
    }

    @Override
    public PaymentOptions getPaymentOptions() {
        paymentOptions.setItemName(getDisplayName());
        return paymentOptions;
    }

    @Override
    public CopyrightLicenseBroker getCopyrightLicenseBroker() {
        return licenseBroker;
    }
}
 */
