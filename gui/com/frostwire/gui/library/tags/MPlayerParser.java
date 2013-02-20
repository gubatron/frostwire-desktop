package com.frostwire.gui.library.tags;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.frostwire.gui.mplayer.MPlayer;

class MPlayerParser extends AbstractTagParser {

    private static final Log LOG = LogFactory.getLog(MPlayerParser.class);

    public MPlayerParser(File file) {
        super(file);
    }

    @Override
    public TagsData parse() {
        TagsData data = null;

        try {
            MPlayer mplayer = new MPlayer();

            try {
                Map<String, String> properties = mplayer.getProperties(file.getAbsolutePath());

                int duration = parseDuration(properties.get("ID_LENGTH"));
                String bitrate = parseBitrate(properties.get("ID_AUDIO_BITRATE"));

                String title = properties.get("Title");
                String artist = properties.get("Artist");
                String album = properties.get("Album");
                String comment = properties.get("Comment");
                String genre = properties.get("Genre");
                String track = properties.get("Track");
                String year = properties.get("Year");

                data = sanitize(duration, bitrate, title, artist, album, comment, genre, track, year);

            } finally {
                mplayer.dispose();
            }
        } catch (Exception e) {
            LOG.warn("Unable to parse file with mplayer: " + file, e);
        }

        return data;
    }

    @Override
    public BufferedImage getArtwork() {
        return null;
    }

    private int parseDuration(String durationInSecs) {
        try {
            return (int) Float.parseFloat(durationInSecs);
        } catch (Exception e) {
            return 0;
        }
    }

    private String parseBitrate(String bitrate) {
        if (bitrate == null) {
            return "";
        }
        try {
            return String.valueOf(Integer.parseInt(bitrate) / 1000);
        } catch (Exception e) {
            return bitrate;
        }
    }
}
