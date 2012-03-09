package com.frostwire.bittorrent.websearch.clearbits;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.frostwire.HttpFetcher;
import com.frostwire.JsonEngine;
import com.frostwire.bittorrent.websearch.WebSearchPerformer;
import com.frostwire.bittorrent.websearch.WebSearchResult;

public class ClearBitsWebSearchPerformer implements WebSearchPerformer {

    public List<WebSearchResult> search(String keywords) {
        List<WebSearchResult> result = new ArrayList<WebSearchResult>();

        ClearBitsResponse response = searchClearBits(keywords);

        if (response != null && response.results != null)
            for (ClearBitsItem bucket : response.results) {

                WebSearchResult sr = new ClearBitsWebSearchResult(bucket);

                result.add(sr);
            }

        return result;
    }

    private ClearBitsResponse searchClearBits(String keywords) {
        String iha = null;
        try {
            iha = URLEncoder.encode(keywords, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        HttpFetcher fetcher = null;
        try {
            fetcher = new HttpFetcher(new URI("http://www.clearbits.net/home/search/index.json?query=" + iha), HTTP_TIMEOUT);
        } catch (URISyntaxException e) {

        }
        byte[] jsonBytes = fetcher.fetch();

        if (jsonBytes == null)
            return null;

        String json = new String(jsonBytes);

        // Feel the power of reflection
        JsonEngine engine = new JsonEngine();
        
        ClearBitsResponse response = null;
        
        try {
        	response = engine.toObject(json, ClearBitsResponse.class);
        } catch (Exception e) {
        	return null;
        }

        response.fixItems();

        return response;
    }
}
