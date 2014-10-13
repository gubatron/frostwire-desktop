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
import com.frostwire.transfers.TransferItem;
import com.frostwire.transfers.TransferState;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.iTunesMediator;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.settings.iTunesImportSettings;
import com.limegroup.gnutella.settings.iTunesSettings;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentFactory;
import org.limewire.util.FileUtils;
import org.limewire.util.OSUtils;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author gubatron
 * @author aldenml
 */
public class BittorrentDownload implements com.frostwire.gui.bittorrent.BTDownload {

    private static final Logger LOG = Logger.getLogger(BittorrentDownload.class);

    private final BTDownload dl;

    private String displayName;
    private long size;
    private boolean partial;

    private boolean deleteTorrentWhenRemove;
    private boolean deleteDataWhenRemove;

    private BTInfoAditionalMetadataHolder holder;
    private CopyrightLicenseBroker licenseBroker;
    private PaymentOptions paymentOptions;

    public BittorrentDownload(BTDownload dl) {
        this.dl = dl;
        this.dl.setListener(new StatusListener());

        this.displayName = dl.getDisplayName();
        this.size = calculateSize(dl);
        this.partial = dl.isPartial();

        if (!dl.wasPaused()) {
            dl.resume();
        }
    }

    public BTDownload getDl() {
        return dl;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public String getDisplayName() {
        return displayName;
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
        dl.remove(deleteTorrentWhenRemove, deleteDataWhenRemove);
    }

    @Override
    public void pause() {
        dl.pause();
    }

    @Override
    public File getSaveLocation() {
        return dl.getSavePath();
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
    public void setDeleteTorrentWhenRemove(boolean deleteTorrentWhenRemove) {
        this.deleteTorrentWhenRemove = deleteTorrentWhenRemove;
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
        return partial;
    }

    @Override
    public Date getDateCreated() {
        return dl.getCreated();
    }

    @Override
    public PaymentOptions getPaymentOptions() {
        setupMetadataHolder();
        return paymentOptions;
    }

    @Override
    public CopyrightLicenseBroker getCopyrightLicenseBroker() {
        setupMetadataHolder();
        return licenseBroker;
    }

    private class StatusListener implements BTDownloadListener {

        @Override
        public void update(BTDownload dl) {
            displayName = dl.getDisplayName();
            size = calculateSize(dl);
            partial = dl.isPartial();
        }

        @Override
        public void finished(BTDownload dl) {
            if (!SharingSettings.SEED_FINISHED_TORRENTS.getValue() || (dl.isPartial() && !SharingSettings.SEED_HANDPICKED_TORRENT_FILES.getValue())) {
                dl.pause();
            }

            File saveLocation = dl.getSavePath();

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
        public void removed(BTDownload dl, Set<File> incompleteFiles) {
            finalCleanup(incompleteFiles);
        }
    }

    public String makeMagnetUri() {
        return dl.makeMagnetUri();
    }

    public TOTorrent getTOTorrent() {
        try {
            File torrent = dl.getTorrentFile();
            return TOTorrentFactory.deserialiseFromBEncodedFile(torrent);
        } catch (Throwable e) {
            LOG.error("Error building vuze torrent from file", e);
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

                if (paymentOptions != null) {
                    paymentOptions.setItemName(getDisplayName());
                }
            } catch (Throwable e) {
                LOG.error("Unable to setup licence holder");
            }
        }
    }

    //Deletes incomplete files and the save location from the itunes import settings
    private void finalCleanup(Set<File> incompleteFiles) {
        for (File f : incompleteFiles) {
            try {
                if (f.exists() && !f.delete()) {
                    LOG.warn("Can't delete file: " + f);
                }
            } catch (Throwable e) {
                LOG.warn("Can't delete file: " + f + ", ex: " + e.getMessage());
            }
        }
        File saveLocation = dl.getSavePath();
        FileUtils.deleteEmptyDirectoryRecursive(saveLocation);
        iTunesImportSettings.IMPORT_FILES.remove(saveLocation);
    }

    private long calculateSize(BTDownload dl) {
        long size = dl.getSize();

        boolean partial = dl.isPartial();
        if (partial) {
            List<TransferItem> items = dl.getItems();

            long totalSize = 0;
            for (TransferItem item : items) {
                if (!item.isSkipped()) {
                    totalSize += item.getSize();
                }
            }

            if (totalSize > 0) {
                size = totalSize;
            }
        }

        return size;
    }
}
