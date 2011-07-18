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
        List<List<Object>> result = db.query("SELECT playlistId, name, FROM Playlists WHERE playlistId = " + obj.getId());
        if (result.size() > 0) {
            List<Object> row = result.get(0);
            fill(row, obj);
        }
    }

    public void fill(List<Object> row, Playlist obj) {
        int id = (Integer) row.get(0);
        String name = (String) row.get(1);

        obj.setId(id);
        obj.setName(name);
    }

    public void save(Playlist obj) {
        if (obj.getId() == LibraryDatabase.OBJECT_INVALID_ID) {
            return;
        }

        if (obj.getId() == LibraryDatabase.OBJECT_NOT_SAVED_ID) {
            int id = db.insert("INSERT INTO Playlists (name) VALUES ('" + obj.getName() + "')");
            obj.setId(id);
        } else {
            db.update("DELETE FROM PlaylistsPlaylistItems WHERE playlistId = " + obj.getId());
            db.update("UPDATE Playlists SET name = '" + obj.getName() + "' WHERE playlistId = " + obj.getId());
        }

        for (PlaylistItem item : obj.getItems()) {
            item.save();
        }
    }

    public void delete(Playlist obj) {
        db.update("DELETE FROM PlaylistsPlaylistItems WHERE playlistId = " + obj.getId());
        db.update("DELETE FROM Playlists WHERE playlistId = " + obj.getId());
    }

    public List<PlaylistItem> getLibraryItems(Playlist playlist) {
        List<List<Object>> result = db
                .query("SELECT playlistItemId, filePath, fileName, fileSize, fileExtension, trackTitle, time, artistName, albumName, coverArtPath "
                        + "FROM PlaylisItems INNER JOIN PlaylistsPlaylistItems ON PlaylisItems.playlisItemId = PlaylistsPlaylistItems.playlisItemId "
                        + "WHERE playlistId = " + playlist.getId());

        List<PlaylistItem> items = new ArrayList<PlaylistItem>(result.size());

        for (List<Object> row : result) {
            PlaylistItem item = new PlaylistItem(playlist);
            item.getDB().fill(row, item);
            items.add(item);
        }

        return items;
    }
}
