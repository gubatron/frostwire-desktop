package com.frostwire.bittorrent.websearch;


import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import com.frostwire.HttpFetcher;
import com.frostwire.bittorrent.websearch.clearbits.ClearBitsResponse;
import com.frostwire.bittorrent.websearch.isohunt.ISOHuntResponse;
import com.frostwire.bittorrent.websearch.mininova.MininovaVuzeResponse;
import com.frostwire.json.JsonEngine;


/**
 * Web Based Torrent Search.
 * 
 * With support for:
 * - ISO Hunt Torrent Search (JSON Api).
 * 
 * 
 */

public class WebTorrentSearch {
	
	public static ClearBitsResponse searchClearBits(String keywords) {
		String iha = null;
		try {
			iha = URLEncoder.encode(keywords, "UTF-8");
		} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		HttpFetcher fetcher = null;
		try {
			fetcher = new HttpFetcher(new URI("http://www.clearbits.net/home/search/index.json?query="+iha));
		} catch (URISyntaxException e) {
			
		}
		byte[] jsonBytes = fetcher.fetch();
		
		if (jsonBytes == null)
			return null;
		
		String json = new String(jsonBytes);
		
		//Massage JSON to make it easy to parse.
		
		// Convert from [ {"torrent":{...}, {"torrent":{...}, ...]
		// to { "results" : [{...},{...},{...}] }
		
		//json = "{ \"results\": " + json.replace("{\"torrent\":", "").replace("}}","}") + "}";
		//System.out.println(json);
		
		// Feel the power of reflection
		JsonEngine engine = new JsonEngine();
		ClearBitsResponse response = engine.toObject(json, ClearBitsResponse.class);
		return response;
			
	}
	
	/**
	 * @see com.frostwire.bittorrent.websearch.isohunt.ISOHuntResponse
	 * @param keywords
	 * @return
	 */
	public static ISOHuntResponse searchISOHunt(String keywords) {
		String iha = null;
		try {
			iha = URLEncoder.encode(keywords, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HttpFetcher fetcher = null;
		try {
			fetcher = new HttpFetcher(new URI("http://isohunt.com/js/json.php?ihq="+iha+"&start=1&rows=100&sort=seeds"));
		} catch (URISyntaxException e) {
		}
		byte[] jsonBytes = fetcher.fetch();

		
		if (jsonBytes == null)
			return null;
		
		String json = new String(jsonBytes);
		
		// Feel the power of reflection
		JsonEngine engine = new JsonEngine();
		ISOHuntResponse response = engine.toObject(json, ISOHuntResponse.class);

		return response;
	}
	
	public static MininovaVuzeResponse searchMininovaVuze(String keywords) {
		String iha = null;
		try {
			iha = URLEncoder.encode(keywords, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HttpFetcher fetcher = null;
		try {
			fetcher = new HttpFetcher(new URI("http://www.mininova.org/vuze.php?search="+iha));
		} catch (URISyntaxException e) {
		}
		byte[] jsonBytes = fetcher.fetch();

		
		if (jsonBytes == null)
			return null;
		
		String json = new String(jsonBytes);
		//fix what seems to be an intentional JSON syntax typo put ther by mininova
		json = json.replace("\"hash\":", ", \"hash\":");
		
		// Feel the power of reflection
		JsonEngine engine = new JsonEngine();
		MininovaVuzeResponse response = engine.toObject(json, MininovaVuzeResponse.class);

		return response;
	}
}
