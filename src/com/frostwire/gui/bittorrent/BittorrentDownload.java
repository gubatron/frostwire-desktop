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
import com.frostwire.logging.Logger;
import com.frostwire.torrent.CopyrightLicenseBroker;
import com.frostwire.torrent.PaymentOptions;
import com.frostwire.transfers.TransferState;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.iTunesMediator;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.settings.iTunesSettings;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.torrent.TOTorrentFactory;
import org.limewire.util.OSUtils;

import java.io.File;
import java.util.Date;

/**
 * @author gubatron
 * @author aldenml
 */
public class BittorrentDownload implements com.frostwire.gui.bittorrent.BTDownload {

    private static final Logger LOG = Logger.getLogger(BittorrentDownload.class);

    private final BTDownload dl;

    private boolean deleteTorrentWhenRemove;
    private boolean deleteDataWhenRemove;

    private BTInfoAditionalMetadataHolder holder;
    private CopyrightLicenseBroker licenseBroker;
    private PaymentOptions paymentOptions;

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
        // TODO:BITTORRENT
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
        return dl.getDownloadSpeed() / 1024;
    }

    @Override
    public double getUploadSpeed() {
        return dl.getUploadSpeed() / 1024;
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
        return dl.isPartial();
    }

    @Override
    public Date getDateCreated() {
        return dl.getDateCreated();
    }

    @Override
    public PaymentOptions getPaymentOptions() {
        setupMetadataHolder();
        if (paymentOptions != null) {
            paymentOptions.setItemName(getDisplayName());
        }

        return paymentOptions;
    }

    @Override
    public CopyrightLicenseBroker getCopyrightLicenseBroker() {
        setupMetadataHolder();
        return licenseBroker;
    }

    public void refresh() {
        // TODO:BITTORRENT
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

        @Override
        public void stopped(BTDownload dl) {
            // TODO:BITTORRENT
            // check it works
            long timeStarted = getDateCreated().getTime();
            if (TorrentUtil.isHandpicked(dl) &&
                    (!SharingSettings.SEED_FINISHED_TORRENTS.getValue() || !SharingSettings.SEED_HANDPICKED_TORRENT_FILES.getValue()) &&
                    dl.isFinished()) {
                TorrentUtil.finalCleanup(dl, timeStarted);
            }
        }
    }

    public String makeMagnetUri() {
        return dl.makeMagnetUri();
    }

    public TOTorrent getTOTorrent() {
        File torrent = dl.getTorrentFile();
        try {
            return TOTorrentFactory.deserialiseFromBEncodedFile(torrent);
        } catch (TOTorrentException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setupMetadataHolder() {
        if (holder == null) {
            try {
                File torrent = dl.getTorrentFile();
                holder = new BTInfoAditionalMetadataHolder(torrent, getDisplayName());
                licenseBroker = holder.getLicenseBroker();
                paymentOptions = holder.getPaymentOptions();
            } catch (Throwable e) {
                LOG.error("Unable to setup licence holder");
            }
        }
    }
}

// TODO:BITTORRENT
/*
public class BTDownloadImpl implements BTDownload {


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

}
 */
