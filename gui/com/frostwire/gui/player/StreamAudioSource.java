package com.frostwire.gui.player;

public class StreamAudioSource extends AudioSource {

    private final String title;

    public StreamAudioSource(String url, String title) {
        super(url);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
