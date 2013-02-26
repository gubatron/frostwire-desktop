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
        List<List<Object>> result = db.query("SELECT playlistId, name, description FROM Playlists WHERE playlistId = ?", obj.getId());
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
        obj.refresh();
    }

    public void save(Playlist obj) {
        if (obj.getId() == LibraryDatabase.OBJECT_INVALID_ID) {
            return;
        }

        if (obj.getId() == LibraryDatabase.OBJECT_NOT_SAVED_ID) {
            int id = db.insert("INSERT INTO Playlists (name, description) VALUES (LEFT(?, 500), LEFT(?, 10000))", obj.getName(), obj.getDescription());
            obj.setId(id);
        } else {
            db.update("DELETE FROM PlaylistItems WHERE playlistId = ?", obj.getId());
            Object[] statementObjects = createPlaylistUpdateStatement(obj);
            db.update((String) statementObjects[0], (Object[]) statementObjects[1]);
        }
        
        List<PlaylistItem> items = new ArrayList<PlaylistItem>(obj.getItems());

        for (PlaylistItem item : items) {
            item.setId(LibraryDatabase.OBJECT_NOT_SAVED_ID);
            item.save();
        }
    }

    public void delete(Playlist obj) {
        db.update("DELETE FROM PlaylistItems WHERE playlistId = ?", obj.getId());
        db.update("DELETE FROM Playlists WHERE playlistId = ?", obj.getId());
    }

    public List<PlaylistItem> getPlaylistItems(Playlist playlist) {
        String query = "SELECT playlistItemId, filePath, fileName, fileSize, fileExtension, trackTitle, trackDurationInSecs, trackArtist, trackAlbum, coverArtPath, trackBitrate, trackComment, trackGenre, trackNumber, trackYear, starred, sortIndex "
                + "FROM PlaylistItems WHERE playlistId = ?";

        List<List<Object>> result = db.query(query, playlist.getId());

        List<PlaylistItem> items = new ArrayList<PlaylistItem>(result.size());

        for (List<Object> row : result) {
            PlaylistItem item = new PlaylistItem(playlist);
            item.getDB().fill(row, item);
            items.add(item);
        }

        return items;
    }

    private Object[] createPlaylistUpdateStatement(Playlist obj) {
        String sql = "UPDATE Playlists SET name = LEFT(?, 500), description = LEFT(?, 10000) WHERE playlistId = ?";
        Object[] values = new Object[] { obj.getName(), obj.getDescription(), obj.getId() };
        return new Object[] { sql, values };
    }

}
