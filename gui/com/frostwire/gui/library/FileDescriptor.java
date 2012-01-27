package com.frostwire.gui.library;


public class FileDescriptor implements Cloneable {

    public int id;
    public byte fileType; // As described in Constants.
    public String filePath;
    public long fileSize;
    public String mime; //MIME Type
    public boolean shared;

    public String title;
    // only if audio/video media
    public String artist;
    public String album;
    public String year;

    /**
     * Empty constructor.
     */
    public FileDescriptor() {
    }

    public FileDescriptor(int id, String artist, String title, String album, String year, String path, byte fileType, String mime, long fileSize, boolean isShared) {
        this.id = id;
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.year = year;
        this.filePath = path;
        this.fileType = fileType;
        this.mime = mime;
        this.fileSize = fileSize;
        this.shared = isShared;
    }

    @Override
    public String toString() {
        return "FD(id:" + id + ", ft:" + fileType + ", t:" + title + ", p:" + filePath + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof FileDescriptor)) {
            return false;
        }

        FileDescriptor fd = (FileDescriptor) o;

        return this.id == fd.id && this.fileType == fd.fileType;
    }
    
    @Override
    public int hashCode() {
        return this.id * 1000 + this.fileType;
    }

    @Override
    public FileDescriptor clone() {
        return new FileDescriptor(id, artist, title, album, year, filePath, fileType, mime, fileSize, shared);
    }
}
