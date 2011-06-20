package com.limegroup.gnutella.gui.player;

import java.util.Map;

public class AudioMetaData {

    private Map<String, String> _properties;

    public AudioMetaData(Map<String, String> props) {
        _properties = props;
    }

    public String getArtist() {
        return _properties.get("Artist");
    }

    public String getAlbum() {
        return _properties.get("Album");
    }

    public String getBitrate() {
        return _properties.get("ID_AUDIO_BITRATE");
    }

    public String getComment() {
        return _properties.get("Comment");
    }

    public String getGenre() {
        return _properties.get("Genre");
    }

    public long getLength() {
        return (long) Double.parseDouble(_properties.get("ID_LENGTH"));
    }

    public String getTitle() {
        return _properties.get("Title");
    }

    public String getTrack() {
        return _properties.get("Track");
    }

    public String getYear() {
        return _properties.get("Year");
    }

    public boolean isSeekable() {
        return _properties.containsKey("ID_SEEKABLE") && (Integer.parseInt(_properties.get("ID_SEEKABLE")) != 0);
    }
}
