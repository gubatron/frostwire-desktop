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
    
    /** Create an HttpDownload out of a promotional Slide */
    public HttpDownload(Slide slide) {
        this(slide.url, slide.title, slide.size);
    }
    
    public HttpDownload(String theURL, String theTitle, long fileSize) {
        url = theURL;
        title = theTitle;
        size = fileSize;
        
        started = false;
        start();
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getSize(boolean update) {
<<<<<<< HEAD
        return size
=======
        return size;
>>>>>>> 5.5.3-pokki
    }

    @Override
    public String getDisplayName() {
<<<<<<< HEAD
        return (StringUtils.isNullOrEmpty(title, true) ? FilenameUtils.getName(saveLocation) : title) + ".mp3";
=======
        return title;
>>>>>>> 5.5.3-pokki
    }

    @Override
    public boolean isResumable() {
        return started && !isPausable() && !finished;
    }

    @Override
    public boolean isPausable() {
<<<<<<< HEAD
        return link.getLinkStatus().hasStatus(LinkStatus.DOWNLOADINTERFACE_IN_PROGRESS);
=======
        return false;
>>>>>>> 5.5.3-pokki
    }

    @Override
    public boolean isCompleted() {
<<<<<<< HEAD
        return link.getLinkStatus().hasStatus(LinkStatus.FINISHED);
=======
        //TODO
        return false;
>>>>>>> 5.5.3-pokki
    }

    @Override
    public int getState() {
<<<<<<< HEAD
        if (link.getLinkStatus().hasStatus(LinkStatus.DOWNLOADINTERFACE_IN_PROGRESS)) {
            return DownloadManager.STATE_DOWNLOADING;
        }

        return DownloadManager.STATE_STOPPED;
=======
        //TODO
        //if (link.getLinkStatus().hasStatus(LinkStatus.DOWNLOADINTERFACE_IN_PROGRESS)) {
            //return DownloadManager.STATE_DOWNLOADING;
        //}

        //return DownloadManager.STATE_STOPPED;
        return 0;
>>>>>>> 5.5.3-pokki
    }

    @Override
    public void remove() {
<<<<<<< HEAD
        pause();
        if (deleteDataWhenRemove) {
            link.deleteFile(true, true);
=======
        //TODO
        pause();
        if (deleteDataWhenRemove) {
            //link.deleteFile(true, true);
>>>>>>> 5.5.3-pokki
        }
    }

    @Override
    public void pause() {
<<<<<<< HEAD
        DownloadController.getInstance().removePackage(filePackage);
        link.abort();
=======
        
>>>>>>> 5.5.3-pokki
    }

    @Override
    public File getSaveLocation() {
<<<<<<< HEAD
        DownloadLink dl = filePackage.getChildren().get(0);
        return new File(dl.getFileOutput());
=======
        //TODO
        return null;
>>>>>>> 5.5.3-pokki
    }

    @Override
    public void resume() {
        start();
    }

    @Override
    public int getProgress() {
<<<<<<< HEAD
        if (link.getDownloadSize() == 0) {
            return 0;
        }
        return (int) ((link.getDownloadCurrent() * 100) / link.getDownloadSize());
=======
        //TODO
        //if (link.getDownloadSize() == 0) {
        //    return 0;
        // }
        //return (int) ((link.getDownloadCurrent() * 100) / link.getDownloadSize());
        return 0;
>>>>>>> 5.5.3-pokki
    }

    @Override
    public String getStateString() {
<<<<<<< HEAD
=======
        //TODO
        return null;
        /**
>>>>>>> 5.5.3-pokki
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
<<<<<<< HEAD
=======
        */
>>>>>>> 5.5.3-pokki
    }

    @Override
    public long getBytesReceived() {
<<<<<<< HEAD
        return link.getDownloadCurrent();
=======
        //TODO
        //return link.getDownloadCurrent();
        return 0;
>>>>>>> 5.5.3-pokki
    }

    @Override
    public long getBytesSent() {
        return 0;
    }

    @Override
    public double getDownloadSpeed() {
<<<<<<< HEAD
        return link.getDownloadSpeed() / 1024;
=======
        //TODO
        return 0;
        //return link.getDownloadSpeed() / 1024;
>>>>>>> 5.5.3-pokki
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
<<<<<<< HEAD
        return dateCreated;
    }

    private void start() {
        link.reset();
        IOEQ.add(new Runnable() {

            public void run() {
                DownloadController.getInstance().addPackage(filePackage);
                IOEQ.add(new Runnable() {
                    public void run() {
                        DownloadWatchDog.getInstance().startDownloads();
                    }

                }, true);
            }

        }, true);
    }

    private String readSaveLocation(FilePackage filePackage) {
        DownloadLink dl = filePackage.getChildren().get(0);
        if (dl.getStringProperty("convertto", "").equals("AUDIOMP3")) {
            return FilenameUtils.getFullPath(dl.getFileOutput()) + File.separator + FilenameUtils.getBaseName(dl.getName()) + ".mp3";
        }

        return dl.getFileOutput();
    }
}
=======
        //TODO
        return null;
        //return dateCreated;
    }

    private void start() {
        //TODO
        //Download code here
    }
}
>>>>>>> 5.5.3-pokki
