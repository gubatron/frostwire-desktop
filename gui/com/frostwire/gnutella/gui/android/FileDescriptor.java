package com.frostwire.gnutella.gui.android;

import java.io.Serializable;

public class FileDescriptor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -594450692434313002L;
	
	public FileDescriptor() {
	}
	
	public FileDescriptor(int id, int fileType, String artist, String title, String album, String year, String fileName, long fileSize) {
	    this.id = id;
	    this.fileType = (byte)fileType;
	    this.artist = artist;
	    this.title = title;
	    this.album = album;
	    this.year = year;
	    this.fileName = fileName;
	    this.fileSize = fileSize;
	}
	
	public int id;
	public byte fileType;
	public String artist;
	public String title;
	public String album;
	public String year;
	public String fileName;
	public long fileSize;
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null || !(obj instanceof FileDescriptor)) {
	        return false;
	    }
	    
	    FileDescriptor fileDescriptor = (FileDescriptor) obj;
	    
	    return this.id == fileDescriptor.id && this.fileType == fileDescriptor.fileType;
	}
}
