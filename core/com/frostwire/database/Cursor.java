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

package com.frostwire.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class Cursor {

    private static final Logger LOG = Logger.getLogger(Cursor.class.getName());

    private final Statement statement;
    private final ResultSet resultSet;

    public Cursor(Statement statement, ResultSet resultSet) {
        this.statement = statement;
        this.resultSet = resultSet;
    }

    public int getInt(int idCol) {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getString(int pathCol) {
        // TODO Auto-generated method stub
        return null;
    }

    public long getLong(int dateAddedCol) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getColumnIndex(String dateAdded) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void close() {
        try {
            resultSet.close();
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Error closing cursor result set", e);
        }
        try {
            statement.close();
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Error closing cursor inner statement", e);
        }
    }

    public boolean moveToPosition(int offset) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean moveToNext() {
        // TODO Auto-generated method stub
        return false;
    }

}
