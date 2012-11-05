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
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.limewire.util.FilenameUtils;

import com.frostwire.content.ContentResolver;
import com.frostwire.content.ContentValues;
import com.frostwire.content.Context;
import com.frostwire.core.CommonConstants;
import com.frostwire.core.Constants;
import com.frostwire.core.FileDescriptor;
import com.frostwire.core.providers.ShareFilesDB;
import com.frostwire.core.providers.UniversalStore;
import com.frostwire.core.providers.ShareFilesDB.Columns;
import com.frostwire.core.providers.UniversalStore.Documents;
import com.frostwire.core.providers.UniversalStore.Documents.DocumentsColumns;
import com.frostwire.database.Cursor;
import com.frostwire.gui.library.AudioMetaData;
import com.frostwire.net.Uri;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.gui.search.NamedMediaType;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class UniversalScanner {

    private static final Logger LOG = Logger.getLogger(UniversalScanner.class.getName());

    private final Context context;

    public UniversalScanner(Context context) {
        this.context = context;
    }

    public void scan(String filePath) {
        try {
            MediaType mt = MediaType.getMediaTypeForExtension(FilenameUtils.getExtension(filePath));

            if (mt.equals(MediaType.getAudioMediaType())) {
                scanAudio(filePath, true);
            } else if (mt.equals(MediaType.getImageMediaType())) {
                //scanPictures
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

    private void fillCommonValues(ContentValues values, byte fileType, String filePath, File file, boolean shared) {
        values.put(Columns.FILE_TYPE, fileType);
        values.put(Columns.FILE_PATH, filePath);
        values.put(Columns.FILE_SIZE, file.length());
        values.put(Columns.MIME, getMimeType(filePath));
        values.put(Columns.DATE_ADDED, System.currentTimeMillis());
        values.put(Columns.DATE_MODIFIED, file.lastModified());
        values.put(Columns.SHARED, shared);
    }

    private void scanAudio(String filePath, boolean shared) {
        File file = new File(filePath);

        AudioMetaData mt = new AudioMetaData(file);

        ContentValues values = new ContentValues();

        fillCommonValues(values, Constants.FILE_TYPE_AUDIO, filePath, file, shared);

        values.put(Columns.TITLE, mt.getTitle());
        values.put(Columns.ARTIST, mt.getArtist());
        values.put(Columns.ALBUM, mt.getAlbum());
        values.put(Columns.YEAR, mt.getYear());

        ShareFilesDB db = ShareFilesDB.intance();

        db.insert(values);
    }
    
    private void scanVideo(String filePath, boolean shared) {
        scanBasic(Constants.FILE_TYPE_VIDEOS, filePath, shared);
    }
    
    private void scanDocument(String filePath, boolean shared) {
        scanBasic(Constants.FILE_TYPE_DOCUMENTS, filePath, shared);
    }
    
    private void scanBasic(byte fileType, String filePath, boolean shared) {
        File file = new File(filePath);

        String displayName = FilenameUtils.getBaseName(file.getName());

        ContentValues values = new ContentValues();

        fillCommonValues(values, fileType, filePath, file, shared);

        values.put(Columns.TITLE, displayName);
        values.put(Columns.ARTIST, "");
        values.put(Columns.ALBUM, "");
        values.put(Columns.YEAR, "");

        ShareFilesDB db = ShareFilesDB.intance();

        db.insert(values);
    }

    private boolean documentExists(String filePath, long size) {
        boolean result = false;

        Cursor c = null;

        try {
            ContentResolver cr = context.getContentResolver();
            c = cr.query(UniversalStore.Documents.Media.CONTENT_URI, new String[] { DocumentsColumns._ID }, DocumentsColumns.DATA + "=?" + " AND " + DocumentsColumns.SIZE + "=?", new String[] { filePath, String.valueOf(size) }, null);
            result = c != null && c.getCount() != 0;
        } catch (Throwable e) {
            LOG.log(Level.WARNING, "Error detecting if file exists: " + filePath, e);
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return result;
    }

    public static String getMimeType(String filePath) {
        try {
            URL u = new URL("file://" + filePath);
            URLConnection uc = null;
            uc = u.openConnection();
            return uc.getContentType();
        } catch (Throwable e) {
            LOG.log(Level.WARNING, "Failed to read mime type for: " + filePath);
            return "";
        }
    }
}
