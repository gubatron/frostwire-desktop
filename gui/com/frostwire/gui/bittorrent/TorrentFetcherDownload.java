package com.frostwire.gui.bittorrent;

import java.io.File;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloader;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderCallBackInterface;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderFactory;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.SharingSettings;

public class TorrentFetcherDownload implements BTDownload {

    private static final String STATE_DOWNLOADING = I18n.tr("Downloading Torrent");
    private static final String STATE_ERROR = I18n.tr("Error");
    private static final String STATE_DUPLICATED = I18n.tr("Duplicated");

    private final TorrentDownloader _torrentDownloader;
    private final String _displayName;
    private final String _hash;
    private final long _size;

    private String _state;
    private BTDownload _delegate;

    public TorrentFetcherDownload(String uri, String displayName, String hash, long size) {
        String saveDir = SharingSettings.TORRENTS_DIR_SETTING.getValue().getAbsolutePath();
        _torrentDownloader = TorrentDownloaderFactory.create(new TorrentDownloaderListener(), uri, null, saveDir);
        _torrentDownloader.start();

        _displayName = displayName;
        _hash = hash;
        _size = size;

        _state = STATE_DOWNLOADING;
    }

    public TorrentFetcherDownload(String uri) {
        this(uri, uri, "", -1);
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

    public boolean isPartialDownload() {
        return _delegate != null ? _delegate.isPartialDownload() : false;
    }

    private final class TorrentDownloaderListener implements TorrentDownloaderCallBackInterface {
        public void TorrentDownloaderEvent(int state, TorrentDownloader inf) {
            if (state == TorrentDownloader.STATE_FINISHED) {
                try {
                    BTDownloadCreator creator = new BTDownloadCreator(inf.getFile());
                    if (!creator.isTorrentInGlobalManager()) {
                        _delegate = creator.createDownload();
                    } else {
                        _state = STATE_DUPLICATED;
                    }
                } catch (Exception e) {
                    _state = STATE_ERROR;
                    e.printStackTrace();
                }
            } else if (state == TorrentDownloader.STATE_ERROR) {
                _state = STATE_ERROR;
            }
        }
    }
}
