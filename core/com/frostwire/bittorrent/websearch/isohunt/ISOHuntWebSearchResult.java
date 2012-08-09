package com.frostwire.bittorrent.websearch.isohunt;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.frostwire.bittorrent.websearch.WebSearchResult;

public class ISOHuntWebSearchResult implements WebSearchResult {

    private ISOHuntItem _item;

    public ISOHuntWebSearchResult(ISOHuntItem item) {
        _item = item;
    }

    public long getCreationTime() {
        //Thu, 29 Apr 2010 16:32:44 GMT
        SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        long result = System.currentTimeMillis();
        try {
            result = date.parse(_item.pubDate).getTime();
        } catch (ParseException e) {
        }
        return result;
    }

    public String getFileName() {
        String titleNoTags = _item.title.replace("<b>", "").replace("</b>", "");
        return titleNoTags + ".torrent";
    }

    public String getFilenameNoExtension() {
        return "<html>" + _item.title + "</html>";
    }

    public String getHash() {
        return _item.hash;
    }

    public String getTorrentURI() {
        return _item.enclosure_url;
    }

    public long getSize() {
        return Long.valueOf(_item.length);
    }

   public String getVendor() {
        return "ISOHunt";
    }

    public int getSeeds() {
        try {
            return Integer.valueOf(_item.Seeds);
        } catch (Exception e) {
            //oh well
            return 0;
        }
    }

    public String getDetailsUrl() {
        return _item.link;
    }

    @Override
    public String getDisplayName() {
        return _item.title;
    }
}
