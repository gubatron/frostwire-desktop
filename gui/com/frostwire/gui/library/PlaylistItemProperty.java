package com.frostwire.gui.library;

class PlaylistItemProperty {

    private final String _value;
    private final boolean _playing;
    private final boolean exists;

    public PlaylistItemProperty(String value, boolean playing, boolean exists) {
        _value = value;
        _playing = playing;
        this.exists = exists;
    }

    public String getValue() {
        return _value;
    }

    public boolean isPlaying() {
        return _playing;
    }
    
    public boolean exists() {
        return exists;
    }
}
