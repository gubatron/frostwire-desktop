package com.frostwire.alexandria.db;

import java.util.ArrayList;
import java.util.List;

import com.frostwire.alexandria.Playlist;
import com.frostwire.alexandria.PlaylistItem;

public class PlaylistDB extends ObjectDB<Playlist> {

    public PlaylistDB(LibraryDatabase db) {
        super(db);
    }

    public void fill(Playlist obj) {
        List<List<Object>> result = db.query("SELECT playlistId, name, description, FROM Playlists WHERE playlistId = " + obj.getId());
        if (result.size() > 0) {
            List<Object> row = result.get(0);
            fill(row, obj);
        }
    }

    public void fill(List<Object> row, Playlist obj) {
        int id = (Integer) row.get(0);
        String name = (String) row.get(1);
        String description = (String) row.get(2);

        obj.setId(id);
        obj.setName(name);
        obj.setDescription(description);
    }

    public void save(Playlist obj) {
        if (obj.getId() == LibraryDatabase.OBJECT_INVALID_ID) {
            return;
        }

        if (obj.getId() == LibraryDatabase.OBJECT_NOT_SAVED_ID) {
            int id = db.insert("INSERT INTO Playlists (name, description) VALUES ('" + obj.getName() + "', '" + obj.getDescription() + "')");
            obj.setId(id);
        } else {
            db.update("DELETE FROM PlaylistsPlaylistItems WHERE playlistId = " + obj.getId());
            db.update("UPDATE Playlists SET name = '" + obj.getName() + "', description = '" + obj.getDescription() + "' WHERE playlistId = " + obj.getId());
        }

        for (PlaylistItem item : obj.getItems()) {
            item.save();
        }
    }

    public void delete(Playlist obj) {
        if (obj.getName().equals(LibraryDatabase.DEFAULT_PLAYLIST_NAME)) {
            return;
        }
        db.update("DELETE FROM PlaylistsPlaylistItems WHERE playlistId = " + obj.getId());
        db.update("DELETE FROM Playlists WHERE playlistId = " + obj.getId());
    }

    public List<PlaylistItem> getLibraryItems(Playlist playlist) {
        String query;

        if (playlist.getName().equals(LibraryDatabase.DEFAULT_PLAYLIST_NAME)) {
            query = "SELECT playlistItemId, filePath, fileName, fileSize, fileExtension, trackTitle, duration, artistName, albumName, coverArtPath "
                    + "FROM PlaylistItems";
        } else {
            query = "SELECT playlistItemId, filePath, fileName, fileSize, fileExtension, trackTitle, duration, artistName, albumName, coverArtPath "
                    + "FROM PlaylistItems INNER JOIN PlaylistsPlaylistItems ON PlaylistItems.playlistItemId = PlaylistsPlaylistItems.playlistItemId "
                    + "WHERE playlistId = " + playlist.getId();
        }

        List<List<Object>> result = db.query(query);

        List<PlaylistItem> items = new ArrayList<PlaylistItem>(result.size());

        for (List<Object> row : result) {
            PlaylistItem item = new PlaylistItem(playlist);
            item.getDB().fill(row, item);
            items.add(item);
        }

        return items;
    }
}
