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

import com.frostwire.bittorrent.BTEngine;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.logging.Logger;
import com.frostwire.torrent.CopyrightLicenseBroker;
import com.frostwire.torrent.PaymentOptions;
import com.frostwire.transfers.TransferState;
import com.frostwire.util.HttpClientFactory;
import com.limegroup.gnutella.gui.GUIMediator;
import org.gudy.azureus2.core3.util.UrlUtils;

import java.io.File;
import java.util.Date;

/**
 * @author gubatron
 * @author aldenml
 */
public class TorrentFetcherDownload implements BTDownload {

    private static final Logger LOG = Logger.getLogger(TorrentFetcherDownload.class);

    private final String uri;
    private final String referer;
    private final String displayName;
    private final boolean partial;

    private final Date dateCreated;

    private TransferState state;

    public TorrentFetcherDownload(String uri, String referrer, String displayName, boolean partial) {
        this.uri = uri;
        this.referer = referrer;
        this.displayName = displayName;
        this.partial = partial;

        this.dateCreated = new Date();

        state = TransferState.DOWNLOADING_TORRENT;

        Thread t = new Thread(new FetcherRunnable(), "Torrent-Fetcher - " + uri);
        t.setDaemon(true);
        t.start();
    }

    public TorrentFetcherDownload(String uri, boolean partial) {
        this(uri, null, getDownloadNameFromMagnetURI(uri), partial);
    }

    public long getSize() {
        return -1;
    }

    @Override
    public String getName() {
        return null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isResumable() {
        return false;
    }

    public boolean isPausable() {
        return false;
    }

    public boolean isCompleted() {
        return false;
    }

    public TransferState getState() {
        return state;
    }

    public void remove() {
        state = TransferState.CANCELED;
    }

    public void pause() {
    }

    public void resume() {
    }

    public File getSaveLocation() {
        return null;
    }

    public int getProgress() {
        return 0;
    }

    public long getBytesReceived() {
        return 0;
    }

    public long getBytesSent() {
        return 0;
    }

    public double getDownloadSpeed() {
        return 0;
    }

    public double getUploadSpeed() {
        return 0;
    }

    public long getETA() {
        return 0;
    }

    public String getPeersString() {
        return "";
    }

    public String getSeedsString() {
        return "";
    }

    public boolean isDeleteTorrentWhenRemove() {
        return false;
    }

    public void setDeleteTorrentWhenRemove(boolean deleteTorrentWhenRemove) {
    }

    public boolean isDeleteDataWhenRemove() {
        return false;
    }

    public void setDeleteDataWhenRemove(boolean deleteDataWhenRemove) {
    }

    public String getHash() {
        return null;
    }

    public String getSeedToPeerRatio() {
        return "";
    }

    public String getShareRatio() {
        return "";
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public boolean isPartialDownload() {
        return false;
    }

    public long getSize(boolean update) {
        return -1;
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

    private void cancel() {
        state = TransferState.CANCELED;
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                BTDownloadMediator.instance().remove(TorrentFetcherDownload.this);
            }
        });
    }

    private void downloadTorrent(final byte[] data) {
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                try {

                    boolean[] selection = null;

                    if (partial) {
                        PartialFilesDialog dlg = new PartialFilesDialog(GUIMediator.getAppFrame(), data, displayName);
                        dlg.setVisible(true);
                        selection = dlg.getFilesSelection();
                        if (selection == null) {
                            return;
                        }
                    }

                    TorrentInfo ti = TorrentInfo.bdecode(data);
                    BTEngine.getInstance().download(ti, null, selection);

                } catch (Throwable e) {
                    LOG.error("Error downloading torrent", e);
                }
            }
        });
    }

    private static String getDownloadNameFromMagnetURI(String uri) {
        if (!uri.startsWith("magnet:")) {
            return uri;
        }

        if (uri.contains("dn=")) {
            String[] split = uri.split("&");
            for (String s : split) {
                if (s.toLowerCase().startsWith("dn=") && s.length() > 3) {
                    return UrlUtils.decode(s.split("=")[1]);
                }
            }
        }

        return uri;
    }

    private class FetcherRunnable implements Runnable {

        @Override
        public void run() {
            if (state == TransferState.CANCELED) {
                return;
            }

            try {
                byte[] data = null;
                if (uri.startsWith("http")) {
                    // use our http client, since we can handle referer
                    data = HttpClientFactory.newInstance().getBytes(uri, 30000, referer);
                } else {
                    data = BTEngine.getInstance().fetchMagnet(uri, 90000);
                }

                if (state == TransferState.CANCELED) {
                    return;
                }

                if (data != null) {
                    try {
                        downloadTorrent(data);
                    } finally {
                        cancel();
                    }
                } else {
                    state = TransferState.ERROR_NOT_ENOUGH_PEERS;
                }
            } catch (Throwable e) {
                state = TransferState.ERROR;
                LOG.error("Error downloading torrent from uri", e);
            }
        }
    }
}
