package com.frostwire.bittorrent.websearch.kat;

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

public class KATWebSearchPerformer implements WebSearchPerformer {

    public List<WebSearchResult> search(String keywords) {
        List<WebSearchResult> result = new ArrayList<WebSearchResult>();

        KATResponse response = searchClearBits(keywords);

        if (response != null && response.list != null)
            for (KATItem bucket : response.list) {

                WebSearchResult sr = new KATWebSearchResult(bucket);

                result.add(sr);
            }

        return result;
    }

    private KATResponse searchClearBits(String keywords) {
        String iha = null;
        try {
            iha = URLEncoder.encode(keywords, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpFetcher fetcher = null;
        try {
            fetcher = new HttpFetcher(new URI("http://www.kat.ph/json.php?q=" + iha));
        } catch (URISyntaxException e) {

        }
        byte[] jsonBytes = fetcher.fetch();

        if (jsonBytes == null)
            return null;

        String json = new String(jsonBytes);

        // Feel the power of reflection
        JsonEngine engine = new JsonEngine();
        
        KATResponse response = null;
        
        try {
            response = engine.toObject(json, KATResponse.class);
        } catch (Exception e) {
        	return null;
        }

        response.fixItems();

        return response;
    }
}