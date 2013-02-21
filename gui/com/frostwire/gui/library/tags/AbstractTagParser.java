package com.frostwire.gui.library.tags;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.util.FilenameUtils;

import com.frostwire.jpeg.JPEGImageIO;

abstract class AbstractTagParser implements TagsParser {

    private static final Log LOG = LogFactory.getLog(AbstractTagParser.class);

    protected final File file;

    public AbstractTagParser(File file) {
        this.file = file;
    }

    protected TagsData sanitize(int duration, String bitrate, String title, String artist, String album, String comment, String genre, String track, String year) {
        if (title == null || title.length() == 0) {
            title = FilenameUtils.getBaseName(file.getAbsolutePath());
        }

        if (duration < 0) {
            duration = 0;
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
        } else {
            genre = genre.replaceFirst("\\(.*\\)", "");
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

        return new TagsData(duration, bitrate, title, artist, album, comment, genre, track, year);
    }

    protected static BufferedImage imageFromData(byte[] data) {
        BufferedImage image = null;
        try {
            try {
                image = ImageIO.read(new ByteArrayInputStream(data, 0, data.length));
            } catch (IIOException e) {
                image = JPEGImageIO.read(new ByteArrayInputStream(data, 0, data.length));
            }
        } catch (Throwable e) {
            LOG.error("Unable to create artwork image from bytes");
        }

        return image;
    }
}
