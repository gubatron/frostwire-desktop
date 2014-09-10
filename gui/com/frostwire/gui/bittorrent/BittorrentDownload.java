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
import com.frostwire.torrent.CopyrightLicenseBroker;
import com.frostwire.torrent.PaymentOptions;
import org.gudy.azureus2.core3.download.DownloadManager;

import java.io.File;
import java.util.Date;

/**
 * @author gubatron
 * @author aldenml
 */
public class BittorrentDownload implements com.frostwire.gui.bittorrent.BTDownload {

    private final BTDownload d;

    private boolean deleteTorrentWhenRemove;
    private boolean deleteDataWhenRemove;

    public BittorrentDownload(BTDownload d) {
        this.d = d;
    }

    @Override
    public long getSize() {
        return d.getSize();
    }

    @Override
    public long getSize(boolean update) {
        return d.getSize();
    }

    @Override
    public String getDisplayName() {
        return d.getName();
    }

    @Override
    public boolean isResumable() {
        return d.isPaused();
    }

    @Override
    public boolean isPausable() {
        return !d.isPaused();
    }

    @Override
    public boolean isCompleted() {
        return d.isFinished();
    }

    @Override
    public int getState() {
        return d.getState().ordinal();
    }

    @Override
    public void remove() {
        d.stop(deleteTorrentWhenRemove, deleteDataWhenRemove);
    }

    @Override
    public void pause() {
        d.pause();
    }

    @Override
    public File getSaveLocation() {
        return new File(d.getSavePath());
    }

    @Override
    public void resume() {
        d.resume();
    }

    @Override
    public int getProgress() {
        return d.getProgress();
    }

    @Override
    public String getStateString() {
        return d.getState().name();
    }

    @Override
    public long getBytesReceived() {
        return d.getBytesReceived();
    }

    @Override
    public long getBytesSent() {
        return d.getBytesSent();
    }

    @Override
    public double getDownloadSpeed() {
        return d.getDownloadSpeed();
    }

    @Override
    public double getUploadSpeed() {
        return d.getUploadSpeed();
    }

    @Override
    public long getETA() {
        return 0;
    }

    @Override
    public DownloadManager getDownloadManager() {
        return null;
    }

    @Override
    public String getPeersString() {
        return d.getConnectedPeers() + "/" + d.getTotalPeers();
    }

    @Override
    public String getSeedsString() {
        return d.getConnectedSeeds() + "/" + d.getTotalSeeds();
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
        return d.getInfoHash();
    }

    @Override
    public String getSeedToPeerRatio() {
        return d.getTotalSeeds() + "/" + d.getTotalPeers();
    }

    @Override
    public String getShareRatio() {
        long sent = d.getTotalBytesSent();
        long received = d.getTotalBytesReceived();

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
        return d.getDateCreated();
    }

    @Override
    public PaymentOptions getPaymentOptions() {
        return null;
    }

    @Override
    public CopyrightLicenseBroker getCopyrightLicenseBroker() {
        return null;
    }
}
