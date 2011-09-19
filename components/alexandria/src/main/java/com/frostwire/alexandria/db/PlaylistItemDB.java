package com.frostwire.alexandria.db;

import java.util.List;

import com.frostwire.alexandria.PlaylistItem;

public class PlaylistItemDB extends ObjectDB<PlaylistItem> {

    public PlaylistItemDB(LibraryDatabase db) {
        super(db);
    }

    public void fill(PlaylistItem obj) {
        List<List<Object>> result = db
                .query("SELECT playlistItemId, filePath, fileName, fileSize, fileExtension, trackTitle, duration, artistName, albumName, coverArtPath, bitrate, comment, genre, track, year "
                        + "FROM PlaylisItems WHERE playlistItemId = " + obj.getId());
        if (result.size() > 0) {
            List<Object> row = result.get(0);
            fill(row, obj);
        }
    }

    public void fill(List<Object> row, PlaylistItem obj) {
        int id = (Integer) row.get(0);
        String filePath = (String) row.get(1);
        String fileName = (String) row.get(2);
        long fileSize = (Long) row.get(3);
        String fileExtension = (String) row.get(4);
        String trackTitle = (String) row.get(5);
        float trackDurationInSecs = (Float) row.get(6);
        String artistName = (String) row.get(7);
        String albumName = (String) row.get(8);
        String coverArtPath = (String) row.get(9);
        String bitrate = (String) row.get(10);
        String comment = (String) row.get(11);
        String genre = (String) row.get(12);
        String track = (String) row.get(13);
        String year = (String) row.get(14);

        obj.setId(id);
        obj.setFilePath(filePath);
        obj.setFileName(fileName);
        obj.setFileSize(fileSize);
        obj.setFileExtension(fileExtension);
        obj.setTrackTitle(trackTitle);
        obj.setTrackDurationInSecs(trackDurationInSecs);
        obj.setArtistName(artistName);
        obj.setAlbumName(albumName);
        obj.setCoverArtPath(coverArtPath);
        obj.setBitrate(bitrate);
        obj.setComment(comment);
        obj.setGenre(genre);
        obj.setTrack(track);
        obj.setYear(year);
    }

    public void save(PlaylistItem obj) {
        if (obj.getId() == LibraryDatabase.OBJECT_INVALID_ID || obj.getPlaylist() == null) {
            return;
        }

        if (obj.getId() == LibraryDatabase.OBJECT_NOT_SAVED_ID) {
            Object[] sqlAndValues = createPlaylistItemInsert(obj);
            int id = db.insert((String) sqlAndValues[0], (Object[]) sqlAndValues[1]);
            obj.setId(id);
            db.update("INSERT INTO PlaylistsPlaylistItems (playlistId, playlistItemId) VALUES (?, ?)", obj.getPlaylist().getId(), obj.getId());
        } else {
            Object[] sqlAndValues = createPlaylistItemUpdate(obj);
            db.update((String) sqlAndValues[0], (Object[]) sqlAndValues[1]);
        }
    }

    public void delete(PlaylistItem obj) {
        db.update("DELETE FROM PlaylistsPlaylistItems WHERE playlistId = " + obj.getPlaylist().getId() + " AND playlistItemId = " + obj.getId());
        List<List<Object>> result = db.query("SELECT * FROM PlaylistsPlaylistItems WHERE playlistItemId = " + obj.getId());
        if (result.size() == 0) {
            db.update("DELETE FROM PlaylistItems WHERE playlistItemId = " + obj.getId());
        }
    }

    public void deleteFromAll(PlaylistItem item) {
        db.update("DELETE FROM PlaylistsPlaylistItems WHERE playlistItemId = " + item.getId());
        db.update("DELETE FROM PlaylistItems WHERE playlistItemId = " + item.getId());
    }

    private Object[] createPlaylistItemInsert(PlaylistItem item) {
        String sql = "INSERT INTO PlaylistItems (filePath, fileName, fileSize, fileExtension, trackTitle, duration, artistName, albumName, coverArtPath, bitrate, comment, genre, track, year) "
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Object[] values = new Object[] { item.getFilePath(), item.getFileName(), item.getFileSize(), item.getFileExtension(), item.getTrackTitle(),
                item.getTrackDurationInSecs(), item.getArtistName(), item.getAlbumName(), item.getCoverArtPath(), item.getBitrate(), item.getComment(),
                item.getGenre(), item.getTrack(), item.getYear() };

        return new Object[] { sql, values };
    }

    private Object[] createPlaylistItemUpdate(PlaylistItem item) {
        String sql = "UPDATE PlaylistItems SET filePath = ?, fileName = ?, fileSize = ?, fileExtension = ?, trackTitle = ?, duration = ?, artistName = ?, albumName = ?, coverArtPath = ?, bitrate = ?, comment = ?, genre = ?, track = ?, year = ? WHERE playlistItemId = ?";

        Object[] values = new Object[] { item.getFilePath(), item.getFileName(), item.getFileSize(), item.getFileExtension(), item.getTrackTitle(),
                item.getTrackDurationInSecs(), item.getArtistName(), item.getAlbumName(), item.getCoverArtPath(), item.getBitrate(), item.getComment(),
                item.getGenre(), item.getTrack(), item.getYear(), item.getId() };

        return new Object[] { sql, values };
    }
}
