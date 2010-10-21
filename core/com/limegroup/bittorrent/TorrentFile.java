package com.limegroup.bittorrent;

import java.io.File;

/*
 * simple class holding the length and the path of a file
 */
public class TorrentFile extends File {
	private static final long serialVersionUID = 4051327846800962608L;

	private final long length;

	// FTA: **still pending to add another attribute to this class**
	/** 
	 * The indices of the first and last blocks 
	 * of the torrent this file occupies
	 */
	private int begin, end; // for pieces
        private long startByte, endByte; //for torrent file

	TorrentFile(long length, String path) {
		super(path);
		this.length = length;
		begin = -1; //these need to be initialized.
		end = -1;
		startByte = -1;
		endByte = -1;
	}
	
	public long length() {
		return length;
	}
	
	public void setBegin(int begin) {
		this.begin = begin;
	}
	
	public int getBegin() {
		return begin;
	}
	
	public void setEnd(int end) {
		this.end = end;
	}
	
	public int getEnd() {
		return end;
	}

	// FTA: The following procedures are used to handle the bytes inside the torrent file.
        // Once we know where a file starts or ends we should be able to split this file (torrent)
        // into files (files inside the torrent).

	public void setStartByte(long startByte) {
		this.startByte = startByte;
	}

	public void setEndByte(long endByte) {
		this.endByte = endByte;
	}


	public long getStartByte() {
        	return startByte;
	}

	public long getEndByte() {
		return endByte;
	}




}
