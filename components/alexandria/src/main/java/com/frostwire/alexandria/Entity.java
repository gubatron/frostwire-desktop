package com.frostwire.alexandria;

import com.frostwire.alexandria.db.ObjectDB;

public abstract class Entity<T extends ObjectDB<?>> {

    protected T db;

    public Entity(T db) {
        this.db = db;
    }

    public T getDB() {
        return db;
    }
    
    public void setDB(T db) {
        this.db = db;
    }
}
