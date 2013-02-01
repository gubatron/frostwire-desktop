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

import java.io.File;
import java.util.Date;

import org.gudy.azureus2.core3.download.DownloadManager;

import com.frostwire.gui.components.Slide;
import com.frostwire.util.HttpClient;
import com.frostwire.util.HttpClientFactory;
import com.frostwire.util.HttpClientType;
import com.limegroup.gnutella.gui.I18n;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class HttpDownload implements BTDownload {

    private static final String STATE_DOWNLOADING = I18n.tr("Downloading");
    private static final String STATE_ERROR = I18n.tr("Error");
    private static final String STATE_STOPPED = I18n.tr("Stopped");
    private static final String STATE_WAITING = I18n.tr("Waiting");
    private static final String STATE_FINISHED = I18n.tr("Finished");


    private boolean deleteDataWhenRemove;

    private boolean started;

    private boolean finished;

    private final String url;
    private final String title;
    private final long size;
    private final String md5; //optional
    
    /** Create an HttpDownload out of a promotional Slide */
    public HttpDownload(Slide slide) {
        this(slide.url, slide.title, slide.size, slide.md5);
    }
    
    public HttpDownload(String theURL, String theTitle, long fileSize, String md5hash) {
        url = theURL;
        title = theTitle;
        size = fileSize;
        md5 = md5hash;
        started = false;
        start();
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getSize(boolean update) {
        return size;
    }

    @Override
    public String getDisplayName() {
        return title;
    }

    @Override
    public boolean isResumable() {
        //TODO: This is up to the http client to tell
        //might be based on knowing what the expected file size is or not.
        //this also depends on the state of the transfer (finished, cancelled)
        return true;
    }

    @Override
    public boolean isPausable() {
        //TODO
        return true;
    }

    @Override
    public boolean isCompleted() {
        //TODO
        return false;
    }

    @Override
    public int getState() {
        //TODO
        //if (link.getLinkStatus().hasStatus(LinkStatus.DOWNLOADINTERFACE_IN_PROGRESS)) {
            //return DownloadManager.STATE_DOWNLOADING;
        //}

        //return DownloadManager.STATE_STOPPED;
        return 0;
    }

    @Override
    public void remove() {
        //TODO
        pause();
        if (deleteDataWhenRemove) {
            //link.deleteFile(true, true);
        }
    }

    @Override
    public void pause() {
        //TODO tells http client to stop, don't remove.
    }

    @Override
    public File getSaveLocation() {
        //TODO
        return null;
    }

    @Override
    public void resume() {
        start();
    }

    @Override
    public int getProgress() {
        //TODO
        //if (link.getDownloadSize() == 0) {
        //    return 0;
        // }
        //return (int) ((link.getDownloadCurrent() * 100) / link.getDownloadSize());
        return 0;
    }

    @Override
    public String getStateString() {
        //TODO
        return null;
        /**
        if (link.getLinkStatus().hasStatus(LinkStatus.DOWNLOADINTERFACE_IN_PROGRESS)) {
            started = true;
            return STATE_DOWNLOADING;
        } else if (link.getLinkStatus().isFailed()) {
            return STATE_ERROR;
        } else if (link.getLinkStatus().hasStatus(LinkStatus.FINISHED)) {
            finished = true;
            return STATE_FINISHED;
        }

        if (!started) {
            return STATE_WAITING;
        }
        return STATE_STOPPED;
        */
    }

    @Override
    public long getBytesReceived() {
        //TODO, should be returned by the http client, don't search for this on the file system.
        //return link.getDownloadCurrent();
        return 0;
    }

    @Override
    public long getBytesSent() {
        return 0;
    }

    @Override
    public double getDownloadSpeed() {
        //TODO
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
        return "";
    }

    @Override
    public String getSeedsString() {
        return "";
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
        return deleteDataWhenRemove;
    }

    @Override
    public void setDeleteDataWhenRemove(boolean deleteDataWhenRemove) {
        this.deleteDataWhenRemove = deleteDataWhenRemove;
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
    public void updateDownloadManager(DownloadManager downloadManager) {

    }

    @Override
    public Date getDateCreated() {
        //TODO
        return null;
    }

    private void start() {
        HttpClient httpClient = HttpClientFactory.newInstance(HttpClientType.PureJava);
        //start the download
        //
    }
}