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
        return false;
    }

    @Override
    public boolean isPausable() {
        return false;
    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public void remove() {

    }

    @Override
    public void pause() {

    }

    @Override
    public File getSaveLocation() {
        return null;
    }

    @Override
    public void resume() {

    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public String getStateString() {
        return null;
    }

    @Override
    public long getBytesReceived() {
        return 0;
    }

    @Override
    public long getBytesSent() {
        return 0;
    }

    @Override
    public double getDownloadSpeed() {
        return 0;
    }

    @Override
    public double getUploadSpeed() {
        return 0;
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
        return null;
    }

    @Override
    public String getSeedsString() {
        return null;
    }

    @Override
    public boolean isDeleteTorrentWhenRemove() {
        return false;
    }

    @Override
    public void setDeleteTorrentWhenRemove(boolean deleteTorrentWhenRemove) {

    }

    @Override
    public boolean isDeleteDataWhenRemove() {
        return false;
    }

    @Override
    public void setDeleteDataWhenRemove(boolean deleteDataWhenRemove) {

    }

    @Override
    public String getHash() {
        return null;
    }

    @Override
    public String getSeedToPeerRatio() {
        return null;
    }

    @Override
    public String getShareRatio() {
        return null;
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
        return null;
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
