package com.frostwire.gui.library.tags;

import java.awt.image.BufferedImage;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TagsReader {

    private static final Log LOG = LogFactory.getLog(TagsReader.class);

    private final File file;

    public TagsReader(File file) {
        this.file = file;
    }

    public TagsData parse() {
        TagsData data = null;

        TagsParser parser = new TagsParserFactory().getInstance(file);

        if (parser != null) {
            data = parser.parse();

            // aldenml: fallback to mplayer parsing, refactor this logic (remove it)
            if (data == null || isEmpty(data)) {
                data = new MPlayerParser(file).parse();
            }
        } else {
            LOG.warn("Unable to create tags parser for file: " + file);
        }

        return data;
    }

    public BufferedImage getArtwork() {
        BufferedImage image = null;

        TagsParser parser = new TagsParserFactory().getInstance(file);
        if (parser != null) {
            image = parser.getArtwork();
        } else {
            LOG.warn("Unable to create tags parser for file: " + file);
        }

        return image;
    }

    private boolean isEmpty(TagsData data) {
        return false; // default behavior for now
    }
}
