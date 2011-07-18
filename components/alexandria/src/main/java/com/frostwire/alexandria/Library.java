package com.frostwire.alexandria;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.frostwire.alexandria.db.LibraryDB;
import com.frostwire.alexandria.db.LibraryDatabase;

public class Library {

    private final LibraryDatabase _database;
    private final LibraryDB _db;

    private boolean _closed;

    private int _id;
    private String _name;
    private int _version;

    public Library(File libraryFile) {
        _database = new LibraryDatabase(libraryFile);
        _db = new LibraryDB(_database);

        //fillLibraryInfo();
    }

    public File getLibraryFile() {
        return _database.getDatabaseFile();
    }
    
    public int getId() {
        return _id;
    }
    
    public void setId(int id) {
        _id = id;
    }

    public String getName() {
        return _name;
    }
    
    public void setName(String name) {
        _name = name;
    }

    public int getVersion() {
        return _version;
    }
    
    public void setVersion(int version) {
        _version = version;
    }
    
    public boolean isClosed() {
        return _closed;
    }

    public void close() {
        if (isClosed()) {
            return;
        }

        _closed = true;
        _database.close();
    }

    public Playlist newPlaylist(String name) {
        return new Playlist(this, LibraryDatabase.OBJECT_NOT_SAVED_ID, name);
    }

    List<PlaylistItem> getLibraryItems(Playlist playlist) {
        if (isClosed()) {
            return new ArrayList<PlaylistItem>();
        }
        
        List<List<Object>> results = _database
                .query("SELECT playlistItemId, filePath, fileName, fileSize, fileExtension, trackTitle, time, artistName, albumName, coverArtPath "
                        + "FROM PlaylisItems INNER JOIN PlaylistsPlaylistItems ON PlaylisItems.playlisItemId = PlaylistsPlaylistItems.playlisItemId "
                        + "WHERE playlistId = " + playlist.getId());

        List<PlaylistItem> items = new ArrayList<PlaylistItem>(results.size());

        for (List<Object> row : results) {
            int id = (Integer) row.get(0);
            String filePath = (String) row.get(1);
            String fileName = (String) row.get(2);
            long fileSize = (Long) row.get(3);
            String fileExtension = (String) row.get(4);
            String trackTitle = (String) row.get(5);
            long time = (Long) row.get(6);
            String artistName = (String) row.get(7);
            String albumName = (String) row.get(8);
            String coverArtPath = (String) row.get(9);

            PlaylistItem item = new PlaylistItem(playlist, id, filePath, fileName, fileSize, fileExtension, trackTitle, time, artistName, albumName,
                    coverArtPath);

            items.add(item);
        }

        return items;
    }

    void save(Playlist playlist) {
        if (isClosed()) {
            return;
        }
        
        _database.update("DELETE FROM PlaylistsPlaylistItems WHERE playlistId = " + playlist.getId());
        
       // _db.update("UPDATE ")
    }
    
    void delete(Playlist playlist) {
        if (isClosed()) {
            return;
        }
    }

    void save(PlaylistItem item) {
        if (isClosed()) {
            return;
        }
    }
    
    void delete(PlaylistItem item) {
        if (isClosed()) {
            return;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public static void main(String[] args) {
        File dbFile = new File("/home/atorres/Downloads/testalex/testdb");
        Library library = new Library(dbFile);
        library.close();
    }
}
