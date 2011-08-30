package com.frostwire.gui.library;

class PlaylistItemProperty {

    private final String _value;
    private final boolean _playing;

    public PlaylistItemProperty(String value, boolean playing) {
        _value = value;
        _playing = playing;
    }

    public String getValue() {
        return _value;
    }

    public boolean isPlaying() {
        return _playing;
    }
}
