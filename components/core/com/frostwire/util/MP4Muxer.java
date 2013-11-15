package com.frostwire.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.PropertyBoxParserImpl;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import com.coremedia.iso.boxes.FileTypeBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.TrackBox;
import com.googlecode.mp4parser.AbstractBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Mp4TrackImpl;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;

public class MP4Muxer {

    public void mux(String video, String audio, String output) throws Exception {

        FileInputStream videoIn = new FileInputStream(video);
        FileChannel videoChannel = videoIn.getChannel();
        Movie videoMovie = buildMovie(videoChannel);

        FileInputStream audioIn = new FileInputStream(audio);
        FileChannel audioChannel = audioIn.getChannel();
        Movie audioMovie = buildMovie(audioChannel);

        Movie outMovie = new Movie();

        for (Track trk : videoMovie.getTracks()) {
            outMovie.addTrack(trk);
        }

        for (Track trk : audioMovie.getTracks()) {
            outMovie.addTrack(trk);
        }

        IsoFile out = new DefaultMp4Builder() {
            protected FileTypeBox createFileTypeBox(Movie movie) {
                List<String> minorBrands = new LinkedList<String>();
                minorBrands.add("iso6");
                minorBrands.add("avc1");
                minorBrands.add("mp41");
                minorBrands.add("\0\0\0\0");

                return new FileTypeBox("MP4 ", 0, minorBrands);
            };

            protected MovieBox createMovieBox(Movie movie, Map<Track, int[]> chunks) {
                MovieBox moov = super.createMovieBox(movie, chunks);
                moov.getMovieHeaderBox().setVersion(0);
                return moov;
            };

            protected TrackBox createTrackBox(Track track, Movie movie, Map<Track, int[]> chunks) {
                TrackBox trak = super.createTrackBox(track, movie, chunks);

                trak.getTrackHeaderBox().setVersion(0);
                trak.getTrackHeaderBox().setVolume(1.0f);

                return trak;
            };

            //            protected Box createUdta(Movie movie) {
            //                //String videoLink = (String) dl.getProperty("videolink", "YouTube.com");
            //                String vidLink = (youTubeVideoLink != null) ? youTubeVideoLink : "YouTube.com";
            //
            //                return addUserDataBox(FilenameUtils.getBaseName(filename), vidLink, jpgFilename);
            //            };
        }.build(outMovie);

        FileOutputStream fos = new FileOutputStream(output);
        out.getBox(fos.getChannel());

        IOUtils.closeQuietly(fos);

        IOUtils.closeQuietly(videoIn);
        IOUtils.closeQuietly(audioIn);
    }

    public void demuxAudio(String video, String output) throws Exception {

        FileInputStream videoIn = new FileInputStream(video);
        FileChannel videoChannel = videoIn.getChannel();
        Movie videoMovie = buildMovie(videoChannel);

        Track audioTrack = null;

        for (Track trk : videoMovie.getTracks()) {
            if (trk.getHandler().equals("soun")) {
                audioTrack = trk;
                break;
            }
        }

        if (audioTrack == null) {
            //TbCm.LOG.info("No Audio track in MP4 file!!! - " + filename);
            IOUtils.closeQuietly(videoIn);
            return;
        }

        Movie outMovie = new Movie();
        outMovie.addTrack(audioTrack);

        IsoFile out = new DefaultMp4Builder() {
            protected FileTypeBox createFileTypeBox(Movie movie) {
                List<String> minorBrands = new LinkedList<String>();
                minorBrands.add("iso6");
                minorBrands.add("avc1");
                minorBrands.add("mp41");
                minorBrands.add("\0\0\0\0");

                return new FileTypeBox("MP4 ", 0, minorBrands);
            };

            protected MovieBox createMovieBox(Movie movie, Map<Track, int[]> chunks) {
                MovieBox moov = super.createMovieBox(movie, chunks);
                moov.getMovieHeaderBox().setVersion(0);
                return moov;
            };

            protected TrackBox createTrackBox(Track track, Movie movie, Map<Track, int[]> chunks) {
                TrackBox trak = super.createTrackBox(track, movie, chunks);

                trak.getTrackHeaderBox().setVersion(0);
                trak.getTrackHeaderBox().setVolume(1.0f);

                return trak;
            };

            //            protected Box createUdta(Movie movie) {
            //                //String videoLink = (String) dl.getProperty("videolink", "YouTube.com");
            //                String vidLink = (youTubeVideoLink != null) ? youTubeVideoLink : "YouTube.com";
            //
            //                return addUserDataBox(FilenameUtils.getBaseName(filename), vidLink, jpgFilename);
            //            };
        }.build(outMovie);

        FileOutputStream fos = new FileOutputStream(output);
        out.getBox(fos.getChannel());

        IOUtils.closeQuietly(fos);

        IOUtils.closeQuietly(videoIn);
    }

    public static Movie buildMovie(ReadableByteChannel channel) throws IOException {
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
        IsoFile isoFile = new IsoFile(channel, parser);
        Movie m = new Movie();
        List<TrackBox> trackBoxes = isoFile.getMovieBox().getBoxes(TrackBox.class);
        for (TrackBox trackBox : trackBoxes) {
            m.addTrack(new Mp4TrackImpl(trackBox));
        }

        // do not close this isoFile at this time, ignore eclipse warning for now
        // NOT: IOUtils.closeQuietly(isoFile);

        return m;
    }

    public static void main(String[] args) throws Exception {
        String video = "/Users/aldenml/Downloads/testv.mp4";
        String audio = "/Users/aldenml/Downloads/testa.mp4";
        String output = "/Users/aldenml/Downloads/test.mp4";
        new MP4Muxer().mux(video, audio, output);
    }
}
