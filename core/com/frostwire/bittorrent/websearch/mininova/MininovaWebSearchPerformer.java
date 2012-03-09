package com.frostwire.bittorrent.websearch.mininova;

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

public class MininovaWebSearchPerformer implements WebSearchPerformer {

    public List<WebSearchResult> search(String keywords) {

        List<WebSearchResult> result = new ArrayList<WebSearchResult>();

        MininovaVuzeResponse response = searchMininovaVuze(keywords);

        if (response != null && response.results != null)
            for (MininovaVuzeItem item : response.results) {

                WebSearchResult sr = new MininovaVuzeWebSearchResult(item);

                result.add(sr);
            }

        return result;
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
            fetcher = new HttpFetcher(new URI("http://www.mininova.org/vuze.php?search=" + iha), HTTP_TIMEOUT);
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
