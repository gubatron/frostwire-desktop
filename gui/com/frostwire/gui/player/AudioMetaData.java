package com.frostwire.gui.player;

import java.io.File;
import java.util.Map;

import com.frostwire.mplayer.MPlayer;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

public class AudioMetaData {

    private Map<String, String> _properties;

    public AudioMetaData(Map<String, String> props) {
        _properties = props;
    }

    public AudioMetaData(File file) {
        MPlayer mplayer = new MPlayer();
        _properties = mplayer.getProperties(file.getAbsolutePath());

        if (file.getName().endsWith("mp3") && getTitle() == null && getArtist() == null && getAlbum() == null) {
            // fall back to new mp3 library
            readMoreMP3Tags(file);
        }
    }

    public String getArtist() {
        return _properties.get("Artist");
    }

    public String getAlbum() {
        return _properties.get("Album");
    }

    public String getBitrate() {
        if (!_properties.containsKey("ID_AUDIO_BITRATE")) {
            return "";
        }
        try {
            return (Integer.parseInt(_properties.get("ID_AUDIO_BITRATE")) / 1000) + " kbps";
        } catch (Exception e) {
            return _properties.get("ID_AUDIO_BITRATE");
        }
    }

    public String getComment() {
        return _properties.get("Comment");
    }

    public String getGenre() {
        return _properties.get("Genre");
    }

    public float getLength() {
        try {
            return Float.parseFloat(_properties.get("ID_LENGTH"));
        } catch (Exception e) {
            return -1;
        }
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

    private void readMoreMP3Tags(File file) {
        try {
            Mp3File mp3 = new Mp3File(file.getAbsolutePath());
            if (mp3.hasId3v2Tag()) {
                ID3v2 tag = mp3.getId3v2Tag();
                _properties.put("Artist", tag.getArtist());
                _properties.put("Album", tag.getAlbum());
                _properties.put("Comment", tag.getComment());
                _properties.put("Genre", tag.getGenreDescription());
                _properties.put("Title", tag.getTitle());
                _properties.put("Track", tag.getTrack());
                _properties.put("Year", tag.getYear());
            }
        } catch (Exception e) {
            // ignore
        }
    }
}
