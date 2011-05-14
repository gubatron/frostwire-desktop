package com.frostwire.gui.download.bittorrent;

import java.io.File;

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
}
