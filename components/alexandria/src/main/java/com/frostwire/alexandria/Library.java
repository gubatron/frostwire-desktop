package com.frostwire.alexandria;

import java.io.File;
import java.util.List;

import com.frostwire.alexandria.db.LibraryDB;

public class Library {

    private final LibraryDB _db;

    private boolean _closed;
    
    private String _name;
    private int _version;

    public Library(File libraryFile) {
        _db = new LibraryDB(libraryFile);
        
        fillLibraryInfo();
    }

    public File getLibraryFile() {
        return _db.getDatabaseFile();
    }

    public void close() {
        if (_closed) {
            return;
        }

        _closed = true;
        _db.close();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
    
    private void fillLibraryInfo() {
        List<List<Object>> result = _db.query("SELECT name, version FROM LibraryInfo");
    }

    public static void main(String[] args) {
        File dbFile = new File("/home/atorres/Downloads/testalex/testdb");
        Library library = new Library(dbFile);
        library.close();
    }
}
