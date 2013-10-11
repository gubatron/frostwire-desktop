/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
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

import java.awt.event.ActionListener;
import java.io.File;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloader;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderCallBackInterface;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderFactory;
import org.gudy.azureus2.core3.util.TorrentUtils;
import org.gudy.azureus2.core3.util.UrlUtils;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.util.BackgroundExecutorService;
import com.limegroup.gnutella.settings.SharingSettings;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class TorrentFetcherDownload implements BTDownload {

    private static final String STATE_DOWNLOADING = I18n.tr("Downloading Torrent");
    private static final String STATE_ERROR = I18n.tr("Error");
    private static final String STATE_CANCELED = I18n.tr("Canceled");

    private String _uri;
    private final String _torrentSaveDir;
    private TorrentDownloader _torrentDownloader;
    private final String _displayName;
    private final String _hash;
    private final long _size;
    private final boolean _partialDownload;
    private final ActionListener _postPartialDownloadAction;
    private final Date dateCreated;

    private String _state;
    private boolean _removed;
    private BTDownload _delegate;
    private String relativePath;

    private boolean[] filesSelection;

    public TorrentFetcherDownload(String uri, String referrer, String displayName, String hash, long size, boolean partialDownload, ActionListener postPartialDownloadAction, final String relativePath) {
        _uri = uri;
        _torrentSaveDir = SharingSettings.TORRENTS_DIR_SETTING.getValue().getAbsolutePath();
        _torrentDownloader = TorrentDownloaderFactory.create(new TorrentDownloaderListener(), _uri, referrer, _torrentSaveDir);
        _displayName = displayName;
        _hash = hash != null ? hash : "";
        _size = size;
        _partialDownload = partialDownload;
        _postPartialDownloadAction = postPartialDownloadAction;
        this.dateCreated = new Date();
        this.relativePath = relativePath;

        _state = STATE_DOWNLOADING;

        if (_hash.length() > 0 && relativePath != null) {
            if (!isDownloadingTorrent(_hash)) {
                _torrentDownloader.start();
            } else {
                BackgroundExecutorService.schedule(new WaitForTorrentReady());
            }
        } else { // best effort
            _torrentDownloader.start();
        }
    }

    private boolean isDownloadingTorrent(String hash) {
        for (BTDownload d : BTDownloadMediator.instance().getDownloads()) {
            if (d != this && d.getHash() != null && d.getHash().equals(_hash)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDownloadingTorrentReady(String hash) {
        for (BTDownload d : BTDownloadMediator.instance().getDownloads()) {
            if (d != this && d.getHash() != null && d.getHash().equals(_hash)) {
                DownloadManager dm = d.getDownloadManager();
                if (dm != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public TorrentFetcherDownload(String uri, boolean partialDownload, ActionListener postPartialDownloadAction) {
        this(uri, null, getDownloadNameFromMagnetURI(uri), "", -1, partialDownload, postPartialDownloadAction, null);
    }

    public TorrentFetcherDownload(String uri, String relativePath, ActionListener postPartialDownloadAction) {
        this(uri, null, getDownloadNameFromMagnetURI(uri), "", -1, true, postPartialDownloadAction, relativePath);
    }

    public TorrentFetcherDownload(String uri, String referrer, String relativePath, String hash, ActionListener postPartialDownloadAction) {
        this(uri, referrer, getDownloadNameFromMagnetURI(uri), hash, -1, true, postPartialDownloadAction, relativePath);
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

    public long getSize() {
        return _delegate != null ? _delegate.getSize() : _size;
    }

    public String getDisplayName() {
        return _delegate != null ? _delegate.getDisplayName() : _displayName;
    }

    public boolean isResumable() {
        return _delegate != null ? _delegate.isResumable() : false;
    }

    public boolean isPausable() {
        return _delegate != null ? _delegate.isPausable() : false;
    }

    public boolean isCompleted() {
        return _delegate != null ? _delegate.isCompleted() : false;
    }

    public int getState() {
        return _delegate != null ? _delegate.getState() : -1;
    }

    public void remove() {
        if (_delegate != null) {
            _delegate.remove();
        } else {
            _removed = true;
            try {
                _torrentDownloader.cancel();
            } catch (Throwable e) {
                // ignore, I can't do anything
                //e.printStackTrace();
            }
            try {
                _torrentDownloader.getFile().delete();
            } catch (Throwable e) {
                // ignore, I can't do anything
                //e.printStackTrace();
            }
        }
    }

    public void pause() {
        if (_delegate != null) {
            _delegate.pause();
        }
    }

    public void resume() {
        if (_delegate != null) {
            _delegate.resume();
        }
    }

    public File getSaveLocation() {
        return _delegate != null ? _delegate.getSaveLocation() : null;
    }

    public int getProgress() {
        return _delegate != null ? _delegate.getProgress() : 0;
    }

    public String getStateString() {
        return _delegate != null ? _delegate.getStateString() : _state;
    }

    public long getBytesReceived() {
        return _delegate != null ? _delegate.getBytesReceived() : 0;
    }

    public long getBytesSent() {
        return _delegate != null ? _delegate.getBytesSent() : 0;
    }

    public double getDownloadSpeed() {
        return _delegate != null ? _delegate.getDownloadSpeed() : 0;
    }

    public double getUploadSpeed() {
        return _delegate != null ? _delegate.getUploadSpeed() : 0;
    }

    public long getETA() {
        return _delegate != null ? _delegate.getETA() : 0;
    }

    public DownloadManager getDownloadManager() {
        return _delegate != null ? _delegate.getDownloadManager() : null;
    }

    public String getPeersString() {
        return _delegate != null ? _delegate.getPeersString() : "";
    }

    public String getSeedsString() {
        return _delegate != null ? _delegate.getSeedsString() : "";
    }

    public boolean isDeleteTorrentWhenRemove() {
        return _delegate != null ? _delegate.isDeleteTorrentWhenRemove() : false;
    }

    public void setDeleteTorrentWhenRemove(boolean deleteTorrentWhenRemove) {
        if (_delegate != null) {
            _delegate.setDeleteTorrentWhenRemove(deleteTorrentWhenRemove);
        }
    }

    public boolean isDeleteDataWhenRemove() {
        return _delegate != null ? _delegate.isDeleteDataWhenRemove() : false;
    }

    public void setDeleteDataWhenRemove(boolean deleteDataWhenRemove) {
        if (_delegate != null) {
            _delegate.setDeleteDataWhenRemove(deleteDataWhenRemove);
        }
    }

    public String getHash() {
        return _delegate != null ? _delegate.getHash() : _hash;
    }

    public String getSeedToPeerRatio() {
        return _delegate != null ? _delegate.getSeedToPeerRatio() : "";
    }

    public String getShareRatio() {
        return _delegate != null ? _delegate.getShareRatio() : "";
    }

    public Date getDateCreated() {
        return _delegate != null ? _delegate.getDateCreated() : dateCreated;
    }

    public boolean isPartialDownload() {
        return _delegate != null ? _delegate.isPartialDownload() : _partialDownload;
    }

    private final class TorrentDownloaderListener implements TorrentDownloaderCallBackInterface {
        private AtomicBoolean finished = new AtomicBoolean(false);

        public void TorrentDownloaderEvent(int state, final TorrentDownloader inf) {
            if (_removed) {
                return;
            }
            if (state == TorrentDownloader.STATE_FINISHED && finished.compareAndSet(false, true)) {
                onTorrentDownloaderFinished(inf.getFile());
            } else if (state == TorrentDownloader.STATE_ERROR) {
                if (_hash != null && _hash.trim() != "" && (_uri.toLowerCase().startsWith("http://") || _uri.toLowerCase().startsWith("https://"))) {
                    _uri = TorrentUtil.getMagnet(_hash);
                    _torrentDownloader = TorrentDownloaderFactory.create(new TorrentDownloaderListener(), _uri, null, _torrentSaveDir);
                    _state = STATE_DOWNLOADING;
                    _torrentDownloader.start();
                } else {
                    _state = STATE_ERROR;
                }
            }
        }

    }

    private void cancelDownload() {
        _state = STATE_CANCELED;
        GUIMediator.safeInvokeLater(new Runnable() {
            public void run() {
                BTDownloadMediator.instance().remove(TorrentFetcherDownload.this);
            }
        });
    }

    @Override
    public long getSize(boolean update) {
        return _delegate != null ? _delegate.getSize(update) : _size;
    }

    @Override
    public void updateDownloadManager(DownloadManager downloadManager) {
        if (_delegate != null) {
            _delegate.updateDownloadManager(downloadManager);
        }
    }

    private void onTorrentDownloaderFinished(final File torrentFile) {
        try {
            //single file download straight from a torrent deep search.
            if (relativePath != null) {
                TOTorrent torrent = TorrentUtils.readFromFile(torrentFile, false);

                filesSelection = new boolean[torrent.getFiles().length];
                for (int i = 0; i < filesSelection.length; i++) {
                    filesSelection[i] = torrent.getFiles()[i].getRelativePath().equals(relativePath);
                }

            } else if (_partialDownload) {
                GUIMediator.safeInvokeAndWait(new Runnable() {
                    public void run() {
                        try {

                            PartialFilesDialog dlg = new PartialFilesDialog(GUIMediator.getAppFrame(), torrentFile);

                            dlg.setVisible(true);
                            filesSelection = dlg.getFilesSelection();
                        } catch (TOTorrentException e) {
                            System.out.println("Error reading torrent:" + e.getMessage());
                            filesSelection = null;
                        }
                    }
                });
                if (filesSelection == null) {
                    cancelDownload();
                    return;
                } else {
                    if (_postPartialDownloadAction != null) {
                        try {
                            _postPartialDownloadAction.actionPerformed(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            BTDownloadCreator creator = new BTDownloadCreator(torrentFile, filesSelection);
            _delegate = creator.createDownload();

            if (_delegate instanceof DuplicateDownload) {
                cancelDownload();
                GUIMediator.safeInvokeLater(new Runnable() {

                    @Override
                    public void run() {
                        BTDownloadMediator.instance().selectRowByDownload(_delegate);
                    }
                });
            }

        } catch (Exception e) {
            _state = STATE_ERROR;
            e.printStackTrace();
        }
    }

    private final class WaitForTorrentReady implements Runnable {

        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            } while (!isDownloadingTorrentReady(_hash));

            try {
                BTDownloadCreator creator = new BTDownloadCreator(_hash, relativePath);
                _delegate = creator.createDownload();

                if (_delegate instanceof DuplicateDownload) {
                    cancelDownload();
                    GUIMediator.safeInvokeLater(new Runnable() {

                        @Override
                        public void run() {
                            BTDownloadMediator.instance().selectRowByDownload(_delegate);
                        }
                    });
                }
            } catch (Throwable e) {
                _state = STATE_ERROR;
                e.printStackTrace();
            }
        }
    }
}
