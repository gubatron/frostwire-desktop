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

package com.frostwire.vuze;

import java.io.File;
import java.util.Date;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.util.DisplayFormatters;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeDownloadManager {

    private final DownloadManager dm;

    private final String displayName;
    private final String hash;
    private final long size;
    private final File savePath;
    private final Date creationDate;

    public VuzeDownloadManager(DownloadManager dm) {
        this.dm = dm;

        this.displayName = dm.getDisplayName();
        this.hash = TorrentUtil.hashToString(dm.getTorrent().getHash());
        this.size = dm.getSize();
        this.savePath = dm.getSaveLocation();
        this.creationDate = new Date(dm.getCreationTime());
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getHash() {
        return hash;
    }

    public long getSize() {
        return size;
    }

    public File getSavePath() {
        return savePath;
    }

    /**
     * This is method should be used with care, since Date is mutable, the user
     * of this class could mess with the inner state of the object.
     * 
     * @return
     */
    public Date getCreationDate() {
        return creationDate;
    }

    public String getStatus() {
        return DisplayFormatters.formatDownloadStatus(dm);
    }

    public int getDownloadCompleted() {
        return dm.getStats().getDownloadCompleted(true) / 10;
    }

    public boolean isDownloading() {
        return dm.getState() == DownloadManager.STATE_DOWNLOADING;
    }

    public boolean isSeeding() {
        return dm.getState() == DownloadManager.STATE_SEEDING;
    }

    public long getBytesReceived() {
        return dm.getStats().getTotalGoodDataBytesReceived();
    }

    public long getBytesSent() {
        return dm.getStats().getTotalDataBytesSent();
    }

    public long getDownloadSpeed() {
        return dm.getStats().getDataReceiveRate();
    }

    public long getUploadSpeed() {
        return dm.getStats().getDataSendRate();
    }

    public long getETA() {
        return dm.getStats().getETA();
    }
}
