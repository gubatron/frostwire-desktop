package com.frostwire.gui.components;


public class Slide {

    /** Just Open The URL */
    public static final int SLIDE_DOWNLOAD_METHOD_OPEN_URL = -1;

    /** Download using the torrent URL */
    public static final int SLIDE_DOWNLOAD_METHOD_TORRENT = 0;

    /** Download via HTTP */
    public static final int  SLIDE_DOWNLOAD_METHOD_HTTP = 1;

	/**
	 * 
	 * @param imgSrc - slide overlay image url
	 * @param clickURL - url where to take user on click (optional)
	 * @param durationInMilliseconds - for how long to show the overlay before autoswitching
	 * @param torrentURL - .torrent file (optional)
	 * @param httpDownloadURL - an http url where to download the file from (check downloadMethod on how to procede)
	 * @param lang - language code in case you want to filter slides by language
	 * @param OS - comma separated os names (windows,mac,linux,android)
	 * @param theTitle - the title of this download (useful for download manager and human presentation)
	 * @param theSize - size in bytes of this download
	 * @param downloadMethod - what to do with the slide.
	 * @param md5hash - optional, string with md5 hash of the finished http download
	 * @param saveAs - optional, name of the file if downloaded via http
	 * @param executeWhenDone - should the finished http download be executed
	 * @param executionParameters - parameters to pass to executable download
	 * @param unzipWhenDone - should the http download be unzipped
	 * @param unzipAndDeleteWhenDone - should the http download be unzipped and should we clean up the .zip
	 * @param excludeTheseVersions - comma separated versions that are not supposed to see this slide.
     * @param audioPreviewURL - HTTP URL of audio file so user can preview before download.
     * @param videoPreviewURL - HTTP URL of video file (youtube maybe) so user can preview promo.
	 * @param facebookURL - optional, related Facebook page url
	 * @param twitterURL - optional, related Twitter page url
	 * @param gPlusURL - optional, related Google Plus page url
	 */
	public Slide(String imgSrc, 
	             String clickURL, 
	             long durationInMilliseconds, 
	             String torrentURL, 
	             String httpDownloadUrl,
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
	             String excludeTheseVersions,
	             String audioPreviewURL,
	             String videoPreviewURL,
	             String facebookURL,
	             String twitterURL,
	             String gPlusURL) {
		imageSrc = imgSrc;
		url = clickURL;
		duration = durationInMilliseconds;
		torrent = torrentURL;
		httpDownloadURL = httpDownloadUrl;
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
		audioURL = audioPreviewURL;
		videoURL = videoPreviewURL;
		facebook = facebookURL;
		twitter = twitterURL;
		gplus = gPlusURL;
	}
		
	public Slide(String imageSrc, String clickURL, int durationInMilli) {
        this(imageSrc,clickURL,durationInMilli,null,null,null,null,null,0,SLIDE_DOWNLOAD_METHOD_OPEN_URL,null,null,false,null,false,false,null,null,null,null,null,null);
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
	 * 
	 */
	public String httpDownloadURL;
	
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
	 * android
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
    
    /** audio file url so user can play preview/promotional audio for promo. */
    public String audioURL;

    /** video file url so frostwire player can be opened, could be a youtube url, player
     * should default to high quality playback */
    public String videoURL;

    /** Facebook page associated with slide */
    public String facebook;

    /** Twitter page associated with slide */
    public String twitter;

    /** Google Plus page associated with slide */
    public String gplus;

}
