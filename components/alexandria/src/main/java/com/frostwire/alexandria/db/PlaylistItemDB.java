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
        if (obj.getId() == LibraryDatabase.OBJECT_INVALID_ID) {
            return;
        }

        if (obj.getId() == LibraryDatabase.OBJECT_NOT_SAVED_ID) {
            int id = db
                    .insert("INSERT INTO PlaylistItems (filePath, fileName, fileSize, fileExtension, trackTitle, duration, artistName, albumName, coverArtPath, bitrate, comment, genre, track, year) "
                            + " VALUES ('"
                            + obj.getFilePath()
                            + "', '"
                            + obj.getFileName()
                            + "', "
                            + obj.getFileSize()
                            + ", '"
                            + obj.getFileExtension()
                            + "', '"
                            + obj.getTrackTitle()
                            + "', "
                            + obj.getTrackDurationInSecs()
                            + ", '"
                            + obj.getArtistName()
                            + "', '"
                            + obj.getAlbumName()
                            + "', '"
                            + obj.getCoverArtPath()
                            + "', '"
                            + obj.getBitrate()
                            + "', '"
                            + obj.getComment()
                            + "', '"
                            + obj.getGenre()
                            + "', '"
                            + obj.getTrack()
                            + "', '"
                            + obj.getYear() + "')");
            obj.setId(id);
            db.update("INSERT INTO PlaylistsPlaylistItems (playlistId, playlistItemId) VALUES (" + obj.getPlaylist().getId() + ", " + obj.getId() + ")");
        } else {
            db.update("UPDATE PlaylistItems SET filePath = '" + obj.getFilePath() + "', fileName = '" + obj.getFileName() + "', fileSize = "
                    + obj.getFileSize() + ", fileExtension = '" + obj.getFileExtension() + "', trackTitle = '" + obj.getTrackTitle() + "', duration = "
                    + obj.getTrackDurationInSecs() + ", artistName = '" + obj.getArtistName() + "', albumName = '" + obj.getAlbumName() + "', coverArtPath = '"
                    + obj.getCoverArtPath() + "', bitrate = '" + obj.getBitrate() + "', comment = '" + obj.getComment() + "', genre = '" + obj.getGenre()
                    + "', track = '" + obj.getTrack() + "', year = '" + obj.getYear()
                    + "' WHERE playlistItemId = " + obj.getId());
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
}
