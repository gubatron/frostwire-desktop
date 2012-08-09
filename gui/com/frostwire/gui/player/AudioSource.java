package com.frostwire.gui.player;

import java.io.File;
import java.net.URL;

import com.frostwire.alexandria.PlaylistItem;

/**
 *  A wrapper for the source of an audio file that is currently playing
 */
public class AudioSource {

    /**
     * current audio source that is loaded in the music player
     */
    private final File file;

    private final String url;

    private final PlaylistItem playlistItem;

    public AudioSource(File file) {
        if (file == null) {
            throw new NullPointerException("File cannot be null");
        }

        this.file = file;
        this.url = null;
        this.playlistItem = null;
    }

    @Override
    public String toString() {
        String name = null;
        if (getFile() != null) {
            name = getFile().getName();
        } else {
            name = url;
        }
        return "[AudioSource@" + hashCode() + ": " + name + "]";
    }

    public AudioSource(String url) {
        if (url == null) {
            throw new NullPointerException("Url cannot be null");
        }

        this.file = null;
        this.url = url;
        this.playlistItem = null;
    }

    public AudioSource(PlaylistItem playlistItem) {
        if (playlistItem == null) {
            throw new NullPointerException("PlaylistItem cannot be null");
        }

        this.file = null;
        this.url = null;
        this.playlistItem = playlistItem;
    }

    public File getFile() {
        return file;
    }

    public String getURL() {
        return url;
    }

    public PlaylistItem getPlaylistItem() {
        return playlistItem;
    }

    @Override
    public boolean equals(Object obj) {
        AudioSource o = (AudioSource) obj;
        if (file != null && o.file != null) {
            return file.equals(o.file);
        }
        if (url != null && o.url != null) {
            return url.equals(o.url);
        }
        if (playlistItem != null && o.playlistItem != null) {
            return playlistItem.equals(o.playlistItem);
        }
        return false;
    }
}
