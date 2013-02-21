package com.frostwire.gui.library.tags;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.PropertyBoxParserImpl;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.coremedia.iso.boxes.apple.AbstractAppleMetaDataBox;
import com.coremedia.iso.boxes.apple.AppleAlbumBox;
import com.coremedia.iso.boxes.apple.AppleArtistBox;
import com.coremedia.iso.boxes.apple.AppleCommentBox;
import com.coremedia.iso.boxes.apple.AppleCustomGenreBox;
import com.coremedia.iso.boxes.apple.AppleDataBox;
import com.coremedia.iso.boxes.apple.AppleItemListBox;
import com.coremedia.iso.boxes.apple.AppleStandardGenreBox;
import com.coremedia.iso.boxes.apple.AppleTrackNumberBox;
import com.coremedia.iso.boxes.apple.AppleTrackTitleBox;
import com.googlecode.mp4parser.AbstractBox;
import com.googlecode.mp4parser.util.Path;

class MP4Parser extends AbstractTagParser {

    private static final Log LOG = LogFactory.getLog(MP4Parser.class);

    public MP4Parser(File file) {
        super(file);
    }

    @Override
    public TagsData parse() {
        TagsData data = null;

        try {
            FileInputStream is = new FileInputStream(file);
            FileChannel ch = is.getChannel();
            BoxParser parser = new PropertyBoxParserImpl() {
                @Override
                public Box parseBox(ReadableByteChannel byteChannel, ContainerBox parent) throws IOException {
                    Box box = super.parseBox(byteChannel, parent);

                    if (box instanceof AbstractBox) {
                        ((AbstractBox) box).parseDetails();
                    }

                    return box;
                }
            };
            IsoFile iso = new IsoFile(ch, parser);

            try {

                int duration = getDuration(iso);
                String bitrate = getBitRate(iso);

                AppleItemListBox ilst = (AppleItemListBox) Path.getPath(iso.getMovieBox(), "/moov/udta/meta/ilst");
                ilst.parseDetails();

                String title = getBoxValue(ilst, AppleTrackTitleBox.class);
                String artist = getBoxValue(ilst, AppleArtistBox.class);
                String album = getBoxValue(ilst, AppleAlbumBox.class);
                String comment = getBoxValue(ilst, AppleCommentBox.class);
                String genre = getGenre(ilst);
                String track = getBoxValue(ilst, AppleTrackNumberBox.class);
                String year = ""; // need to research

                data = sanitize(duration, bitrate, title, artist, album, comment, genre, track, year);

            } finally {
                closeQuietly(iso);
                closeQuietly(ch);
                closeQuietly(is);
            }

        } catch (Exception e) {
            LOG.warn("Unable to parse file using mp4parser: " + file);
        }

        return data;
    }

    @Override
    public BufferedImage getArtwork() {
        return getArtworkFromMP4(file);
    }

    static BufferedImage getArtworkFromMP4(File file) {
        BufferedImage image = null;

        try {
            FileInputStream is = new FileInputStream(file);
            FileChannel ch = is.getChannel();
            BoxParser parser = new PropertyBoxParserImpl() {
                @Override
                public Box parseBox(ReadableByteChannel byteChannel, ContainerBox parent) throws IOException {
                    Box box = super.parseBox(byteChannel, parent);

                    if (box instanceof AbstractBox) {
                        ((AbstractBox) box).parseDetails();
                    }

                    return box;
                }
            };
            IsoFile iso = new IsoFile(ch, parser);

            try {

                AppleDataBox data = (AppleDataBox) Path.getPath(iso.getMovieBox(), "/moov/udta/meta/ilst/covr/data");
                data.parseDetails();
                if (data != null) {
                    byte[] imageData = data.getData();
                    if ((data.getFlags() & 0xd) == 0xd) { // jpg
                        image = imageFromData(imageData);
                    } else if ((data.getFlags() & 0xe) == 0xe) { // png
                        try {
                            image = ImageIO.read(new ByteArrayInputStream(imageData, 0, imageData.length));
                        } catch (IIOException e) {
                            LOG.warn("Unable to decode png image from data tag");
                        }
                    }
                }
            } finally {
                closeQuietly(iso);
                closeQuietly(ch);
                closeQuietly(is);
            }
        } catch (Throwable e) {
            //LOG.error("Unable to read cover art from mp4 file: " + file);
        }

        return image;
    }

    private int getDuration(IsoFile iso) {
        MovieHeaderBox mvhd = iso.getMovieBox().getMovieHeaderBox();
        return (int) (mvhd.getDuration() / mvhd.getTimescale());
    }

    private String getBitRate(IsoFile iso) {
        return ""; // deep research of atoms per codec
    }

    private <T extends AbstractAppleMetaDataBox> String getBoxValue(AppleItemListBox ilst, Class<T> clazz) {
        String value = "";
        List<T> boxes = ilst.getBoxes(clazz);
        if (boxes != null && !boxes.isEmpty()) {
            value = boxes.get(0).getValue();
        }
        return value;
    }

    private String getGenre(AppleItemListBox ilst) {
        String value = getBoxValue(ilst, AppleStandardGenreBox.class);
        if (value == null || value.equals("")) {
            value = getBoxValue(ilst, AppleCustomGenreBox.class);
        }
        return value;
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Throwable e) {
            // ignore
        }
    }
}
