package com.frostwire.gui.library.tags;

import java.awt.image.BufferedImage;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp4.Mp4FileReader;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.mp4.Mp4TagField;

class M4AParser extends JaudiotaggerParser {

    private static final Log LOG = LogFactory.getLog(MP3Parser.class);

    public M4AParser(File file) {
        super(file, new Mp4FileReader());
    }

    @Override
    public BufferedImage getArtwork() {
        BufferedImage image = super.getArtwork();

        if (image == null) {
            try {
                AudioFile audioFile = AudioFileIO.read(file);
                Mp4Tag mp4tag = (Mp4Tag) audioFile.getTag();
                if (mp4tag != null) {
                    Mp4TagField artField = mp4tag.getFirstField(Mp4FieldKey.ARTWORK);
                    if (artField != null) {
                        byte[] data = artField.getRawContentDataOnly();
                        image = imageFromData(data);
                    }
                }
                if (image == null) { // one more try
                    image = MP4Parser.getArtworkFromMP4(file);
                }
            } catch (Throwable e) {
                LOG.error("Unable to read cover art from m4a");
            }
        }

        return image;
    }
}
