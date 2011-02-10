package com.frostwire.gnutella.gui;

public class Slide {
    
	public Slide() {
		
	}
	
	public Slide(String imgSrc, String clickURL, long durationMilli) {
		this(imgSrc, clickURL, durationMilli, null);
	}
	
	public Slide(String imgSrc, String clickURL, long durationInMilliseconds, String torrentURL) {
		imageSrc = imgSrc;
		url = clickURL;
		duration = durationInMilliseconds;
		torrent = torrentURL;
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
}
