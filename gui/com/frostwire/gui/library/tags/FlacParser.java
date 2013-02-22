package com.frostwire.gui.library.tags;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.flac.FlacFileReader;
import org.jaudiotagger.audio.flac.metadatablock.MetadataBlockDataPicture;
import org.jaudiotagger.tag.flac.FlacTag;

class FlacParser extends JaudiotaggerParser {

    private static final Log LOG = LogFactory.getLog(FlacParser.class);

    public FlacParser(File file) {
        super(file, new FlacFileReader());
    }

    @Override
    public BufferedImage getArtwork() {
        BufferedImage image = super.getArtwork();

        if (image == null) {
            try {
                AudioFile audioFile = AudioFileIO.read(file);
                FlacTag tag = (FlacTag) audioFile.getTag();
                if (tag != null) {
                    List<MetadataBlockDataPicture> images = tag.getImages();
                    if (images != null && !images.isEmpty()) {
                        MetadataBlockDataPicture picture = images.get(0);
                        byte[] data = picture.getImageData();
                        image = imageFromData(data);
                    }
                }
            } catch (Throwable e) {
                LOG.error("Unable to read cover art from flac");
            }
        }

        return image;
    }
}
