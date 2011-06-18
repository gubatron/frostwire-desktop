package com.frostwire.bittorrent.websearch.mininova;

/**
 *               { "title": "...", 
 *               "date": "Wed, 18 Nov 2009 10:07:42 +0100", 
 *               "peers": 0, 
 *               "seeds": 10, 
 *               "superseeds": 3, 
 *               "category": "Music", 
 *               "cdp": "...", 
 *               "comments": 1, 
 *               "size": 4828003, 
 *               "votes": 1, 
 *               "download":"..." #notice how there's no comma
 *               "hash": "..." }
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
