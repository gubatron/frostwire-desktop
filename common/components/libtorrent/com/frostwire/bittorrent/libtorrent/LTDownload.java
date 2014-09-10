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

package com.frostwire.bittorrent.libtorrent;

import com.frostwire.bittorrent.BTDownload;
import com.frostwire.bittorrent.BTDownloadState;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.TorrentStatus;

import java.util.Date;

/**
 * @author gubatron
 * @author aldenml
 */
public final class LTDownload implements BTDownload {

    private final TorrentHandle th;

    private final TorrentInfo ti;
    private final String name;
    private final long size;

    public LTDownload(TorrentHandle th) {
        this.th = th;

        this.ti = this.th.getTorrentInfo();
        this.name = this.ti.getName();
        this.size = this.ti.getTotalSize();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public boolean isPaused() {
        return th.isPaused();
    }

    @Override
    public boolean isSeeding() {
        return th.isSeeding();
    }

    @Override
    public boolean isFinished() {
        return th.isFinished();
    }

    @Override
    public BTDownloadState getState() {
        TorrentStatus.State state = th.getStatus().state;

        if (th.isPaused()) {
            return BTDownloadState.PAUSED;
        }

        switch (state) {
            case QUEUED_FOR_CHECKING:
                return BTDownloadState.QUEUED_FOR_CHECKING;
            case CHECKING_FILES:
                return BTDownloadState.CHECKING_FILES;
            case DOWNLOADING_METADATA:
                return BTDownloadState.DOWNLOADING_METADATA;
            case DOWNLOADING:
                return BTDownloadState.DOWNLOADING;
            case FINISHED:
                return BTDownloadState.FINISHED;
            case SEEDING:
                return BTDownloadState.SEEDING;
            case ALLOCATING:
                return BTDownloadState.ALLOCATING;
            case CHECKING_RESUME_DATA:
                return BTDownloadState.CHECKING_RESUME_DATA;
            default:
                throw new IllegalArgumentException("No enum value supported");
        }
    }

    @Override
    public String getSavePath() {
        return th.getSavePath();
    }

    @Override
    public int getProgress() {
        float fp = th.getStatus().progress;

        if (Float.compare(fp, 1f) == 0) {
            return 100;
        }

        int p = (int) (th.getStatus().progress * 100);
        return Math.min(p, 100);
    }

    @Override
    public long getBytesReceived() {
        return th.getStatus().totalDownload;
    }

    @Override
    public long getTotalBytesReceived() {
        return th.getStatus().allTimeDownload;
    }

    @Override
    public long getBytesSent() {
        return th.getStatus().totalUpload;
    }

    @Override
    public long getTotalBytesSent() {
        return th.getStatus().allTimeUpload;
    }

    @Override
    public float getDownloadSpeed() {
        return th.getStatus().downloadRate;
    }

    @Override
    public float getUploadSpeed() {
        return th.getStatus().uploadRate;
    }

    @Override
    public int getConnectedPeers() {
        return th.getStatus().numPeers;
    }

    @Override
    public int getTotalPeers() {
        return th.getStatus().listPeers;
    }

    @Override
    public int getConnectedSeeds() {
        return th.getStatus().numSeeds;
    }

    @Override
    public int getTotalSeeds() {
        return th.getStatus().listSeeds;
    }

    @Override
    public String getInfoHash() {
        return th.getTorrentInfo().getInfoHash();
    }

    @Override
    public Date getDateCreated() {
        return new Date(th.getStatus().addedTime);
    }

    @Override
    public void pause() {
        th.pause();
    }

    @Override
    public void resume() {
        th.resume();
    }
}
