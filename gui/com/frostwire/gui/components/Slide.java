package com.frostwire.gui.components;


public class Slide {

    /** Just Open The URL */
    public static final int SLIDE_DOWNLOAD_METHOD_OPEN_URL = -1;

    /** Download using the torrent URL */
    public static final int SLIDE_DOWNLOAD_METHOD_TORRENT = 0;

    /** Download via HTTP */
    public static final int  SLIDE_DOWNLOAD_METHOD_HTTP = 1;

    public Slide() {
        
    }
    
	public Slide(String imgSrc, String clickURL, long durationMilli) {
		
	}
	
	public Slide(String imgSrc, 
	             String clickURL, 
	             long durationInMilliseconds, 
	             String torrentURL, 
	             String lang, 
	             String OS, 
	             String theTitle, 
	             long theSize, 
	             int downloadMethod, 
	             String md5hash,
	             String saveAs,
	             boolean executeWhenDone,
	             String executionParameters,
	             boolean unzipWhenDone, 
	             boolean unzipAndDeleteWhenDone,
	             String excludeTheseVersions) {
		imageSrc = imgSrc;
		url = clickURL;
		duration = durationInMilliseconds;
		torrent = torrentURL;
	    language = lang;
	    os = OS;
	    title = theTitle;
	    size = theSize;
		method = downloadMethod;
		md5 = md5hash;
		saveFileAs = saveAs;
		execute = executeWhenDone;
		executeParameters = executionParameters;
		unzip = unzipWhenDone;
		unzipAndDelete = unzipAndDeleteWhenDone;
		excludedVersions = excludeTheseVersions;
	}
		
	/**
	 * http address where to go if user clicks on this slide
	 */
	public String url;
	
	/**
	 * url of torrent file that should be opened if user clicks on this slide
	 */
	public String torrent;
	
	/**
	 * url of image that will be displayed on this slide
	 */
	public String imageSrc;
	
	/**
	 * length of time this slide will be shown
	 */
	public long duration;
	
	/**
	 * language (optional filter) = Can be given in the forms of:
	 * *
	 * en
	 * en_US
	 * 
	 */
	public String language;
	
	/**
	 * os (optional filter) = Can be given in the forms of commq separated:
	 * windows
	 * mac
	 * linux
	 */
	public String os;
	
	/**
	 * The Download title.
	 */
	public String title;
	
	/**
	 * Download size in bytes.
	 */
	public long size;
	
	/**
	 * decide what to do with this Slide onClick.
	 */
	public int method;

	/** Optional MD5 hash */
    public String md5;

    /** If != null, rename file to this file name. */
    public String saveFileAs;

    /** If true, try executing the finished file download. */
    public boolean execute;

    /** If != null && execute, pass these parameters to the finished downloaded file. */
    public String executeParameters;

    /** Unzip the file when finished downloading */
    public boolean unzip;

    /** Delete the .zip file you downloaded after it's unzipped */
    public boolean unzipAndDelete;

    /** Comma separated list of versions that should not use this */
    public String excludedVersions;
}
