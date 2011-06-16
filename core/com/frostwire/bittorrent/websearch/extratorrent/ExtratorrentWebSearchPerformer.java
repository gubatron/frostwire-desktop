package com.frostwire.bittorrent.websearch.extratorrent;

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

public class ExtratorrentWebSearchPerformer implements WebSearchPerformer {

    @Override
    public List<WebSearchResult> search(String keywords) {
        List<WebSearchResult> result = new ArrayList<WebSearchResult>();

        ExtratorrentResponse response = searchExtratorrent(keywords);

        if (response.list != null)
            for (ExtratorrentItem item : response.list) {

                WebSearchResult sr = new ExtratorrentResponseWebSearchResult(item);

                result.add(sr);
            }

        return result;
    }

    private ExtratorrentResponse searchExtratorrent(String keywords) {
        String iha = null;
        try {
            iha = URLEncoder.encode(keywords, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        HttpFetcher fetcher = null;
        try {
            fetcher = new HttpFetcher(new URI("http://extratorrent.com/json/?search=" + iha));
        } catch (URISyntaxException e) {

        }
        byte[] jsonBytes = fetcher.fetch();

        if (jsonBytes == null)
            return null;

        String json = new String(jsonBytes);
        
        // Feel the power of reflection
        JsonEngine engine = new JsonEngine();
        ExtratorrentResponse response = engine.toObject(json, ExtratorrentResponse.class);

        return response;
    }
}
