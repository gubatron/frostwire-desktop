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

package com.frostwire.core.providers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.frostwire.content.ContentProvider;
import com.frostwire.content.ContentUris;
import com.frostwire.content.ContentValues;
import com.frostwire.content.Context;
import com.frostwire.content.UriMatcher;
import com.frostwire.core.Constants;
import com.frostwire.core.providers.UniversalStore.Documents;
import com.frostwire.core.providers.UniversalStore.Documents.DocumentsColumns;
import com.frostwire.core.providers.UniversalStore.Documents.Media;
import com.frostwire.database.Cursor;
import com.frostwire.database.SQLException;
import com.frostwire.database.sqlite.SQLiteDatabase;
import com.frostwire.database.sqlite.SQLiteOpenHelper;
import com.frostwire.database.sqlite.SQLiteQueryBuilder;
import com.frostwire.net.Uri;
import com.frostwire.text.TextUtils;
import com.limegroup.gnutella.gui.init.SetupManager;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class FilesProvider extends ContentProvider {

    private static final Logger LOG = Logger.getLogger(FilesProvider.class.getName());

    private static final String DATABASE_NAME = "files";

    private static final int DATABASE_VERSION = 1;

    private static final String AUDIO_TABLE_NAME = "audio";
    private static final String PICTURES_TABLE_NAME = "pictures";
    private static final String VIDEOS_TABLE_NAME = "videos";
    private static final String DOCUMENTS_TABLE_NAME = "documents";
    private static final String APPLICATIONS_TABLE_NAME = "applications";
    private static final String RINGTONES_TABLE_NAME = "ringtones";

    private static final int AUDIO_ALL = 1;
    private static final int PICTURES_ALL = 2;
    private static final int VIDEOS_ALL = 3;
    private static final int DOCUMENTS_ALL = 4;
    private static final int APPLICATIONS_ALL = 5;
    private static final int RINGTONES_ALL = 6;

    private static final int AUDIO_ID = 7;
    private static final int PICTURES_ID = 8;
    private static final int VIDEOS_ID = 9;
    private static final int DOCUMENTS_ID = 10;
    private static final int APPLICATIONS_ID = 11;
    private static final int RINGTONES_ID = 12;

    private static final UriMatcher uriMatcher;

    private static HashMap<Integer, String> tables;
    private static HashMap<Integer, HashMap<String, String>> projections;

    private DatabaseHelper databaseHelper;

    static {

        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(MediaStore.AUTHORITY, "external/audio/media", AUDIO_ALL);
        uriMatcher.addURI(MediaStore.AUTHORITY, "external/images/media", PICTURES_ALL);
        uriMatcher.addURI(MediaStore.AUTHORITY, "external/video/media", VIDEOS_ALL);
        uriMatcher.addURI(UniversalStore.UNIVERSAL_DOCUMENTS_AUTHORITY, "documents", DOCUMENTS_ALL);
        uriMatcher.addURI(UniversalStore.UNIVERSAL_APPLICATIONS_AUTHORITY, "applications", APPLICATIONS_ALL);
        uriMatcher.addURI(MediaStore.AUTHORITY, "internal/audio/media", RINGTONES_ALL);

        uriMatcher.addURI(MediaStore.AUTHORITY, "external/audio/media/#", AUDIO_ID);
        uriMatcher.addURI(MediaStore.AUTHORITY, "external/images/media/#", PICTURES_ID);
        uriMatcher.addURI(MediaStore.AUTHORITY, "external/video/media/#", VIDEOS_ID);
        uriMatcher.addURI(UniversalStore.UNIVERSAL_DOCUMENTS_AUTHORITY, "documents/#", DOCUMENTS_ID);
        uriMatcher.addURI(UniversalStore.UNIVERSAL_APPLICATIONS_AUTHORITY, "documents/#", APPLICATIONS_ID);
        uriMatcher.addURI(MediaStore.AUTHORITY, "internal/audio/media/#", RINGTONES_ID);

        tables = new HashMap<Integer, String>();

        tables.put(AUDIO_ALL, AUDIO_TABLE_NAME);
        tables.put(PICTURES_ALL, PICTURES_TABLE_NAME);
        tables.put(VIDEOS_ALL, VIDEOS_TABLE_NAME);
        tables.put(DOCUMENTS_ALL, DOCUMENTS_TABLE_NAME);
        tables.put(APPLICATIONS_ALL, APPLICATIONS_TABLE_NAME);
        tables.put(RINGTONES_ALL, RINGTONES_TABLE_NAME);

        tables.put(AUDIO_ID, AUDIO_TABLE_NAME);
        tables.put(PICTURES_ID, PICTURES_TABLE_NAME);
        tables.put(VIDEOS_ID, VIDEOS_TABLE_NAME);
        tables.put(DOCUMENTS_ID, DOCUMENTS_TABLE_NAME);
        tables.put(APPLICATIONS_ID, APPLICATIONS_TABLE_NAME);
        tables.put(RINGTONES_ID, RINGTONES_TABLE_NAME);

        projections = new HashMap<Integer, HashMap<String, String>>();

        projections.put(DOCUMENTS_ALL, getDocumentsProjectionMap());
    }

    public FilesProvider(Context context) {
        super(context);
    }

    @Override
    public boolean onCreate() {

        databaseHelper = new DatabaseHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(DOCUMENTS_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
        case DOCUMENTS_ALL:
            //qb.setProjectionMap(documentsPM);
            break;

        case DOCUMENTS_ID:
            //qb.setProjectionMap(documentsPM);
            qb.appendWhere(DocumentsColumns._ID + "=" + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;

        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = Documents.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
        case DOCUMENTS_ALL:
            return Media.CONTENT_TYPE;
        case DOCUMENTS_ID:
            return Media.CONTENT_TYPE_ITEM;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (uriMatcher.match(uri) != DOCUMENTS_ALL) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;

        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis() / 1000);

        // Make sure that the fields are all set
        if (values.containsKey(DocumentsColumns.DATA) == false) {
            values.put(DocumentsColumns.DATA, "");
        }

        if (values.containsKey(DocumentsColumns.SIZE) == false) {
            values.put(DocumentsColumns.SIZE, 0);
        }

        if (values.containsKey(DocumentsColumns.DISPLAY_NAME) == false) {
            values.put(DocumentsColumns.DISPLAY_NAME, "");
        }

        if (values.containsKey(DocumentsColumns.TITLE) == false) {
            values.put(DocumentsColumns.TITLE, "--");
        }

        if (values.containsKey(DocumentsColumns.DATE_ADDED) == false) {
            values.put(DocumentsColumns.DATE_ADDED, now);
        }

        if (values.containsKey(DocumentsColumns.DATE_MODIFIED) == false) {
            values.put(DocumentsColumns.DATE_MODIFIED, now);
        }

        if (values.containsKey(DocumentsColumns.MIME_TYPE) == false) {
            values.put(DocumentsColumns.MIME_TYPE, "");
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        long rowId = db.insert(DOCUMENTS_TABLE_NAME, "", values);

        if (rowId > 0) {

            Uri documentUri = ContentUris.withAppendedId(Documents.Media.CONTENT_URI, rowId);
            //getContext().getContentResolver().notifyChange(documentUri, null);

            return documentUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        int count;

        switch (uriMatcher.match(uri)) {
        case DOCUMENTS_ALL:
            count = db.delete(DOCUMENTS_TABLE_NAME, where, whereArgs);
            break;

        case DOCUMENTS_ID:
            String documentId = uri.getPathSegments().get(1);
            count = db.delete(DOCUMENTS_TABLE_NAME, DocumentsColumns._ID + "=" + documentId + (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        //getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
    
    private static HashMap<String, String> getAudioProjectionMap() {
        HashMap<String, String> map = new HashMap<String, String>();

        map.put(DocumentsColumns._ID, DocumentsColumns._ID);
        map.put(DocumentsColumns.DATA, DocumentsColumns.DATA);
        map.put(DocumentsColumns.SIZE, DocumentsColumns.SIZE);
        map.put(DocumentsColumns.DISPLAY_NAME, DocumentsColumns.DISPLAY_NAME);
        map.put(DocumentsColumns.TITLE, DocumentsColumns.TITLE);
        map.put(DocumentsColumns.DATE_ADDED, DocumentsColumns.DATE_ADDED);
        map.put(DocumentsColumns.DATE_MODIFIED, DocumentsColumns.DATE_MODIFIED);
        map.put(DocumentsColumns.MIME_TYPE, DocumentsColumns.MIME_TYPE);

        return map;
    }

    private static HashMap<String, String> getDocumentsProjectionMap() {
        HashMap<String, String> map = new HashMap<String, String>();

        map.put(DocumentsColumns._ID, DocumentsColumns._ID);
        map.put(DocumentsColumns.DATA, DocumentsColumns.DATA);
        map.put(DocumentsColumns.SIZE, DocumentsColumns.SIZE);
        map.put(DocumentsColumns.DISPLAY_NAME, DocumentsColumns.DISPLAY_NAME);
        map.put(DocumentsColumns.TITLE, DocumentsColumns.TITLE);
        map.put(DocumentsColumns.DATE_ADDED, DocumentsColumns.DATE_ADDED);
        map.put(DocumentsColumns.DATE_MODIFIED, DocumentsColumns.DATE_MODIFIED);
        map.put(DocumentsColumns.MIME_TYPE, DocumentsColumns.MIME_TYPE);

        return map;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        int count;

        switch (uriMatcher.match(uri)) {
        case DOCUMENTS_ALL:
            count = db.update(DOCUMENTS_TABLE_NAME, values, where, whereArgs);
            break;

        case DOCUMENTS_ID:
            String documentId = uri.getPathSegments().get(1);
            count = db.update(DOCUMENTS_TABLE_NAME, values, DocumentsColumns._ID + "=" + documentId + (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        //getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("SET IGNORECASE TRUE");

            db.execSQL("CREATE TABLE " + DOCUMENTS_TABLE_NAME + " (" + DocumentsColumns._ID + " INTEGER IDENTITY," + DocumentsColumns.DATA + " VARCHAR," + DocumentsColumns.SIZE + " INTEGER," + DocumentsColumns.DISPLAY_NAME + " VARCHAR," + DocumentsColumns.TITLE + " VARCHAR,"
                    + DocumentsColumns.DATE_ADDED + " BIGINT," + DocumentsColumns.DATE_MODIFIED + " BIGINT," + DocumentsColumns.MIME_TYPE + " VARCHAR" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            LOG.warning("Upgrading documents database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DOCUMENTS_TABLE_NAME);
            onCreate(db);
        }
    }
}