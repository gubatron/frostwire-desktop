package com.frostwire.alexandria.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LibraryDB {

    private final File _databaseFile;

    private final Connection _connection;

    static {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public LibraryDB(File databaseFile) {
        _databaseFile = databaseFile;
        _connection = openOrCreateDatabase();
    }

    public synchronized List<List<Object>> query(String expression) {

        Statement statment = null;
        ResultSet resultSet = null;

        try {

            statment = _connection.createStatement();
            resultSet = statment.executeQuery(expression);

            return convertResultSetToList(resultSet);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statment != null) {
                try {
                    statment.close();
                } catch (SQLException e) {
                }
            }
        }

        return new ArrayList<List<Object>>();
    }

    public void close() {
        try {
            Statement statement = _connection.createStatement();
            statement.execute("SHUTDOWN");
            _connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Connection openConnection(boolean createIfNotExists) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("jdbc:hsqldb:file:");
            sb.append(_databaseFile.getAbsolutePath());

            if (!createIfNotExists) {
                sb.append(";ifexists=true");
            }
            return DriverManager.getConnection(sb.toString(), "SA", "");
        } catch (Exception e) {
            return null;
        }
    }

    private Connection createDatabase() {
        Connection connection = openConnection(true);

        return connection;
    }

    private Connection openOrCreateDatabase() {
        Connection connection = openConnection(false);
        if (connection == null) {
            return createDatabase();
        } else {
            return null;
        }
    }

    private List<List<Object>> convertResultSetToList(ResultSet resultSet) {
//     ResultSetMetaData meta   = resultSet.getMetaData();
//        int               numColums = meta.getColumnCount();
//        int               i;
//        Object            o = null;
//
//        // the result set is a cursor into the data.  You can only
//        // point to one row at a time
//        // assume we are pointing to BEFORE the first row
//        // rs.next() points to next row and returns true
//        // or false if there is no next row, which breaks the loop
//        for (; rs.next(); ) {
//            for (i = 0; i < colmax; ++i) {
//                o = rs.getObject(i + 1);    // Is SQL the first column is indexed
//
//                // with 1 not 0
//                System.out.print(o.toString() + " ");
//            }
//
//            System.out.println(" ");
//        }
        return null;
    }
}
