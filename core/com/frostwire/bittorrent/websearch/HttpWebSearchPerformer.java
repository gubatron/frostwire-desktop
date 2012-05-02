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

/**
 * Extend this for engines that don't provide JSON APIs.
 *
 * @author gubatron
 *
 */
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
            fetcher = new HttpFetcher(getURI(keywords), HTTP_TIMEOUT);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return result;
        }
        byte[] htmlBytes = fetcher.fetch();

        if (htmlBytes == null) {
            return result;
        }

        String html = new String(htmlBytes);

        String regex = getRegex();
        
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);
        
        //System.out.println(html);
        
        int max = getMaxResults();
        
        int i = 0;
        
        while (matcher.find() && i < max) {
            try {
                WebSearchResult sr = getNextSearchResult(matcher);
                if (sr != null) {
                    result.add(sr);
                    i++;
                }
            } catch (Exception e) {
                // do nothing
            }
        }
        
        return result;
    }

    /** Returns the URI of the search engine search command */
	protected abstract URI getURI(String keywords) throws URISyntaxException;
	
	/** This method should return an implementation of WebSearchResult using a matcher that is able to find all the torrent fields*/
    protected abstract WebSearchResult getNextSearchResult(Matcher matcher);

    /** This function must return the regex necessary for a pattern matcher to find the necessary fields of a SearchResult*/
    protected abstract String getRegex();
    
    protected int getMaxResults() {
        return Integer.MAX_VALUE;
    }
}
