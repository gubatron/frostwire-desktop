package com.frostwire.bittorrent.websearch.monova;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.frostwire.HttpFetcher;
import com.frostwire.bittorrent.websearch.HttpWebSearchPerformer;
import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.limegroup.gnutella.settings.SearchEnginesSettings;

public class MonovaWebSearchPerformer extends HttpWebSearchPerformer {
    
    private static final Log LOG = LogFactory.getLog(MonovaWebSearchPerformer.class);

    private static final Pattern HTML_PATTERN = Pattern
            .compile("(?is).*<div id=\"downloadbox\"><h2><a href=\"(.*)\" rel=\"nofollow\"><img src=\"http://www.mnova.eu/images/download.png\".*<a href=\"magnet:\\?xt=urn:btih:(.*)\"><b>Magnet</b></a>.*<font color=\"[A-Za-z]*\">(.*)</font> seeds,.*<strong>Total size:</strong>(.*)<br /><strong>Pieces:.*");

    @Override
    public URI getURI(String encodedKeywords) throws URISyntaxException {
        return new URI("http://www.mnova.eu/search.php?sort=5&term=" + encodedKeywords);
    }

    @Override
    public WebSearchResult getNextSearchResult(final Matcher matcher) {
        String torrentDetailsUrl = "http://www.mnova.eu/details.php?id=" + matcher.group(1);
        //System.out.println(torrentDetailsUrl);
        
        String html = fetchTorrentPage(torrentDetailsUrl);
        if (html == null) {
            return null;
        }
        
        Matcher innerMatcher = HTML_PATTERN.matcher(html);

        if (innerMatcher.find()) {
            return new MonovaWebSearchResult(torrentDetailsUrl, innerMatcher);
        } else {
            return null;
        }
    }

    @Override
    public String getRegex() {
        return "(?is)<a href=\"http://www.mnova.eu/torrent/([0-9]*)/";
    }
    
    @Override
    protected int getMaxResults() {
        return SearchEnginesSettings.MONOVA_WEBSEARCHPERFORMER_MAX_RESULTS.getValue();
    }

    private String fetchTorrentPage(String torrentUrl) {
        HttpFetcher fetcher = null;
        try {
            fetcher = new HttpFetcher(new URI(torrentUrl), HTTP_TIMEOUT);
        } catch (URISyntaxException e) {
            LOG.error("Error creating HttpFetcher", e);
            return null;
        }
        
        byte[] htmlBytes = fetcher.fetch();
        if (htmlBytes == null) {
            return null;
        }
        
        return new String(htmlBytes);
    }
}
