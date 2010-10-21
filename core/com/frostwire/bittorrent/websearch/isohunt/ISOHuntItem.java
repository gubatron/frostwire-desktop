package com.frostwire.bittorrent.websearch.isohunt;

public class ISOHuntItem {
	public String title;
	public String link; //isohunt torrent details.
	public String guid;
	public String enclosure_url; //actual .torrent url
	public String length; //size in bytes
	public String type;
	public String tracker; 
	public String tracker_url;
	public String original_link; //html page where torrent was crawled from
	public String size; //human readable size
	public String files; //total files inside torrent
	public String Seeds;
	public String leechers;
	public String pubDate; //In this format Thu, 29 Apr 2010 16:32:44 GMT
}
