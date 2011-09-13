package com.frostwire.gui.player;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *  A wrapper for the source of an audio file that is currently playing
 */
public class AudioSource {

    /**
     * current audio source that is loaded in the music player
     */
    private final File file;

    private final URL url;

    public AudioSource(File file, AudioMetaData metaData) {
        if (file == null) {
            throw new NullPointerException("File cannot be null");
        }

        this.file = file;
        this.url = null;
    }
    
    public AudioSource(File file) {
        this(file, null);
    }

    public AudioSource(URL url) {
        if (url == null) {
            throw new NullPointerException("URL cannot be null");
        }

        this.file = null;
        this.url = url;
    }

    public File getFile() {
        return file;
    }

    public URL getURL() {
        if (url != null)
            return url;
        else if (file != null)
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
            }
        return null;
    }
}
