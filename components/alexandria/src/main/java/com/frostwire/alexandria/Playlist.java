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
    
    private boolean deleted;

    private List<PlaylistItem> _items;

    public Playlist(Library library) {
        super(new PlaylistDB(library.db.getDatabase()));
        _library = library;
        _id = LibraryDatabase.OBJECT_INVALID_ID;
        _items = new LinkedList<PlaylistItem>();
        this.deleted = false;
    }

    public Playlist(Library library, int id, String name, String description) {
        super(new PlaylistDB(library.db.getDatabase()));
        _library = library;
        _id = id;
        _name = name;
        _description = description;
        _items = new LinkedList<PlaylistItem>();
        this.deleted = false;
    }
    
    public boolean isStarred() {
    	return _id == LibraryDatabase.STARRED_PLAYLIST_ID;
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
    
    public boolean isDeleted() {
        return deleted;
    }

    public synchronized void save() {
        if (db != null) {
            db.save(this);
        }
    }

    public synchronized void delete() {
        if (db != null) {
            db.delete(this);
            deleted = true;
        }
    }

    public synchronized void refresh() {
        if (db != null) {
            _items.clear();
            _items.addAll(db.getLibraryItems(this));
        }
    }

    public PlaylistItem newItem(String filePath, String fileName, long fileSize, String fileExtension, String trackTitle, float trackDurationInSecs, String trackArtist,
            String trackAlbum, String coverArtPath, String trackBitrate, String trackComment, String trackGenre, String trackNumber, String trackYear, boolean starred) {
        return new PlaylistItem(this, LibraryDatabase.OBJECT_NOT_SAVED_ID, filePath, fileName, fileSize, fileExtension, trackTitle, trackDurationInSecs, trackArtist,
                trackAlbum, coverArtPath, trackBitrate, trackComment, trackGenre, trackNumber, trackYear, starred);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Playlist)) {
            return false;
        }
        
    	Playlist other = (Playlist) obj;
    	return other.getId() == getId();
    }
}
