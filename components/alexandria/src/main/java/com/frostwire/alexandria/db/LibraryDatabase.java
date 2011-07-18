package com.frostwire.alexandria.db;

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

public class LibraryDatabase {

    public static final int OBJECT_NOT_SAVED_ID = -1;
    public static final int OBJECT_INVALID_ID = -2;

    public static final int LIBRARY_DATABASE_VERSION = 1;

    private final File _databaseFile;
    private final String _name;

    private final Connection _connection;

    private boolean _closed;

    static {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public LibraryDatabase(File databaseFile) {
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

    public void close() {
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
            sb.append("jdbc:hsqldb:file:");
            sb.append(new File(path, name).getAbsolutePath());

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

        // STRUCTURE CREATION

        update(connection, "CREATE TABLE Library (libraryId INTEGER IDENTITY, name VARCHAR(500), version INTEGER)");

        //update(connection, "DROP TABLE Playlists IF EXISTS CASCADE");
        update(connection, "CREATE TABLE Playlists (playlistId INTEGER IDENTITY, name VARCHAR(500))");
        update(connection, "CREATE INDEX idx_Playlists_name ON Playlists (name)");

        //update(connection, "DROP TABLE PlaylistItems IF EXISTS CASCADE");
        update(connection,
                "CREATE TABLE PlaylistItems (playlistItemId INTEGER IDENTITY, filePath VARCHAR(10000), fileName VARCHAR(500), fileSize BIGINT, fileExtension VARCHAR(10), trackTitle VARCHAR(500), duration BIGINT, artistName VARCHAR(500), albumName VARCHAR(500), coverArtPath VARCHAR(10000))");
        update(connection, "CREATE INDEX idx_PlaylistItems_fileName ON PlaylistItems (fileName)");
        update(connection, "CREATE INDEX idx_PlaylistItems_fileExtension ON PlaylistItems (fileExtension)");
        update(connection, "CREATE INDEX idx_PlaylistItems_trackTitle ON PlaylistItems (trackTitle)");
        update(connection, "CREATE INDEX idx_PlaylistItems_artistName ON PlaylistItems (artistName)");
        update(connection, "CREATE INDEX idx_PlaylistItems_albumName ON PlaylistItems (albumName)");

        //update(connection, "DROP TABLE PlaylistsPlaylistItems IF EXISTS CASCADE");
        update(connection, "CREATE TABLE PlaylistsPlaylistItems (playlistPlaylistItemId INTEGER IDENTITY, playlistId INTEGER, playlistItemId INTEGER)");

        // INITIAL DATA
        update(connection, "INSERT INTO LibraryInfo (name , version) VALUES ('" + name + "', " + LIBRARY_DATABASE_VERSION + ")");

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
