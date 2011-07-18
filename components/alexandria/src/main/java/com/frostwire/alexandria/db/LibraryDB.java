package com.frostwire.alexandria.db;

import java.util.List;

import com.frostwire.alexandria.Library;

public class LibraryDB extends ObjectDB<Library> {

    public LibraryDB(LibraryDatabase db) {
        super(db);
    }

    public void fill(Library obj) {
        List<List<Object>> result = db.query("SELECT libraryId, name, version FROM Library");
        if (result.size() > 0) {
            List<Object> row = result.get(0);

            int id = (Integer) row.get(0);
            String name = (String) row.get(1);
            int version = (Integer) row.get(2);

            obj.setId(id);
            obj.setName(name);
            obj.setVersion(version);
        }
    }
}
