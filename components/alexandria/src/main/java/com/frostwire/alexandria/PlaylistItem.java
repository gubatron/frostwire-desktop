package com.frostwire.alexandria;

public class PlaylistItem {

    private final Playlist _playlist;

    private int _id;
    private String _filePath;
    private String _fileName;
    private long _fileSize;
    private String _fileExtension;
    private String _trackTitle;
    private long _time;
    private String _artistName;
    private String _albumName;
    private String _coverArtPath;

    public PlaylistItem(Playlist playlist, int id, String filePath, String fileName, long fileSize, String fileExtension, String trackTitle, long time,
            String artistName, String albumName, String coverArtPath) {
        _playlist = playlist;
        _id = id;
        _filePath = filePath;
        _fileName = fileName;
        _fileSize = fileSize;
        _fileExtension = fileExtension;
        _trackTitle = trackTitle;
        _time = time;
        _artistName = artistName;
        _albumName = albumName;
        _coverArtPath = coverArtPath;
    }

    public Playlist getPlaylist() {
        return _playlist;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public String getFilePath() {
        return _filePath;
    }

    public void setFilePath(String filePath) {
        _filePath = filePath;
    }

    public String getFileName() {
        return _fileName;
    }

    public void setFileName(String fileName) {
        _fileName = fileName;
    }

    public long getFileSize() {
        return _fileSize;
    }

    public void setFileSize(long fileSize) {
        _fileSize = fileSize;
    }

    public String getFileExtension() {
        return _fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        _fileExtension = fileExtension;
    }

    public String getTrackTitle() {
        return _trackTitle;
    }

    public void setTrackTitle(String trackTitle) {
        _trackTitle = trackTitle;
    }

    public long getTime() {
        return _time;
    }

    public void setTime(long time) {
        _time = time;
    }

    public String getArtistName() {
        return _artistName;
    }

    public void setArtistName(String artistName) {
        _artistName = artistName;
    }

    public String getAlbumName() {
        return _albumName;
    }

    public void setAlbumName(String albumName) {
        _albumName = albumName;
    }

    public String getCoverArtPath() {
        return _coverArtPath;
    }

    public void setCoverArtPath(String coverArtPath) {
        _coverArtPath = coverArtPath;
    }
    
    public void save() {
        _playlist.save(this);
    }
    
    public void delete() {
        _playlist.delete(this);
    }
}
