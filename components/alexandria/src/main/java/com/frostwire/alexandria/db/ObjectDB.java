package com.frostwire.alexandria.db;

import java.util.List;

public abstract class ObjectDB<T> {

    protected final LibraryDatabase db;

    public ObjectDB(LibraryDatabase db) {
        this.db = db;
    }

    public LibraryDatabase getDatabase() {
        return db;
    }

    public abstract void fill(T obj);

    public abstract void fill(List<Object> row, T obj);

    public abstract void save(T obj);

    public abstract void delete(T obj);
}
