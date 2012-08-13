/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.bittorrent.websearch.soundcloud;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.util.StringUtils;

import com.frostwire.HttpFetcher;
import com.frostwire.JsonEngine;
import com.frostwire.bittorrent.websearch.WebSearchPerformer;
import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.limegroup.gnutella.settings.SearchEnginesSettings;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class SoundcloudSearchPerformer implements WebSearchPerformer {

    private static final Log LOG = LogFactory.getLog(SoundcloudSearchPerformer.class);

    private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM, dd yyyy HH:mm:ss Z");

    public List<WebSearchResult> search(String keywords) {
        List<WebSearchResult> result = new ArrayList<WebSearchResult>();

        try {
            keywords = URLEncoder.encode(keywords, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("Can't UTF-8 encode keywords: " + keywords, e);
        }

        int pages = getMaxResults() / 10;

        for (int i = 0; i < pages; i++) {
            result.addAll(searchPage(i + 1, keywords));
        }

        return result;
    }

    private List<WebSearchResult> searchPage(int page, String keywords) {
        List<WebSearchResult> result = new ArrayList<WebSearchResult>();

        HttpFetcher fetcher = null;
        try {
            fetcher = new HttpFetcher(getURI(page, keywords), HTTP_TIMEOUT);
        } catch (URISyntaxException e) {
            LOG.error("Can't create uri", e);
            return result;
        }
        byte[] htmlBytes = fetcher.fetch();

        if (htmlBytes == null) {
            return result;
        }

        String html = StringUtils.getUTF8String(htmlBytes);

        String regex = getRegex();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);

        JsonEngine engine = new JsonEngine();

        int max = getMaxResults();

        int i = 0;

        while (matcher.find() && i < max) {
            try {
                SoundcloudItem item = engine.toObject(matcher.group(2), SoundcloudItem.class);
                try {
                    item.date = DATE_FORMAT.parse(matcher.group(1)).getTime();
                } catch (Throwable e) {
                    item.date = -1;
                }
                WebSearchResult sr = new SoundcloudTrackSearchResult(item);
                if (sr != null) {
                    result.add(sr);
                    i++;
                }
            } catch (Exception e) {
                // do nothing
            }
        }

        return result;
    }

    public URI getURI(int page, String encodedKeywords) throws URISyntaxException {
        return new URI("http://soundcloud.com/tracks/search?page=" + page + "&q[fulltext]=" + encodedKeywords);
    }

    public String getRegex() {
        return "(?is)<abbr title='(.*?)'.*?window.SC.bufferTracks.push\\((.*?)\\);";
    }

    protected int getMaxResults() {
        return SearchEnginesSettings.SOUNDCLOUD_WEBSEARCHPERFORMER_MAX_RESULTS.getValue();
    }
}
