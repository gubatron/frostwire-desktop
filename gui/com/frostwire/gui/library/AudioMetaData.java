/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.gui.library;

import java.io.File;
import java.util.Map;

import org.limewire.util.FilenameUtils;
import org.limewire.util.StringUtils;

import com.frostwire.gui.mplayer.MPlayer;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class AudioMetaData {

    private String title;
    private float durationInSecs;
    private String artist;
    private String album;
    private String bitrate;
    private String comment;
    private String genre;
    private String track;
    private String year;

    public AudioMetaData(File file) {
        readUsingMPlayer(file);
        if (file.getName().endsWith("mp3")) {
            readUsingMP3Tags(file);
        }

        sanitizeData(file);
    }

    public String getTitle() {
        return title;
    }

    public float getDurationInSecs() {
        return durationInSecs;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getBitrate() {
        return bitrate;
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

    private void readUsingMPlayer(File file) {
        MPlayer mplayer = new MPlayer();

        try {
            Map<String, String> properties = mplayer.getProperties(file.getAbsolutePath());

            title = properties.get("Title");
            durationInSecs = parseDurationInSecs(properties.get("ID_LENGTH"));
            artist = properties.get("Artist");
            album = properties.get("Album");
            bitrate = parseBitrate(properties.get("ID_AUDIO_BITRATE"));
            comment = properties.get("Comment");
            genre = properties.get("Genre");
            track = properties.get("Track");
            year = properties.get("Year");
        } finally {
            mplayer.dispose();
        }
    }

    private void readUsingMP3Tags(File file) {
        try {
            Mp3File mp3 = new Mp3File(file.getAbsolutePath());
            if (mp3.hasId3v2Tag()) {
                ID3v2 tag = mp3.getId3v2Tag();
                if (!StringUtils.isNullOrEmpty(tag.getTitle(), true)) {
                    title = tag.getTitle();
                }
                if (!StringUtils.isNullOrEmpty(tag.getArtist(), true)) {
                    artist = tag.getArtist();
                }
                if (!StringUtils.isNullOrEmpty(tag.getAlbum(), true)) {
                    album = tag.getAlbum();
                }
                if (!StringUtils.isNullOrEmpty(tag.getComment(), true) || comment.startsWith("0")) {
                    comment = tag.getComment();
                }
                if (!StringUtils.isNullOrEmpty(tag.getGenreDescription(), true) || genre.trim().equals("Unknown")) {
                    genre = tag.getGenreDescription();
                }
                if (!StringUtils.isNullOrEmpty(tag.getTrack(), true)) {
                    track = tag.getTrack();
                }
                if (!StringUtils.isNullOrEmpty(tag.getYear(), true)) {
                    year = tag.getYear();
                }

                durationInSecs = mp3.getLengthInSeconds();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void sanitizeData(File file) {
        if (StringUtils.isNullOrEmpty(title, true)) {
            title = FilenameUtils.getBaseName(file.getAbsolutePath());
        }
        if (durationInSecs < 0) {
            durationInSecs = 0;
        }
        if (artist == null) {
            artist = "";
        }
        if (album == null) {
            album = "";
        }
        if (bitrate == null) {
            bitrate = "";
        }
        if (comment == null) {
            comment = "";
        }

        if (genre == null) {
            genre = "";
        }

        if (track == null) {
            track = "";
        } else {
            int index = -1;
            index = track.indexOf('/');
            if (index != -1) {
                track = track.substring(0, index);
            }
        }

        if (year == null) {
            year = "";
        }
    }

    private String parseBitrate(String bitrate) {
        if (bitrate == null) {
            return "";
        }
        try {
            return (Integer.parseInt(bitrate) / 1000) + " kbps";
        } catch (Exception e) {
            return bitrate;
        }
    }

    public float parseDurationInSecs(String durationInSecs) {
        try {
            return Float.parseFloat(durationInSecs);
        } catch (Exception e) {
            return 0;
        }
    }
}
