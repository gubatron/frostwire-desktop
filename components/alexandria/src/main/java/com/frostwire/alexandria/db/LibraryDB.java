package com.frostwire.alexandria.db;

import java.util.ArrayList;
import java.util.List;

import com.frostwire.alexandria.Library;
import com.frostwire.alexandria.Playlist;

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

    public List<Playlist> getPlaylists(Library library) {
        List<List<Object>> result = db.query("SELECT playlistId, name, description FROM Playlists");

        List<Playlist> playlists = new ArrayList<Playlist>(result.size());

        for (List<Object> row : result) {
            Playlist playlist = new Playlist(library);
            playlist.getDB().fill(row, playlist);
            playlists.add(playlist);
        }

        return playlists;
    }

    public Playlist getPlaylist(Library library, String name) {
        List<List<Object>> result = db.query("SELECT playlistId, name, description FROM Playlists WHERE name = '" + name + "'");
        Playlist playlist = null;
        if (result.size() > 0) {
            List<Object> row = result.get(0);
            playlist = new Playlist(library);
            playlist.getDB().fill(row, playlist);
        }
        return playlist;
    }
}
