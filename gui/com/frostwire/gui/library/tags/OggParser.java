package com.frostwire.gui.library.tags;

import java.awt.image.BufferedImage;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.ogg.OggFileReader;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;

class OggParser extends JaudiotaggerParser {

    private static final Log LOG = LogFactory.getLog(OggParser.class);

    public OggParser(File file) {
        super(file, new OggFileReader());
    }

    @Override
    public BufferedImage getArtwork() {
        BufferedImage image = super.getArtwork();

        if (image == null) {
            try {
                AudioFile audioFile = AudioFileIO.read(file);
                VorbisCommentTag tag = (VorbisCommentTag) audioFile.getTag();
                byte[] data = tag.getArtworkBinaryData();
                image = imageFromData(data);
            } catch (Throwable e) {
                LOG.error("Unable to read cover art from flac", e);
            }
        }

        return image;
    }
}
