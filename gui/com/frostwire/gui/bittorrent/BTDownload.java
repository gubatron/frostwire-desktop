package com.frostwire.gui.bittorrent;

import java.io.File;
import java.util.Date;

import org.gudy.azureus2.core3.download.DownloadManager;

public interface BTDownload {

    public long getSize();
    
    public long getSize(boolean update);

    public String getDisplayName();

    public boolean isResumable();

    public boolean isPausable();

    public boolean isCompleted();

    public int getState();

    public void remove();

    public void pause();

    public File getSaveLocation();

    public void resume();

    public int getProgress();

    public String getStateString();

    public long getBytesReceived();
    
    public long getBytesSent();

    public double getDownloadSpeed();

    public double getUploadSpeed();

    public long getETA();
    
    public DownloadManager getDownloadManager();

    public String getPeersString();

    public String getSeedsString();
    
    public boolean isDeleteTorrentWhenRemove();
    
    public void setDeleteTorrentWhenRemove(boolean deleteTorrentWhenRemove);
    
    public boolean isDeleteDataWhenRemove();
    
    public void setDeleteDataWhenRemove(boolean deleteDataWhenRemove);
    
    public String getHash();

    public String getSeedToPeerRatio();

    public String getShareRatio();

    public boolean isPartialDownload();

	public void updateDownloadManager(DownloadManager downloadManager);

    public Date getDateCreated();
}
