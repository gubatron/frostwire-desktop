package com.frostwire.alexandria;

import java.io.File;

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
        getDB().getDatabase().close();
    }

    public Playlist newPlaylist(String name) {
        return new Playlist(this, LibraryDatabase.OBJECT_NOT_SAVED_ID, name);
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
