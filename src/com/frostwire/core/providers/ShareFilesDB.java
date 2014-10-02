/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
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

import com.frostwire.content.ContentValues;
import com.frostwire.content.Context;
import com.frostwire.core.Constants;
import com.frostwire.database.Cursor;
import com.frostwire.database.sqlite.SQLiteDatabase;
import com.frostwire.database.sqlite.SQLiteOpenHelper;
import com.frostwire.database.sqlite.SQLiteQueryBuilder;
import com.frostwire.logging.Logger;
import com.frostwire.text.TextUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public final class ShareFilesDB {

    private static final Logger LOG = Logger.getLogger(ShareFilesDB.class);

    private static final String DATABASE_NAME = "sharefiles";

    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "ShareFiles";

    public static final String DEFAULT_SORT_ORDER = Columns.DATE_ADDED + " DESC";

    private final DatabaseHelper databaseHelper;

    private static ShareFilesDB instance = new ShareFilesDB();

    public static ShareFilesDB intance() {
        return instance;
    }

    private ShareFilesDB() {
        databaseHelper = new DatabaseHelper(new Context());
    }

    public Cursor query(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(TABLE_NAME);

        // If no sort order is specified use the default
        String orderBy;

        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        return c;
    }

    public long insert(ContentValues initialValues) {
        ContentValues values;

        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis() / 1000);

        if (values.containsKey(Columns.FILE_TYPE) == false) {
            values.put(Columns.FILE_TYPE, Constants.FILE_TYPE_DOCUMENTS);
        }

        if (values.containsKey(Columns.FILE_PATH) == false) {
            throw new IllegalArgumentException("No file path specified");
        }

        if (values.containsKey(Columns.FILE_SIZE) == false) {
            values.put(Columns.FILE_SIZE, 0);
        }

        if (values.containsKey(Columns.MIME) == false) {
            values.put(Columns.MIME, "");
        }

        if (values.containsKey(Columns.DATE_ADDED) == false) {
            values.put(Columns.DATE_ADDED, now);
        }

        if (values.containsKey(Columns.DATE_MODIFIED) == false) {
            values.put(Columns.DATE_MODIFIED, now);
        }

        if (values.containsKey(Columns.SHARED) == false) {
            values.put(Columns.SHARED, false);
        }

        if (values.containsKey(Columns.TITLE) == false) {
            values.put(Columns.TITLE, "");
        }

        if (values.containsKey(Columns.ARTIST) == false) {
            values.put(Columns.ARTIST, "");
        }

        if (values.containsKey(Columns.ALBUM) == false) {
            values.put(Columns.ALBUM, "");
        }

        if (values.containsKey(Columns.YEAR) == false) {
            values.put(Columns.YEAR, "");
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        return db.insert(TABLE_NAME, "", values);
    }

    public int delete(String where, String[] whereArgs) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        int count = db.delete(TABLE_NAME, where, whereArgs);

        return count;
    }

    public int update(ContentValues values, String where, String[] whereArgs) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        int count = db.update(TABLE_NAME, values, where, whereArgs);

        return count;
    }

    public static final class Columns {

        private Columns() {
        }

        public static final String ID = "id";
        public static final String FILE_TYPE = "fileType";
        public static final String FILE_PATH = "filePath";
        public static final String FILE_SIZE = "fileSize";
        public static final String MIME = "mime";
        public static final String DATE_ADDED = "dateAdded";
        public static final String DATE_MODIFIED = "dateModified";
        public static final String SHARED = "shared";

        public static final String TITLE = "title";
        public static final String ARTIST = "artist";
        public static final String ALBUM = "album";
        public static final String YEAR = "year";
    }

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        // 4MB cache size and scan-resistant cache algorithm "Two Queue" (2Q) with second level soft reference
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION, "CACHE_SIZE=4096;CACHE_TYPE=SOFT_TQ");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("SET IGNORECASE TRUE");

            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + Columns.ID + " INTEGER IDENTITY," + Columns.FILE_TYPE + " INTEGER," + Columns.FILE_PATH + " VARCHAR," + Columns.FILE_SIZE + " INTEGER," + Columns.MIME + " VARCHAR," + Columns.DATE_ADDED + " BIGINT," + Columns.DATE_MODIFIED + " BIGINT,"
                    + Columns.SHARED + " BOOLEAN," + Columns.TITLE + " VARCHAR," + Columns.ARTIST + " VARCHAR," + Columns.ALBUM + " VARCHAR," + Columns.YEAR + " VARCHAR" + ");");

            db.execSQL("CREATE INDEX idx_" + TABLE_NAME + "_" + Columns.ID + " ON " + TABLE_NAME + " (" + Columns.ID + ")");
            db.execSQL("CREATE INDEX idx_" + TABLE_NAME + "_" + Columns.FILE_TYPE + " ON " + TABLE_NAME + " (" + Columns.FILE_TYPE + ")");
            db.execSQL("CREATE INDEX idx_" + TABLE_NAME + "_" + Columns.FILE_PATH + " ON " + TABLE_NAME + " (" + Columns.FILE_PATH + ")");
            db.execSQL("CREATE INDEX idx_" + TABLE_NAME + "_" + Columns.SHARED + " ON " + TABLE_NAME + " (" + Columns.SHARED + ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            LOG.warn("Upgrading documents database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
