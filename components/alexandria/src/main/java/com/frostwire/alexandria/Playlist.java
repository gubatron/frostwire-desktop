package com.frostwire.alexandria;

import java.util.LinkedList;
import java.util.List;

import com.frostwire.alexandria.db.LibraryDatabase;
import com.frostwire.alexandria.db.PlaylistDB;

public class Playlist extends Entity<PlaylistDB> {

    private final Library _library;

    private int _id;
    private String _name;

    private List<PlaylistItem> _items;

    public Playlist(Library library) {
        super(new PlaylistDB(library.db.getDatabase()));
        _library = library;
        _id = LibraryDatabase.OBJECT_INVALID_ID;
        _items = new LinkedList<PlaylistItem>();
    }

    public Playlist(Library library, int id, String name) {
        super(new PlaylistDB(library.db.getDatabase()));
        _library = library;
        _id = id;
        _name = name;
        _items = new LinkedList<PlaylistItem>();
    }

    public Library getLibrary() {
        return _library;
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

    public List<PlaylistItem> getItems() {
        return _items;
    }

    public void save() {
        db.save(this);
    }

    public void delete() {
        db.delete(this);
    }

    public void refresh() {
        _items.clear();
        _items.addAll(db.getLibraryItems(this));
    }

    public PlaylistItem newItem(String filePath, String fileName, long fileSize, String fileExtension, String trackTitle, long time, String artistName,
            String albumName, String coverArtPath) {
        return new PlaylistItem(this, LibraryDatabase.OBJECT_NOT_SAVED_ID, filePath, fileName, fileSize, fileExtension, trackTitle, time, artistName,
                albumName, coverArtPath);
    }
}
