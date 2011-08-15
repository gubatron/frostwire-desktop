package com.frostwire.bittorrent.websearch;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.frostwire.HttpFetcher;

public abstract class HttpWebSearchPerformer implements WebSearchPerformer {

    public List<WebSearchResult> search(String keywords) {
        List<WebSearchResult> result = new ArrayList<WebSearchResult>();
        
        try {
            keywords = URLEncoder.encode(keywords, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpFetcher fetcher = null;
        try {
            fetcher = new HttpFetcher(getURI(keywords));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return result;
        }
        byte[] htmlBytes = fetcher.fetch();

        if (htmlBytes == null) {
            return result;
        }

        String html = new String(htmlBytes);
        
        System.out.println(html);

        String regex = getRegex();
        
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);
        
        while (matcher.find()) {
        	System.out.println("found...");
        	WebSearchResult sr = getNextSearchResult(matcher);
        	result.add(sr);
        }
       
        
        return result;
    }

	public abstract URI getURI(String keywords) throws URISyntaxException; 
    public abstract WebSearchResult getNextSearchResult(Matcher matcher);
	public abstract String getRegex();
}
