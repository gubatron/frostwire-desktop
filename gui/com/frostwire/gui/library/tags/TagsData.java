package com.frostwire.gui.library.tags;

public class TagsData {

    private final int duration;
    private final String bitrate;
    private final String title;
    private final String artist;
    private final String album;
    private final String comment;
    private final String genre;
    private final String track;
    private final String year;

    public TagsData(int duration, String bitrate, String title, String artist, String album, String comment, String genre, String track, String year) {
        this.duration = duration;
        this.bitrate = bitrate;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.comment = comment;
        this.genre = genre;
        this.track = track;
        this.year = year;
    }

    public int getDuration() {
        return duration;
    }

    public String getBitrate() {
        return bitrate;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getComment() {
        return comment;
    }

    public String getGenre() {
        return genre;
    }

    public String getTrack() {
        return track;
    }

    public String getYear() {
        return year;
    }
}
