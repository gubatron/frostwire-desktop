/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
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

package com.frostwire.search.torrentsfm;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.frostwire.search.SearchMatcher;
import com.frostwire.search.torrent.AbstractTorrentSearchResult;

public class TorrentsSearchResult extends AbstractTorrentSearchResult {

    private final static long[] BYTE_MULTIPLIERS = new long[] { 1, 2 << 9, 2 << 19, 2 << 29, 2 << 39, 2 << 49 };

    private static final Map<String, Integer> UNIT_TO_BYTE_MULTIPLIERS_MAP;

    static {
        UNIT_TO_BYTE_MULTIPLIERS_MAP = new HashMap<String, Integer>();
        UNIT_TO_BYTE_MULTIPLIERS_MAP.put("B", 0);
        UNIT_TO_BYTE_MULTIPLIERS_MAP.put("KiB", 1);
        UNIT_TO_BYTE_MULTIPLIERS_MAP.put("MiB", 2);
        UNIT_TO_BYTE_MULTIPLIERS_MAP.put("GiB", 3);
        UNIT_TO_BYTE_MULTIPLIERS_MAP.put("TiB", 4);
        UNIT_TO_BYTE_MULTIPLIERS_MAP.put("PiB", 5);
    }

    private String filename;
    private String displayName;
    private String detailsUrl;
    private String torrentUrl;
    private String infoHash;
    private long size;
    private long creationTime;
    private int seeds;

    public TorrentsSearchResult(String domainName, String detailsUrl, SearchMatcher matcher) {
        this.detailsUrl = detailsUrl;
        this.filename = matcher.group(1);//parseFileName(matcher.group(1), FilenameUtils.getBaseName(detailsUrl));
        this.size = parseSize(matcher.group(2));
        this.creationTime = parseCreationTime(matcher.group(4));
        this.seeds = parseSeeds(matcher.group(3));
        //a magnet
        this.torrentUrl = matcher.group(5);//"http://" + domainName + "/tor/" + matcher.group(5) + ".torrent";
        this.displayName = matcher.group(1);//HtmlManipulator.replaceHtmlEntities(FilenameUtils.getBaseName(filename));
        this.infoHash = parseInfoHash(torrentUrl);
    }

    private String parseInfoHash(String url) {
        //magnet:?xt=urn:btih:e3811b9539cacff680e418124272177c47477157&amp;
        return url.substring("magnet:?xt=urn:btih:".length(),url.indexOf("&amp"));
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String getSource() {
        return "Torrents.fm";
    }

    @Override
    public String getHash() {
        return infoHash;
    }

    @Override
    public int getSeeds() {
        return seeds;
    }

    @Override
    public String getDetailsUrl() {
        return detailsUrl;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public String getTorrentUrl() {
        return torrentUrl;
    }

//    private String parseFileName(String urlEncodedFileName, String fallbackName) {
//        String decodedFileName = fallbackName;
//        try {
//            if (!StringUtils.isNullOrEmpty(urlEncodedFileName)) {
//                decodedFileName = URLDecoder.decode(urlEncodedFileName, "UTF-8");
//                decodedFileName.replace("&amp;", "and");
//            }
//        } catch (UnsupportedEncodingException e) {
//        }
//        return decodedFileName + ".torrent";
//    }

    private long parseSize(String group) {
        String[] size = group.split(" ");
        String amount = size[0].trim();
        String unit = size[1].trim();

        long multiplier = BYTE_MULTIPLIERS[UNIT_TO_BYTE_MULTIPLIERS_MAP.get(unit)];

        //fractional size
        if (amount.indexOf(".") > 0) {
            float floatAmount = Float.parseFloat(amount);
            return (long) (floatAmount * multiplier);
        }
        //integer based size
        else {
            int intAmount = Integer.parseInt(amount);
            return (long) (intAmount * multiplier);
        }
    }

    private int parseSeeds(String group) {
        try {
            return Integer.parseInt(group);
        } catch (Exception e) {
            return 0;
        }
    }

    private long parseCreationTime(String dateString) {
        long result = System.currentTimeMillis();
        try {
            SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            result = myFormat.parse(dateString).getTime();
        } catch (Throwable t) {
        }
        return result;
    }
}