package com.frostwire.gnutella.gui.android;

import java.io.Serializable;

public class FileDescriptor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -594450692434313002L;
	
	public int id;
	public byte fileType;
	public String artist;
	public String title;
	public String album;
	public String year;
	public String fileName;
	public long fileSize;
	
	public Device device;
}
