package com.frostwire.bittorrent.websearch.clearbits;

import java.util.List;

/**
 * 
 * ClearBits JSON Responses look like this:
 * [{"torrent": 
    {"leechers": 0, 
    "created_at": "2010-07-15T16:02:42Z", 
    "title": "Bear and Lampshade - Siddhartha", 
    "seeds": 3, 
    "hashstr": "faabc8daf2e33e9ed6058b8acc1819c9bf35177a", 
    "mb_size": 65, 
    "license_url": "http://creativecommons.org/licenses/by-nc-nd/3.0/", 
    "torrent_url": "http://www.clearbits.net/get/1235-bear-and-lampshade---siddhartha.torrent", 
    "location": "http://www.clearbits.net/torrents/1235-bear-and-lampshade---siddhartha"}
    }, 
    
 {"torrent": 
    {"leechers": 0, 
    "created_at": "2010-06-23T06:34:19Z", 
    "title": "Yokandesh - Viva Como Queira", 
    "seeds": 5, 
    "hashstr": "d7fc71b8554befcebd125176826fa1f41bead46a", 
    "mb_size": 136, 
    "license_url": "http://creativecommons.org/licenses/by-nc-nd/3.0/", 
    "torrent_url": "http://www.clearbits.net/get/1196-yokandesh---viva-como-queira.torrent", 
    "location": "http://www.clearbits.net/torrents/1196-yokandesh---viva-como-queira"}}, ...
 * 
 * @author gubatron
 *
 */
public class ClearBitsResponse {
	public List<ClearBitsItem> results;
}
