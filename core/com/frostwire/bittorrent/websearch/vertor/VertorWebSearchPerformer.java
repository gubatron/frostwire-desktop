package com.frostwire.bittorrent.websearch.vertor;

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

public class VertorWebSearchPerformer implements WebSearchPerformer {

    @Override
    public List<WebSearchResult> search(String keywords) {
        List<WebSearchResult> result = new ArrayList<WebSearchResult>();

        VertorResponse response = searchExtratorrent(keywords);

        if (response.results != null)
            for (VertorItem item : response.results) {

                WebSearchResult sr = new VertorResponseWebSearchResult(item);

                result.add(sr);
            }

        return result;
    }

    private VertorResponse searchExtratorrent(String keywords) {
        String iha = null;
        try {
            iha = URLEncoder.encode(keywords, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        HttpFetcher fetcher = null;
        try {
            fetcher = new HttpFetcher(new URI("http://www.vertor.com/index.php?mod=json&search=&words=" + iha));
        } catch (URISyntaxException e) {

        }
        byte[] jsonBytes = fetcher.fetch();

        if (jsonBytes == null)
            return null;

        String json = new String(jsonBytes);
        
        // Feel the power of reflection
        JsonEngine engine = new JsonEngine();
        VertorResponse response = engine.toObject(json, VertorResponse.class);

        return response;
    }
}
