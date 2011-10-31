package com.frostwire.alexandria;

import java.io.File;
import java.util.List;

import com.frostwire.alexandria.db.LibraryDB;
import com.frostwire.alexandria.db.LibraryDatabase;

public class Library extends Entity<LibraryDB> {

    private int _id;
    private String _name;
    private int _version;

    public Library(File libraryFile) {
        super(new LibraryDB(new LibraryDatabase(libraryFile)));
        db.fill(this);
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

    public void close() {
        db.getDatabase().close();
    }

    public List<Playlist> getPlaylists() {
        return db.getPlaylists(this);
    }

    public Playlist getPlaylist(String name) {
        return db.getPlaylist(this, name);
    }
    
    public List<InternetRadioStation> getInternetRadioStations() {
        return db.getInternetRadioStations(this);
    }

    public Playlist newPlaylist(String name, String description) {
        return new Playlist(this, LibraryDatabase.OBJECT_NOT_SAVED_ID, name, description);
    }

    public void dump() {
        db.getDatabase().dump();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public Playlist getStarredPlaylist() {
        return db.getStarredPlaylist(this);
    }

    public void updatePlaylistItemProperties(String filePath, String title, String artist, String album, String comment, String genre, String track, String year) {
        db.updatePlaylistItemProperties(filePath, title, artist, album, comment, genre, track, year);
    }
}
