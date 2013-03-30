/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
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

package com.frostwire.gui;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.limewire.util.FilenameUtils;
import org.limewire.util.StringUtils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Descriptor;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.frostwire.content.ContentValues;
import com.frostwire.core.Constants;
import com.frostwire.core.providers.ShareFilesDB;
import com.frostwire.core.providers.ShareFilesDB.Columns;
import com.frostwire.gui.library.tags.TagsData;
import com.frostwire.gui.library.tags.TagsReader;
import com.frostwire.util.MimeDetector;
import com.limegroup.gnutella.MediaType;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class UniversalScanner {

    private static final Logger LOG = Logger.getLogger(UniversalScanner.class.getName());

    public UniversalScanner() {
    }

    public void scan(String filePath) {
        try {
            MediaType mt = MediaType.getMediaTypeForExtension(FilenameUtils.getExtension(filePath));

            if (mt == null) {
                scanDocument(filePath, true);
            } else if (mt.equals(MediaType.getAudioMediaType())) {
                scanAudio(filePath, true);
            } else if (mt.equals(MediaType.getImageMediaType())) {
                scanPictures(filePath, true);
            } else if (mt.equals(MediaType.getVideoMediaType())) {
                scanVideo(filePath, true); // until we integrate mplayer for video and research metadata extraction
            } else {
                scanDocument(filePath, true);
            }

        } catch (Throwable e) {
            scanDocument(filePath, true);
            LOG.log(Level.WARNING, "Error scanning file, scanned as document: " + filePath, e);
        }
    }

    private void scanPictures(String filePath, boolean shared) {
        File file = new File(filePath);

        ContentValues values = new ContentValues();

        String mime = "image/" + FilenameUtils.getExtension(filePath);
        fillCommonValues(values, Constants.FILE_TYPE_PICTURES, filePath, file, mime, shared);

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            ExifIFD0Directory dir = metadata.getDirectory(ExifIFD0Directory.class);
            ExifIFD0Descriptor desc = new ExifIFD0Descriptor(dir);

            String title = desc.getWindowsTitleDescription();
            if (StringUtils.isNullOrEmpty(title, true)) {
                title = FilenameUtils.getBaseName(file.getName());
            }

            String artist = desc.getWindowsAuthorDescription();
            if (StringUtils.isNullOrEmpty(artist, true)) {
                artist = dir.getString(ExifIFD0Directory.TAG_ARTIST, "UTF-8");
            }
            if (StringUtils.isNullOrEmpty(artist, true)) {
                artist = "";
            }

            String album = "";
            String year = dir.getString(ExifIFD0Directory.TAG_DATETIME);
            if (StringUtils.isNullOrEmpty(year, true)) {
                year = "";
            }

            values.put(Columns.TITLE, title);
            values.put(Columns.ARTIST, artist);
            values.put(Columns.ALBUM, album);
            values.put(Columns.YEAR, year);
        } catch (Throwable e) {
            String displayName = FilenameUtils.getBaseName(file.getName());

            values.put(Columns.TITLE, displayName);
            values.put(Columns.ARTIST, "");
            values.put(Columns.ALBUM, "");
            values.put(Columns.YEAR, "");
        }

        ShareFilesDB db = ShareFilesDB.intance();

        db.insert(values);
    }

    private void fillCommonValues(ContentValues values, byte fileType, String filePath, File file, String mime, boolean shared) {
        values.put(Columns.FILE_TYPE, fileType);
        values.put(Columns.FILE_PATH, filePath);
        values.put(Columns.FILE_SIZE, file.length());
        values.put(Columns.MIME, mime);
        values.put(Columns.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(Columns.DATE_MODIFIED, file.lastModified());
        values.put(Columns.SHARED, shared);
    }

    private void scanAudio(String filePath, boolean shared) {
        File file = new File(filePath);

        ContentValues values = new ContentValues();

        String mime = "audio/" + FilenameUtils.getExtension(filePath);
        fillCommonValues(values, Constants.FILE_TYPE_AUDIO, filePath, file, mime, shared);

        try {
            TagsData mt = new TagsReader(file).parse();

            values.put(Columns.TITLE, mt.getTitle());
            values.put(Columns.ARTIST, mt.getArtist());
            values.put(Columns.ALBUM, mt.getAlbum());
            values.put(Columns.YEAR, mt.getYear());
        } catch (Throwable e) {
            String displayName = FilenameUtils.getBaseName(file.getName());

            values.put(Columns.TITLE, displayName);
            values.put(Columns.ARTIST, "");
            values.put(Columns.ALBUM, "");
            values.put(Columns.YEAR, "");
        }

        ShareFilesDB db = ShareFilesDB.intance();

        db.insert(values);
    }

    private void scanVideo(String filePath, boolean shared) {
        String mime = "video/" + FilenameUtils.getExtension(filePath);
        scanBasic(Constants.FILE_TYPE_VIDEOS, filePath, mime, shared);
    }

    private void scanDocument(String filePath, boolean shared) {
        scanBasic(Constants.FILE_TYPE_DOCUMENTS, filePath, getMimeType(filePath), shared);
    }

    private void scanBasic(byte fileType, String filePath, String mime, boolean shared) {
        File file = new File(filePath);

        String displayName = FilenameUtils.getBaseName(file.getName());

        ContentValues values = new ContentValues();

        fillCommonValues(values, fileType, filePath, file, mime, shared);

        values.put(Columns.TITLE, displayName);
        values.put(Columns.ARTIST, "");
        values.put(Columns.ALBUM, "");
        values.put(Columns.YEAR, "");

        ShareFilesDB db = ShareFilesDB.intance();

        db.insert(values);
    }

    private static String getMimeType(String filePath) {
        try {
            return MimeDetector.getMimeType(FilenameUtils.getExtension(filePath));
        } catch (Throwable e) {
            LOG.log(Level.WARNING, "Failed to read mime type for: " + filePath);
            return MimeDetector.UNKNOWN;
        }
    }
}
