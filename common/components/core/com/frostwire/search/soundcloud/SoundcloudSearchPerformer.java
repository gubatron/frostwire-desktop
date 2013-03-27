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

package com.frostwire.search.soundcloud;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.frostwire.search.PagedWebSearchPerformer;
import com.frostwire.search.SearchResult;
import com.frostwire.util.JsonUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class SoundcloudSearchPerformer extends PagedWebSearchPerformer {

    private static final int MAX_RESULTS = 16;

    private static final String DATE_FORMAT = "MMMM, dd yyyy HH:mm:ss Z";

    private static final String REGEX = "(?is)<a href=\"http://i1.sndcdn.com/artworks-(.*?)\" class=\"artwork\".*?<abbr title='(.*?)'.*?window.SC.bufferTracks.push\\((.*?)\\);";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    public SoundcloudSearchPerformer(long token, String keywords, int timeout) {
        super(token, keywords, timeout, MAX_RESULTS / 4);
    }

    @Override
    protected String getUrl(int page, String encodedKeywords) {
        return "http://soundcloud.com/tracks/search?page=" + page + "&q[fulltext]=" + encodedKeywords + "&q[downloadable]=true&advanced=1";
    }

    @Override
    protected List<? extends SearchResult> searchPage(String page) {
        List<SearchResult> result = new LinkedList<SearchResult>();

        Matcher matcher = PATTERN.matcher(page);

        int max = MAX_RESULTS;

        int i = 0;

        while (matcher.find() && i < max && !isStopped()) {
            try {
                SoundcloudItem item = JsonUtils.toObject(matcher.group(3), SoundcloudItem.class);
                try {
                    item.thumbnailUrl = buildThumbnailUrl(matcher.group(1));
                    item.date = new SimpleDateFormat(DATE_FORMAT, Locale.US).parse(matcher.group(2)).getTime();
                } catch (Throwable e) {
                    item.date = -1;
                }
                SearchResult sr = new SoundcloudSearchResult(item);
                if (sr != null) {
                    result.add(sr);
                    i++;
                }
            } catch (Throwable e) {
                // do nothing
            }
        }

        return result;
    }

    private String buildThumbnailUrl(String str) {
        //http://i1.sndcdn.com/artworks-000019588274-le8r71-crop.jpg?be0edad
        //https://i1.sndcdn.com/artworks-000019588274-le8r71-t500x500.jpg
        return "http://i1.sndcdn.com/artworks-" + str.substring(0, str.indexOf("-crop.")) + "-t300x300.jpg";
    }
}
