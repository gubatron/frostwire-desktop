/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.frostwire.database.sqlite;

import com.frostwire.content.ContentValues;
import com.frostwire.database.Cursor;
import com.frostwire.database.SQLException;

public class SQLiteDatabase {

    /**
     * Runs the provided SQL and returns a cursor over the result set.
     *
     * @param cursorFactory the cursor factory to use, or null for the default factory
     * @param sql the SQL query. The SQL string must not be ; terminated
     * @param selectionArgs You may include ?s in where clause in the query,
     *     which will be replaced by the values from selectionArgs. The
     *     values will be bound as Strings.
     * @param editTable the name of the first table, which is editable
     * @return A {@link Cursor} object, which is positioned before the first entry. Note that
     * {@link Cursor}s are not synchronized, see the documentation for more details.
     */
    public Cursor rawQueryWithFactory(CursorFactory cursorFactory, String sql, String[] selectionArgs, String editTable) {
        return null;
    }

    /**
     * Execute a single SQL statement that is NOT a SELECT
     * or any other SQL statement that returns data.
     * <p>
     * It has no means to return any data (such as the number of affected rows).
     * Instead, you're encouraged to use {@link #insert(String, String, ContentValues)},
     * {@link #update(String, ContentValues, String, String[])}, et al, when possible.
     * </p>
     * <p>
     * When using {@link #enableWriteAheadLogging()}, journal_mode is
     * automatically managed by this class. So, do not set journal_mode
     * using "PRAGMA journal_mode'<value>" statement if your app is using
     * {@link #enableWriteAheadLogging()}
     * </p>
     *
     * @param sql the SQL statement to be executed. Multiple statements separated by semicolons are
     * not supported.
     * @throws SQLException if the SQL string is invalid
     */
    public void execSQL(String sql) throws SQLException {
        executeSql(sql, null);
    }

    /**
     * Execute a single SQL statement that is NOT a SELECT/INSERT/UPDATE/DELETE.
     * <p>
     * For INSERT statements, use any of the following instead.
     * <ul>
     *   <li>{@link #insert(String, String, ContentValues)}</li>
     *   <li>{@link #insertOrThrow(String, String, ContentValues)}</li>
     *   <li>{@link #insertWithOnConflict(String, String, ContentValues, int)}</li>
     * </ul>
     * <p>
     * For UPDATE statements, use any of the following instead.
     * <ul>
     *   <li>{@link #update(String, ContentValues, String, String[])}</li>
     *   <li>{@link #updateWithOnConflict(String, ContentValues, String, String[], int)}</li>
     * </ul>
     * <p>
     * For DELETE statements, use any of the following instead.
     * <ul>
     *   <li>{@link #delete(String, String, String[])}</li>
     * </ul>
     * <p>
     * For example, the following are good candidates for using this method:
     * <ul>
     *   <li>ALTER TABLE</li>
     *   <li>CREATE or DROP table / trigger / view / index / virtual table</li>
     *   <li>REINDEX</li>
     *   <li>RELEASE</li>
     *   <li>SAVEPOINT</li>
     *   <li>PRAGMA that returns no data</li>
     * </ul>
     * </p>
     * <p>
     * When using {@link #enableWriteAheadLogging()}, journal_mode is
     * automatically managed by this class. So, do not set journal_mode
     * using "PRAGMA journal_mode'<value>" statement if your app is using
     * {@link #enableWriteAheadLogging()}
     * </p>
     *
     * @param sql the SQL statement to be executed. Multiple statements separated by semicolons are
     * not supported.
     * @param bindArgs only byte[], String, Long and Double are supported in bindArgs.
     * @throws SQLException if the SQL string is invalid
     */
    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        if (bindArgs == null) {
            throw new IllegalArgumentException("Empty bindArgs");
        }
        executeSql(sql, bindArgs);
    }

    /**
     * Convenience method for inserting a row into the database.
     *
     * @param table the table to insert the row into
     * @param nullColumnHack optional; may be <code>null</code>.
     *            SQL doesn't allow inserting a completely empty row without
     *            naming at least one column name.  If your provided <code>values</code> is
     *            empty, no column names are known and an empty row can't be inserted.
     *            If not set to null, the <code>nullColumnHack</code> parameter
     *            provides the name of nullable column name to explicitly insert a NULL into
     *            in the case where your <code>values</code> is empty.
     * @param values this map contains the initial column values for the
     *            row. The keys should be the column names and the values the
     *            column values
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return 0;
    }

    /**
     * Convenience method for deleting rows in the database.
     *
     * @param table the table to delete from
     * @param whereClause the optional WHERE clause to apply when deleting.
     *            Passing null will delete all rows.
     * @return the number of rows affected if a whereClause is passed in, 0
     *         otherwise. To remove all rows and get a count pass "1" as the
     *         whereClause.
     */
    public int delete(String table, String whereClause, String[] whereArgs) {
        return 0;
    }

    /**
     * Convenience method for updating rows in the database.
     *
     * @param table the table to update in
     * @param values a map from column names to new column values. null is a
     *            valid value that will be translated to NULL.
     * @param whereClause the optional WHERE clause to apply when updating.
     *            Passing null will update all rows.
     * @return the number of rows affected
     */
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return 0;
    }

    private int executeSql(String sql, Object[] bindArgs) throws SQLException {
        //        if (DatabaseUtils.getSqlStatementType(sql) == DatabaseUtils.STATEMENT_ATTACH) {
        //            disableWriteAheadLogging();
        //            mHasAttachedDbs = true;
        //        }
        //        SQLiteStatement statement = new SQLiteStatement(this, sql, bindArgs);
        //        try {
        //            return statement.executeUpdateDelete();
        //        } catch (SQLiteDatabaseCorruptException e) {
        //            onCorruption();
        //            throw e;
        //        } finally {
        //            statement.close();
        //        }
        return 0;
    }

    /**
     * Finds the name of the first table, which is editable.
     *
     * @param tables a list of tables
     * @return the first table listed
     */
    public static String findEditTable(String tables) {
        if (!TextUtils.isEmpty(tables)) {
            // find the first word terminated by either a space or a comma
            int spacepos = tables.indexOf(' ');
            int commapos = tables.indexOf(',');

            if (spacepos > 0 && (spacepos < commapos || commapos < 0)) {
                return tables.substring(0, spacepos);
            } else if (commapos > 0 && (commapos < spacepos || spacepos < 0)) {
                return tables.substring(0, commapos);
            }
            return tables;
        } else {
            throw new IllegalStateException("Invalid tables");
        }
    }

    /**
     * Used to allow returning sub-classes of {@link Cursor} when calling query.
     */
    public interface CursorFactory {
        /**
         * See
         * {@link SQLiteCursor#SQLiteCursor(SQLiteCursorDriver, String, SQLiteQuery)}.
         */
        //        public Cursor newCursor(SQLiteDatabase db,
        //                SQLiteCursorDriver masterQuery, String editTable,
        //                SQLiteQuery query);
    }
}
