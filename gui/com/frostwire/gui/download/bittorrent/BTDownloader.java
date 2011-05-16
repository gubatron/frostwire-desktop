package com.frostwire.gui.download.bittorrent;

import java.io.File;

import org.gudy.azureus2.core3.download.DownloadManager;

public interface BTDownloader {

    public long getSize();

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

    public double getDownloadSpeed();

    public double getUploadSpeed();

    public long getETA();
    
    public DownloadManager getDownloadManager();
}
