package com.frostwire.bittorrent.websearch.tpb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;

import com.frostwire.bittorrent.websearch.HttpWebSearchPerformer;
import com.frostwire.bittorrent.websearch.WebSearchResult;

public class TPBWebSearchPerformer extends HttpWebSearchPerformer {

	@Override
	public URI getURI(String encodedKeywords) throws URISyntaxException {
		return new URI("http://thepiratebay.org/search/"+encodedKeywords+"/0/8/0");
	}

	@Override
	public WebSearchResult getNextSearchResult(final Matcher matcher) {
		return new TPBWebSearchResult(matcher);
	}

	@Override
	public String getRegex() {
		//smoke this joint
		return "(?is)<td class=\"vertTh\">.*?<a href=\"[^\"]*?\" title=\"More from this category\">(.*?)</a>.*?</td>.*?<a href=\"([^\"]*?)\" class=\"detLink\" title=\"Details for ([^\"]*?)\">.*?</a>.*?<a href=\"([^\"]*?)\" title=\"Download this torrent\">.*?<font class=\"detDesc\">Uploaded (.*?)&nbsp.*?Size (.*?), ULed.*?<td align=\"right\">(.*?)</td>\\s*<td align=\"right\">(.*?)</td>";
	}

}
