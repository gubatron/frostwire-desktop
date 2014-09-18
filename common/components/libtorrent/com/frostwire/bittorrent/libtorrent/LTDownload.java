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
import com.frostwire.bittorrent.BTDownloadListener;
import com.frostwire.bittorrent.BTDownloadState;
import com.frostwire.jlibtorrent.*;
import com.frostwire.jlibtorrent.alerts.TorrentFinishedAlert;
import com.frostwire.logging.Logger;

import java.io.File;
import java.util.Date;

/**
 * @author gubatron
 * @author aldenml
 */
public final class LTDownload extends TorrentAlertAdapter implements BTDownload {

    private static final Logger LOG = Logger.getLogger(LTDownload.class);

    private final TorrentHandle th;

    private BTDownloadListener listener;

    public LTDownload(TorrentHandle th) {
        super(th);
        this.th = th;

        LTEngine.getInstance().getSession().addListener(this);
    }

    @Override
    public String getName() {
        return th.getDisplayName();
    }

    @Override
    public long getSize() {
        TorrentInfo ti = th.getTorrentInfo();
        return ti != null ? ti.getTotalSize() : 0;
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
        return th.getInfoHash();
    }

    @Override
    public Date getDateCreated() {
        return new Date(th.getStatus().addedTime);
    }

    @Override
    public long getETA() {
        TorrentInfo ti = th.getTorrentInfo();
        if (ti == null) {
            return 0;
        }

        TorrentStatus status = th.getStatus();
        long left = ti.getTotalSize() - status.totalDone;
        long rate = status.downloadPayloadRate;

        if (left <= 0) {
            return 0;
        }

        if (rate <= 0) {
            return -1;
        }

        return left / rate;
    }

    @Override
    public void pause() {
        th.pause();
    }

    @Override
    public void resume() {
        th.resume();
    }

    @Override
    public void stop() {
        this.stop(false, false);
    }

    @Override
    public void stop(boolean deleteTorrent, boolean deleteData) {
        LTEngine engine = LTEngine.getInstance();
        Session s = engine.getSession();

        Session.Options options = Session.Options.NONE;
        if (deleteData) {
            options = Session.Options.DELETE_FILES;
        }

        s.removeListener(this);
        s.removeTorrent(th, options);

        String infoHash = this.getInfoHash();

        if (deleteTorrent) {
            File torrent = LTEngine.getInstance().readTorrentPath(infoHash);
            if (torrent.exists()) {
                torrent.delete();
            }
        }

        engine.resumeDataFile(infoHash).delete();
        engine.resumeTorrentFile(infoHash).delete();
    }

    @Override
    public BTDownloadListener getListener() {
        return listener;
    }

    @Override
    public void setListener(BTDownloadListener listener) {
        this.listener = listener;
    }

    TorrentHandle getTorrentHandle() {
        return th;
    }

    @Override
    public void onTorrentFinished(TorrentFinishedAlert alert) {
        if (listener != null) {
            try {
                listener.finished(this);
            } catch (Throwable e) {
                LOG.error("Error calling listener", e);
            }
        }
    }

    @Override
    public boolean isPartial() {
        return th.isPartial();
    }
}
