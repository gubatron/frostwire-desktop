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

    public Playlist getDefaultPlaylist() {
        return getPlaylist(LibraryDatabase.DEFAULT_PLAYLIST_NAME);
    }

    public Playlist newPlaylist(String name, String description) {
        if (name.equals(LibraryDatabase.DEFAULT_PLAYLIST_NAME)) {
            return getDefaultPlaylist();
        } else {
            return new Playlist(this, LibraryDatabase.OBJECT_NOT_SAVED_ID, name, description);
        }
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

    public static void main(String[] args) {
        File dbFile = new File("/home/atorres/Downloads/testalex/testdb");
        Library library = new Library(dbFile);

        Playlist newPlaylist = library.newPlaylist("testPL", "test");

        newPlaylist.save();

        PlaylistItem it1 = newPlaylist.newItem("a", "a", 1, "a", "a", 1, "a", "a", "a");
        it1.save();
        PlaylistItem it2 = newPlaylist.newItem("b", "b", 1, "b", "b", 1, "b", "b", "b");
        it2.save();

        library.close();

        library = new Library(dbFile);

        Playlist pl = library.getPlaylists().get(0);
        pl.refresh();
        System.out.println(pl.getItems().size());
        library.close();
    }
}
