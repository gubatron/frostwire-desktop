package com.frostwire.bittorrent.websearch.btjunkie;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.frostwire.HttpFetcher;
import com.frostwire.bittorrent.websearch.WebSearchPerformer;
import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.frostwire.json.JsonEngine;

public class BTJunkieWebSearchPerformer implements WebSearchPerformer {

    @Override
    public List<WebSearchResult> search(String keywords) {
        List<WebSearchResult> result = new ArrayList<WebSearchResult>();

        BTJunkieResponse response = searchBTJunkie(keywords);

        if (response != null && response.results != null)
            for (BTJunkieItem item : response.results) {

                WebSearchResult sr = new BTJunkieResponseWebSearchResult(item);

                result.add(sr);
            }

        return result;
    }

    private BTJunkieResponse searchBTJunkie(String keywords) {
        String iha = null;
        try {
            iha = URLEncoder.encode(keywords, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        HttpFetcher fetcher = null;
        try {
            fetcher = new HttpFetcher(new URI("http://btjunkie.org/json.php?q=" + iha));
        } catch (URISyntaxException e) {

        }
        byte[] jsonBytes = fetcher.fetch();

        if (jsonBytes == null)
            return null;

        String json = new String(jsonBytes);
        json = json.replace("&#039", "'");
        
        // Feel the power of reflection
        JsonEngine engine = new JsonEngine();
        BTJunkieResponse response = engine.toObject(json, BTJunkieResponse.class);

        return response;
    }
}
