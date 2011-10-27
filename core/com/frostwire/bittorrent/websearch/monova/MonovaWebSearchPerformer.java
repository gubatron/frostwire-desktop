package com.frostwire.bittorrent.websearch.monova;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.frostwire.HttpFetcher;
import com.frostwire.bittorrent.websearch.HttpWebSearchPerformer;
import com.frostwire.bittorrent.websearch.WebSearchResult;

public class MonovaWebSearchPerformer extends HttpWebSearchPerformer {

    private static final Pattern HTML_PATTERN = Pattern
            .compile("(?is).*<div id=\"downloadbox\"><h2><a href=\"(.*)\" rel=\"nofollow\"><img src=\"http://www.monova.org/images/download.png\".*<a href=\"magnet:\\?xt=urn:btih:(.*)\"><b>Magnet</b></a>.*<font color=\"[A-Za-z]*\">(.*)</font> seeds,.*<strong>Total size:</strong>(.*)<br /><strong>Pieces:.*");

    @Override
    public URI getURI(String encodedKeywords) throws URISyntaxException {
        return new URI("http://www.monova.org/search.php??sort=5&term=" + encodedKeywords);
    }

    @Override
    public WebSearchResult getNextSearchResult(final Matcher matcher) {
        String torrentDetailsUrl = "http://www.monova.org/details.php?id=" + matcher.group(1);
        //System.out.println(torrentDetailsUrl);

        String html = fetchTorrentPage(torrentDetailsUrl);

        Matcher innerMatcher = HTML_PATTERN.matcher(html);

        if (innerMatcher.find()) {
            return new MonovaWebSearchResult(torrentDetailsUrl, innerMatcher);
        } else {
            return null;
        }
    }

    @Override
    public String getRegex() {
        return "(?is)<a href=\"http://www.monova.org/torrent/([0-9]*)/";
    }
    
    @Override
    protected int getMax() {
        return 10;
    }

    private String fetchTorrentPage(String torrentUrl) {
        HttpFetcher fetcher = null;
        try {
            fetcher = new HttpFetcher(new URI(torrentUrl));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
        byte[] htmlBytes = fetcher.fetch();

        if (htmlBytes == null) {
            return null;
        }

        return new String(htmlBytes);
    }

    /*
    public static void main(String[] args) throws IOException {
    	File f = new File("/Users/gubatron/Desktop/tpb.sample.html");
    	FileInputStream fis = new FileInputStream(f);
    	DataInputStream dis = new DataInputStream(fis);

    	byte[] fileBytes = new byte[(int) f.length()];
    	dis.readFully(fileBytes);
    	String HTML = new String(fileBytes);
    	
    	TPBWebSearchPerformer performer = new TPBWebSearchPerformer();
    	
    	Pattern pattern = Pattern.compile(performer.getRegex());
    	Matcher matcher = pattern.matcher(HTML);
    	
    	int i=0;
    	
    	while (matcher.find()) {
    		TPBWebSearchResult sr = new TPBWebSearchResult(matcher);
    		System.out.println(i++);
    	}
    	
    	fis.close();
    }
    */

}
