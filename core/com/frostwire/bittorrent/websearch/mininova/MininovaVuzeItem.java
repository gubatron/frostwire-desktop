package com.frostwire.bittorrent.websearch.mininova;

/**
 *               { "title": "2012 - ZYMBOLIZER (remake of britney spears song WOMANIZER [single/2009])", 
 *               "date": "Wed, 18 Nov 2009 10:07:42 +0100", 
 *               "peers": 0, 
 *               "seeds": 10, 
 *               "superseeds": 3, 
 *               "category": "Music", 
 *               "cdp": "http://www.mininova.org/tor/3162234", 
 *               "comments": 1, 
 *               "size": 4828003, 
 *               "votes": 1, 
 *               "download":"http://www.mininova.org/get/3162234" #notice how there's no comma
 *               "hash": "9c8671d69e53125b4777c9473119a088e857c6dd" }
 * @author gubatron
 *
 */
public class MininovaVuzeItem {
	public String title;
	public String date;
	public int peers;
	public int seeds;
	public int superseeds;
	public String cdp; //Torrent Detail URL
	public long size; //file size
	public String download; //.torrent download url
	public String hash;
}
