package com.frostwire.gui.bittorrent;

import java.io.File;
import java.util.Date;

import jd.controlling.IOEQ;
import jd.controlling.downloadcontroller.DownloadController;
import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.LinkStatus;

import org.gudy.azureus2.core3.download.DownloadManager;
import org.limewire.util.FilenameUtils;

import com.limegroup.gnutella.gui.I18n;

public class YouTubeItemDownload implements BTDownload {
    
    private static final String STATE_DOWNLOADING = I18n.tr("Downloading");
    private static final String STATE_ERROR = I18n.tr("Error");
    private static final String STATE_STOPPED = I18n.tr("Stopped");
    private static final String STATE_WAITING = I18n.tr("Waiting");
    private static final String STATE_FINISHED = I18n.tr("Finished");

    private final FilePackage filePackage;
    private final DownloadLink link;
    private final Date dateCreated;
    private final String saveLocation;
    
    private boolean deleteDataWhenRemove;
    
    private boolean started;
    
    private boolean finished;

    public YouTubeItemDownload(FilePackage filePackage) {
        this.filePackage = filePackage;
        this.link = filePackage.getChildren().get(0);
        this.dateCreated = new Date();
        this.saveLocation = readSaveLocation(filePackage);

        this.started = false;
        start();
    }

    @Override
    public long getSize() {
        return link.getDownloadSize();
    }

    @Override
    public long getSize(boolean update) {
        return link.getDownloadSize();
    }

    @Override
    public String getDisplayName() {
        return FilenameUtils.getName(saveLocation);
    }

    @Override
    public boolean isResumable() {
        return started && !isPausable() && !finished; 
    }

    @Override
    public boolean isPausable() {
        return link.getLinkStatus().hasStatus(LinkStatus.DOWNLOADINTERFACE_IN_PROGRESS);
    }

    @Override
    public boolean isCompleted() {
        return link.getLinkStatus().hasStatus(LinkStatus.FINISHED);
    }

    @Override
    public int getState() {
        if (link.getLinkStatus().hasStatus(LinkStatus.DOWNLOADINTERFACE_IN_PROGRESS)) {
            return DownloadManager.STATE_DOWNLOADING;
        }
        
        return DownloadManager.STATE_STOPPED;
    }

    @Override
    public void remove() {
        pause();
        if (deleteDataWhenRemove) {
            link.deleteFile(true, true);
        }
    }

    @Override
    public void pause() {
        DownloadController.getInstance().removePackage(filePackage);
        link.abort();
    }

    @Override
    public File getSaveLocation() {
        return new File(saveLocation);
    }

    @Override
    public void resume() {
        start();
    }

    @Override
    public int getProgress() {
        return (int) ((link.getDownloadCurrent() * 100) / link.getDownloadSize());
    }

    @Override
    public String getStateString() {
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
    }

    @Override
    public long getBytesReceived() {
        return link.getDownloadCurrent();
    }

    @Override
    public long getBytesSent() {
        return 0;
    }

    @Override
    public double getDownloadSpeed() {
        return link.getDownloadSpeed() / 1024;
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
