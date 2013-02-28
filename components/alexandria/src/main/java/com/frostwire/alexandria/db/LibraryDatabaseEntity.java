package com.frostwire.alexandria.db;


public class LibraryDatabaseEntity {

    protected LibraryDatabase db;
    
    public LibraryDatabaseEntity(LibraryDatabase db) {
        this.db = db;
    }
    
    public void setLibraryDatabase(LibraryDatabase db) {
        this.db = db;
    }
    
    public LibraryDatabase getLibraryDatabase() {
        return db;
    }
}
