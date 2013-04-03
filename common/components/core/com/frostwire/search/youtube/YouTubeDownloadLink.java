package com.frostwire.search.youtube;

public class YouTubeDownloadLink {

    private final String filename;
    private final long size;
    private final String downloadUrl;
    private final int iTag;
    /** http://en.wikipedia.org/wiki/YouTube */
    private final boolean audio;

    public YouTubeDownloadLink(String filename, long size, String downloadUrl, int iTag) {
        this(filename, size, downloadUrl, iTag, false);
    }

    public YouTubeDownloadLink(String filename, long size, String downloadUrl, int iTag, boolean audio) {
        this.filename = filename;
        this.size = size;
        this.downloadUrl = downloadUrl;
        this.iTag = iTag;
        this.audio = audio;
    }

    public String getFilename() {
        return filename;
    }

    public long getSize() {
        return size;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public int getITag() {
        return iTag;
    }

    public boolean isAudio() {
        return audio;
    }
}