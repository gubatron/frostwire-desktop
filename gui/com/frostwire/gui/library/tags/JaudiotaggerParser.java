package com.frostwire.gui.library.tags;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

class JaudiotaggerParser extends AbstractTagParser {

    private static final Log LOG = LogFactory.getLog(JaudiotaggerParser.class);

    public JaudiotaggerParser(File file) {
        super(file);
    }

    @Override
    public TagsData parse() {
        TagsData data = null;

        try {
            AudioFile audioFile = AudioFileIO.read(file);

            AudioHeader header = audioFile.getAudioHeader();

            int duration = header.getTrackLength();
            String bitrate = header.getBitRate();

            String title = getTitle(audioFile);
            String artist = getArtist(audioFile);
            String album = getAlbum(audioFile);
            String comment = getComment(audioFile);
            String genre = getGenre(audioFile);
            String track = getTrack(audioFile);
            String year = getYear(audioFile);

            data = new TagsData(duration, bitrate, title, artist, album, comment, genre, track, year);

        } catch (Exception e) {
            LOG.warn("Unable to parse file with Jaudiotagger: " + file, e);
        }

        return data;
    }

    protected String getTitle(AudioFile audioFile) {
        return getValueSafe(audioFile.getTag(), FieldKey.TITLE);
    }

    protected String getArtist(AudioFile audioFile) {
        return getValueSafe(audioFile.getTag(), FieldKey.ARTIST);
    }

    protected String getAlbum(AudioFile audioFile) {
        return getValueSafe(audioFile.getTag(), FieldKey.ALBUM);
    }

    protected String getComment(AudioFile audioFile) {
        return getValueSafe(audioFile.getTag(), FieldKey.COMMENT);
    }

    protected String getGenre(AudioFile audioFile) {
        return getValueSafe(audioFile.getTag(), FieldKey.GENRE);
    }

    protected String getTrack(AudioFile audioFile) {
        return getValueSafe(audioFile.getTag(), FieldKey.TRACK);
    }

    protected String getYear(AudioFile audioFile) {
        return getValueSafe(audioFile.getTag(), FieldKey.YEAR);
    }

    private String getValueSafe(Tag tag, FieldKey id) {
        String value = null;

        try {
            value = tag.getFirst(id);
        } catch (Exception e) {
            LOG.warn("Unable to get value for key: " + id, e);
        }

        return value;
    }
}
