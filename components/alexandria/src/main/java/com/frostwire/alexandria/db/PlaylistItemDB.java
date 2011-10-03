package com.frostwire.alexandria.db;

import java.util.List;

import com.frostwire.alexandria.PlaylistItem;

public class PlaylistItemDB extends ObjectDB<PlaylistItem> {

    public PlaylistItemDB(LibraryDatabase db) {
        super(db);
    }

    public void fill(PlaylistItem obj) {
        List<List<Object>> result = db
                .query("SELECT playlistItemId, filePath, fileName, fileSize, fileExtension, trackTitle, trackDurationInSecs, trackArtist, trackAlbum, coverArtPath, trackBitrate, trackComment, trackGenre, trackNumber, trackYear, starred "
                        + "FROM PlaylistItems WHERE playlistItemId = ?", obj.getId());
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
        String trackArtist = (String) row.get(7);
        String trackAlbum = (String) row.get(8);
        String coverArtPath = (String) row.get(9);
        String trackBitrate = (String) row.get(10);
        String trackComment = (String) row.get(11);
        String trackGenre = (String) row.get(12);
        String trackNumber = (String) row.get(13);
        String trackYear = (String) row.get(14);
        boolean starred = (Boolean) row.get(15);

        obj.setId(id);
        obj.setFilePath(filePath);
        obj.setFileName(fileName);
        obj.setFileSize(fileSize);
        obj.setFileExtension(fileExtension);
        obj.setTrackTitle(trackTitle);
        obj.setTrackDurationInSecs(trackDurationInSecs);
        obj.setTrackArtist(trackArtist);
        obj.setTrackAlbum(trackAlbum);
        obj.setCoverArtPath(coverArtPath);
        obj.setTrackBitrate(trackBitrate);
        obj.setTrackComment(trackComment);
        obj.setTrackGenre(trackGenre);
        obj.setTrackNumber(trackNumber);
        obj.setTrackYear(trackYear);
        obj.setStarred(starred);
    }

    public void save(PlaylistItem obj) {
        if (obj.getId() == LibraryDatabase.OBJECT_INVALID_ID || obj.getPlaylist() == null) {
            return;
        }

        if (obj.getId() == LibraryDatabase.OBJECT_NOT_SAVED_ID) {
            obj.setStarred(isStarred(obj) || obj.isStarred());
            Object[] sqlAndValues = createPlaylistItemInsert(obj);
            int id = db.insert((String) sqlAndValues[0], (Object[]) sqlAndValues[1]);
            obj.setId(id);
            sqlAndValues = updateStarred(obj);
            db.update((String) sqlAndValues[0], (Object[]) sqlAndValues[1]);
        } else {
            obj.setStarred(obj.getPlaylist().getId() == LibraryDatabase.STARRED_PLAYLIST_ID || obj.isStarred());
            Object[] sqlAndValues = createPlaylistItemUpdate(obj);
            db.update((String) sqlAndValues[0], (Object[]) sqlAndValues[1]);
            sqlAndValues = updateStarred(obj);
            db.update((String) sqlAndValues[0], (Object[]) sqlAndValues[1]);
        }
    }

    public void delete(PlaylistItem obj) {
        db.update("DELETE FROM PlaylistItems WHERE playlistItemId = ?", obj.getId());
    }
    
    private Object[] createPlaylistItemInsert(PlaylistItem item) {
        String sql = "INSERT INTO PlaylistItems (playlistId, filePath, fileName, fileSize, fileExtension, trackTitle, trackDurationInSecs, trackArtist, trackAlbum, coverArtPath, trackBitrate, trackComment, trackGenre, trackNumber, trackYear, starred) "
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Object[] values = new Object[] { item.getPlaylist().getId(), item.getFilePath(), item.getFileName(), item.getFileSize(), item.getFileExtension(), item.getTrackTitle(),
                item.getTrackDurationInSecs(), item.getTrackArtist(), item.getTrackAlbum(), item.getCoverArtPath(), item.getTrackBitrate(), item.getTrackComment(),
                item.getTrackGenre(), item.getTrackNumber(), item.getTrackYear(), item.isStarred() };

        return new Object[] { sql, values };
    }

    private Object[] createPlaylistItemUpdate(PlaylistItem item) {
        String sql = "UPDATE PlaylistItems SET filePath = ?, fileName = ?, fileSize = ?, fileExtension = ?, trackTitle = ?, trackDurationInSecs = ?, trackArtist = ?, trackAlbum = ?, coverArtPath = ?, trackBitrate = ?, trackComment = ?, trackGenre = ?, trackNumber = ?, trackYear = ?, starred = ? WHERE playlistItemId = ?";

        Object[] values = new Object[] { item.getFilePath(), item.getFileName(), item.getFileSize(), item.getFileExtension(), item.getTrackTitle(),
                item.getTrackDurationInSecs(), item.getTrackArtist(), item.getTrackAlbum(), item.getCoverArtPath(), item.getTrackBitrate(), item.getTrackComment(),
                item.getTrackGenre(), item.getTrackNumber(), item.getTrackYear(), item.isStarred(), item.getId() };

        return new Object[] { sql, values };
    }

    private Object[] updateStarred(PlaylistItem item) {
        String sql = "UPDATE PlaylistItems SET starred = ? WHERE filePath = ?";

        Object[] values = new Object[] { item.isStarred(), item.getFilePath() };

        return new Object[] { sql, values };
    }
    
    private boolean isStarred(PlaylistItem item) {
        List<List<Object>> result = db
                .query("SELECT starred FROM PlaylistItems WHERE filePath = ? LIMIT 1", item.getFilePath());
        if (result.size() > 0) {
            return (Boolean) result.get(0).get(0);
        }
        
        return false;
    }
}
