package com.frostwire.alexandria.db;

public abstract class ObjectDB<T> {

    protected final LibraryDatabase db;

    public ObjectDB(LibraryDatabase db) {
        this.db = db;
    }

    public abstract void fill(T obj);
}
