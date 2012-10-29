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
import com.frostwire.core.Constants;
import com.frostwire.core.FileDescriptor;
import com.frostwire.core.providers.UniversalStore;
import com.frostwire.core.providers.UniversalStore.Documents;
import com.frostwire.core.providers.UniversalStore.Documents.DocumentsColumns;
import com.frostwire.database.Cursor;
import com.frostwire.net.Uri;

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
        scanDocument(filePath);
    }

    private void scanDocument(String filePath) {
        File file = new File(filePath);
        
        if (documentExists(filePath, file.length())) {
            return;
        }

        String displayName = FilenameUtils.getBaseName(file.getName());

        ContentResolver cr = context.getContentResolver();

        ContentValues values = new ContentValues();

        values.put(DocumentsColumns.DATA, filePath);
        values.put(DocumentsColumns.SIZE, file.length());
        values.put(DocumentsColumns.DISPLAY_NAME, displayName);
        values.put(DocumentsColumns.TITLE, displayName);
        values.put(DocumentsColumns.DATE_ADDED, System.currentTimeMillis());
        values.put(DocumentsColumns.DATE_MODIFIED, file.lastModified());
        values.put(DocumentsColumns.MIME_TYPE, getMimeType(filePath));

        Uri uri = cr.insert(Documents.Media.CONTENT_URI, values);

        FileDescriptor fd = new FileDescriptor();
        fd.fileType = Constants.FILE_TYPE_DOCUMENTS;
        fd.id = Integer.valueOf(uri.getLastPathSegment());
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
