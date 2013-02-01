package com.frostwire.gui.components;


public class Slide {

    /** Just Open The URL */
    public static final int SLIDE_DOWNLOAD_METHOD_OPEN_URL = -1;

    /** Download using the torrent URL */
    public static final int SLIDE_DOWNLOAD_METHOD_TORRENT = 0;

    /** Download via HTTP */
    public static final int  SLIDE_DOWNLOAD_METHOD_HTTP = 1;

    /** Download and install pokki with available parameters.*/
    public static final int  SLIDE_DOWNLOAD_METHOD_INSTALL_POKKI = 2;
    
    public Slide() {
        
    }
    
	public Slide(String imgSrc, String clickURL, long durationMilli) {
		this(imgSrc, clickURL, durationMilli, null, null, null, null, -1, SLIDE_DOWNLOAD_METHOD_OPEN_URL);
	}
	
	public Slide(String imgSrc, String clickURL, long durationInMilliseconds, String torrentURL, String lang, String OS, String theTitle, long theSize, int downloadMethod) {
		imageSrc = imgSrc;
		url = clickURL;
		duration = durationInMilliseconds;
		torrent = torrentURL;
	    language = lang;
	    os = OS;
	    title = theTitle;
	    size = theSize;
		method = downloadMethod;
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
	 * os (optional filter) = Can be given in the forms of:
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
	
}
