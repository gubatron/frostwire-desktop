package com.frostwire.bittorrent.websearch;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.frostwire.HttpFetcher;
import com.frostwire.JsonEngine;
import com.frostwire.bittorrent.websearch.isohunt.ISOHuntResponse;

public abstract class HttpWebSearchPerformer implements WebSearchPerformer {
    
    private final String uri;
    
    public HttpWebSearchPerformer(String uri) {
        this.uri = uri;
    }

    public List<WebSearchResult> search(String keywords) {
        List<WebSearchResult> result = new ArrayList<WebSearchResult>();
        
        try {
            keywords = URLEncoder.encode(keywords, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpFetcher fetcher = null;
        try {
            fetcher = new HttpFetcher(new URI("uri" + keywords));
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
        
        
        
        return result;
    }
    
    public abstract String getRegex();
}
