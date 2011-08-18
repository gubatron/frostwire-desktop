package com.limegroup.gnutella.gui.search.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SmartSearchDB {

    public static final int OBJECT_NOT_SAVED_ID = -1;
    public static final int OBJECT_INVALID_ID = -2;
    
    public static final String DEFAULT_PLAYLIST_NAME = "Default";
    public static final String DEFAULT_PLAYLIST_DESCRIPTION = "Default playlist";

    public static final int SMART_SEARCH_DATABASE_VERSION = 1;

    private final File _databaseFile;
    private final String _name;

    private final Connection _connection;

    private boolean _closed;

    static {
        try {
            //Class.forName("org.hsqldb.jdbcDriver");
        	Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public SmartSearchDB(File databaseFile) {
        _databaseFile = databaseFile;

        File path = databaseFile;
        _name = databaseFile.getName();

        _connection = openOrCreateDatabase(path, _name);
    }

    public File getDatabaseFile() {
        return _databaseFile;
    }

    public String getName() {
        return _name;
    }

    public boolean isClosed() {
        return _closed;
    }

    public synchronized List<List<Object>> query(String expression) {
        if (isClosed()) {
            return new ArrayList<List<Object>>();
        }

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

    /**
     * This method is synchronized due to possible concurrent issues, specially
     * during recently generated id retrieval.
     * @param expression
     * @return
     */
    public synchronized int update(String expression) {
        if (isClosed()) {
            return -1;
        }

        return update(_connection, expression);
    }

    /**
     * This method is synchronized due to possible concurrent issues, specially
     * during recently generated id retrieval.
     * @param expression
     * @return
     */
    public synchronized int insert(String expression) {
        if (isClosed()) {
            return OBJECT_INVALID_ID;
        }

        if (!expression.toUpperCase().startsWith("INSERT")) {
            return OBJECT_INVALID_ID;
        }

        if (update(expression) != -1) {
            return getIdentity();
        }

        return OBJECT_INVALID_ID;
    }

    public synchronized void close() {
    	System.out.println("SmartSearchDB is shutting down.");

    	if (isClosed()) {
            return;
        }

        _closed = true;

        try {
            Statement statement = _connection.createStatement();
            statement.execute("SHUTDOWN");
            _connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Connection openConnection(File path, String name, boolean createIfNotExists) {
        try {
            StringBuilder sb = new StringBuilder();
            //sb.append("jdbc:hsqldb:file:");
            sb.append("jdbc:h2:");
            sb.append(new File(path, name).getAbsolutePath());
            //sb.append(";hsqldb.default_table_type=cached;");

            if (!createIfNotExists) {
                sb.append(";ifexists=true");
            }
            return DriverManager.getConnection(sb.toString(), "SA", "");
        } catch (Exception e) {
            return null;
        }
    }

    private Connection createDatabase(File path, String name) {
        Connection connection = openConnection(path, name, true);

        update(connection, "SET IGNORECASE TRUE");
        
        //TORRENTS
        update(connection, "CREATE TABLE TORRENTS (torrentId INTEGER IDENTITY, infoHash VARCHAR(60), timestamp BIGINT, torrentName VARCHAR(256), seeds INTEGER, json VARCHAR(32768))");
        update(connection, "CREATE INDEX idxTorrents ON TORRENTS (infoHash)");
        update(connection, "CREATE INDEX idxSeeds ON TORRENTS(seeds)");
        
        //FILES
        update(connection, "CREATE ALIAS IF NOT EXISTS FT_INIT FOR \"org.h2.fulltext.FullText.init\"");
        update(connection, "CALL FT_INIT()");
        
        update(connection, "CREATE TABLE FILES (fileId INTEGER IDENTITY, torrentId INTEGER, fileName VARCHAR(256), json VARCHAR(32768))");
        //update(connection, "CREATE INDEX idxFiles ON Files (fileName)");
        update(connection,"CALL FT_CREATE_INDEX('PUBLIC','FILES','FILENAME')");
        update(connection, "CREATE INDEX idxTorrentId ON FILES (torrentId)");
        
        //SNAPSHOTS - (Created right before user imports a DB, this way the user can delete (rollback) all new insertions after the snapshot)
        update(connection, "CREATE TABLE Snapshots (snapshotId INTEGER IDENTITY, timestamp BIGINT)");

        /** This table keeps only a single row to identify what version of the database we have */
        update(connection, "CREATE TABLE SmartSearchMetaData (smartSearchId INTEGER IDENTITY, name VARCHAR(500), version INTEGER)");
        update(connection, "INSERT INTO SmartSearchMetaData (name , version) VALUES ('" + name + "', " + SMART_SEARCH_DATABASE_VERSION + ")");
        
        return connection;
    }

    private Connection openOrCreateDatabase(File path, String name) {
        Connection connection = openConnection(path, name, false);
        if (connection == null) {
            return createDatabase(path, name);
        } else {            
            return connection;
        }
    }

    private List<List<Object>> convertResultSetToList(ResultSet resultSet) throws SQLException {
        ResultSetMetaData meta = resultSet.getMetaData();
        int numColums = meta.getColumnCount();
        int i;

        List<List<Object>> result = new LinkedList<List<Object>>();

        while (resultSet.next()) {
            List<Object> row = new ArrayList<Object>(numColums);
            for (i = 1; i <= numColums; i++) {
                row.add(resultSet.getObject(i));
            }
            result.add(row);
        }
        return result;
    }

    private int getIdentity() {
        if (isClosed()) {
            return OBJECT_INVALID_ID;
        }

        Statement statment = null;
        ResultSet resultSet = null;

        try {
            statment = _connection.createStatement();
            resultSet = statment.executeQuery("CALL IDENTITY()");

            resultSet.next();

            return resultSet.getInt(1);
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

        return OBJECT_INVALID_ID;
    }

    private int update(Connection connection, String expression) {

        Statement statment = null;

        try {
            statment = connection.createStatement();

            return statment.executeUpdate(expression);
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

        return -1;
    }
}
