package com.frostwire.alexandria.db;

import java.util.List;

import com.frostwire.alexandria.Library;

public class LibraryDB extends ObjectDB<Library> {

    public LibraryDB(LibraryDatabase db) {
        super(db);
    }

    public void fill(Library obj) {
        // this is a special case, since we will have only one library per database
        List<List<Object>> result = db.query("SELECT libraryId, name, version FROM Library");
        if (result.size() > 0) {
            List<Object> row = result.get(0);
            fill(row, obj);
        }
    }

    public void fill(List<Object> row, Library obj) {
        int id = (Integer) row.get(0);
        String name = (String) row.get(1);
        int version = (Integer) row.get(2);

        obj.setId(id);
        obj.setName(name);
        obj.setVersion(version);
    }

    public void save(Library obj) {
        // nothing
    }

    public void delete(Library obj) {
        // nothing
    }
}
