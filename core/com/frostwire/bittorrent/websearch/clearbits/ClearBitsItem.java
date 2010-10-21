package com.frostwire.bittorrent.websearch.clearbits;

public class ClearBitsItem {
	/**
	 {"leechers": 0, 
    "created_at": "2010-07-15T16:02:42Z", 
    "title": "Bear and Lampshade - Siddhartha", 
    "seeds": 3, 
    "hashstr": "faabc8daf2e33e9ed6058b8acc1819c9bf35177a", 
    "mb_size": 65, 
    "license_url": "http://creativecommons.org/licenses/by-nc-nd/3.0/", 
    "torrent_url": "http://www.clearbits.net/get/1235-bear-and-lampshade---siddhartha.torrent", 
    "location": "http://www.clearbits.net/torrents/1235-bear-and-lampshade---siddhartha"}
    }
	 */
	public int leechers;
	public String created_at;
	public String title;
	public int seeds;
	public String hashstr;
	public int mb_size;
	public String license_url;
	public String torrent_url;
	public String location;
}
