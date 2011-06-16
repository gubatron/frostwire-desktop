package com.frostwire.bittorrent.websearch.mininova;

import java.util.List;

/**
 * { "results": [ 
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
 *               "download":"..." ,
 *               "hash": "..." }, 
 */

public class MininovaVuzeResponse {
	public List<MininovaVuzeItem> results;
}
