package com.frostwire.bittorrent.websearch.tpb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;

import com.frostwire.bittorrent.websearch.HttpWebSearchPerformer;
import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.limegroup.gnutella.settings.SearchEnginesSettings;

public class TPBWebSearchPerformer extends HttpWebSearchPerformer {

    @Override
    public URI getURI(String encodedKeywords) throws URISyntaxException {
        return new URI("http://thepiratebay.se/search/" + encodedKeywords + "/0/7/0");
    }

    @Override
    public WebSearchResult getNextSearchResult(final Matcher matcher) {
        return new TPBWebSearchResult(matcher);
    }

    @Override
    public String getRegex() {
        //smoke this joint
        return "(?is)<td class=\"vertTh\">.*?<a href=\"[^\"]*?\" title=\"More from this category\">(.*?)</a>.*?</td>.*?<a href=\"([^\"]*?)\" class=\"detLink\" title=\"Details for ([^\"]*?)\">.*?</a>.*?<a href=\\\"(magnet:\\?xt=urn:btih:.*?)\\\" title=\\\"Download this torrent using magnet\\\">.*?</a>.*?<font class=\"detDesc\">Uploaded ([^,]*?), Size (.*?), ULed.*?<td align=\"right\">(.*?)</td>\\s*<td align=\"right\">(.*?)</td>";
    }

    @Override
    protected int getMaxResults() {
        return SearchEnginesSettings.TPB_WEBSEARCHPERFORMER_MAX_RESULTS.getValue();
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
