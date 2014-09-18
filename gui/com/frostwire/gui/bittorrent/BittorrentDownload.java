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
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.iTunesMediator;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.settings.iTunesSettings;
import org.gudy.azureus2.core3.download.DownloadManager;
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
    public int getState() {
        return dl.getState().ordinal();
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
    public String getStateString() {
        return dl.getState().name();
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
    public DownloadManager getDownloadManager() {
        return null;
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
    public void updateDownloadManager(DownloadManager downloadManager) {

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
}
