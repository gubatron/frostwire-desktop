/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2015, FrostWire(R). All rights reserved.
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

import java.io.File;
import java.util.Date;

import com.frostwire.core.FileDescriptor;
import com.frostwire.bittorrent.CopyrightLicenseBroker;
import com.frostwire.bittorrent.PaymentOptions;
import com.frostwire.transfers.TransferState;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class BTPeerHttpUpload implements BTDownload {

    private final PeerHttpUpload upload;

    public BTPeerHttpUpload(FileDescriptor fd) {
        this.upload = new PeerHttpUpload(fd);
    }

    public PeerHttpUpload getUpload() {
        return upload;
    }

    @Override
    public long getSize() {
        return upload.getSize();
    }

    @Override
    public String getName() {
        return upload.getDisplayName();
    }

    @Override
    public String getDisplayName() {
        return upload.getDisplayName();
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
        return upload.isComplete();
    }

    @Override
    public TransferState getState() {
        return isCompleted() ? TransferState.STOPPED : TransferState.SEEDING;
    }

    @Override
    public void remove() {
        upload.remove();
    }

    @Override
    public void pause() {
        upload.remove();
    }

    @Override
    public File getSaveLocation() {
        return new File(upload.getFD().filePath);
    }

    @Override
    public void resume() {
    }

    @Override
    public int getProgress() {
        return upload.getProgress();
    }

    @Override
    public long getBytesReceived() {
        return upload.getBytesReceived();
    }

    @Override
    public long getBytesSent() {
        return upload.getBytesSent();
    }

    @Override
    public double getDownloadSpeed() {
        return upload.getDownloadSpeed();
    }

    @Override
    public double getUploadSpeed() {
        return upload.getUploadSpeed() / 1000;
    }

    @Override
    public long getETA() {
        return upload.getETA();
    }

    @Override
    public String getPeersString() {
        return "";
    }

    @Override
    public String getSeedsString() {
        return "";
    }

    @Override
    public void setDeleteTorrentWhenRemove(boolean deleteTorrentWhenRemove) {
    }

    @Override
    public void setDeleteDataWhenRemove(boolean deleteDataWhenRemove) {
    }

    @Override
    public boolean deleteDataWhenRemoved() {
        return false;
    }

    @Override
    public String getHash() {
        return null;
    }

    @Override
    public String getSeedToPeerRatio() {
        return "";
    }

    @Override
    public String getShareRatio() {
        return "";
    }

    @Override
    public boolean isPartialDownload() {
        return false;
    }

    @Override
    public Date getDateCreated() {
        return upload.getCreated();
    }

    @Override
    public PaymentOptions getPaymentOptions() {
        return null;
    }

    @Override
    public CopyrightLicenseBroker getCopyrightLicenseBroker() {
        return null;
    }

    @Override
    public boolean canPreview() {
        return false;
    }

    @Override
    public File getPreviewFile() {
        return null;
    }
}
