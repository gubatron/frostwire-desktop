package com.limegroup.gnutella.gui.tables;

public abstract class AbstractActionsHolder {
    private boolean playing;
    private final DataLine dataLine;

    public AbstractActionsHolder(DataLine dataLine, boolean playing) {
        this.playing = playing;
        this.dataLine = dataLine;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }
    
    public DataLine getDataLine() {
        return dataLine;
    }
    
    public abstract boolean isPlayable();
    public abstract boolean isDownloadable();
}