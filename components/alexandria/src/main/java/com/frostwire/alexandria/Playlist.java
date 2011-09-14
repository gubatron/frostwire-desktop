package com.frostwire.alexandria;

import java.util.LinkedList;
import java.util.List;

import com.frostwire.alexandria.db.LibraryDatabase;
import com.frostwire.alexandria.db.PlaylistDB;

public class Playlist extends Entity<PlaylistDB> {

    private final Library _library;

    private int _id;
    private String _name;
    private String _description;

    private List<PlaylistItem> _items;

    public Playlist(Library library) {
        super(new PlaylistDB(library.db.getDatabase()));
        _library = library;
        _id = LibraryDatabase.OBJECT_INVALID_ID;
        _items = new LinkedList<PlaylistItem>();
    }

    public Playlist(Library library, int id, String name, String description) {
        super(new PlaylistDB(library.db.getDatabase()));
        _library = library;
        _id = id;
        _name = name;
        _description = description;
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
    
    public String getDescription() {
        return _description;
    }
    
    public void setDescription(String description) {
        _description = description;
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

    public PlaylistItem newItem(String filePath, String fileName, long fileSize, String fileExtension, String trackTitle, float trackDurationInSecs, String artistName,
            String albumName, String coverArtPath, String bitrate, String comment, String genre, String track, String year) {
        return new PlaylistItem(this, LibraryDatabase.OBJECT_NOT_SAVED_ID, filePath, fileName, fileSize, fileExtension, trackTitle, trackDurationInSecs, artistName,
                albumName, coverArtPath, bitrate, comment, genre, track, year);
    }
    
    @Override
    public boolean equals(Object obj) {
    	Playlist other = (Playlist) obj;
    	return other.getId()==getId();
    }
}
