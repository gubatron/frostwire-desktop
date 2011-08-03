package com.frostwire.bittorrent.websearch.isohunt;

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

public class ISOHuntWebSearchPerformer implements WebSearchPerformer {

    public List<WebSearchResult> search(String keywords) {
        List<WebSearchResult> result = new ArrayList<WebSearchResult>();

        ISOHuntResponse response = searchISOHunt(keywords);

        if (response.items != null && response.items.list != null)
            for (ISOHuntItem item : response.items.list) {

                WebSearchResult sr = new ISOHuntWebSearchResult(item);

                result.add(sr);
            }

        return result;
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
            e.printStackTrace();
        }

        HttpFetcher fetcher = null;
        try {
            fetcher = new HttpFetcher(new URI("http://isohunt.com/js/json.php?ihq=" + iha + "&start=1&rows=100&sort=seeds"));
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
}
