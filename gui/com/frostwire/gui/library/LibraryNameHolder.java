package com.frostwire.gui.library;

public class LibraryNameHolder extends PlayableCell {

    private boolean exists;

    public LibraryNameHolder(Object dataLine, Object wrapMe, boolean isPlaying, boolean exists, int columnIndex) {
        super(dataLine, wrapMe, isPlaying, columnIndex);
        this.exists = exists;
    }

    public boolean isExists() {
        return exists;
    }
}
