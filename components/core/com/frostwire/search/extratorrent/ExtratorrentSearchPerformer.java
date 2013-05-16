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

package com.frostwire.search.extratorrent;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.frostwire.search.torrent.TorrentJsonSearchPerformer;
import com.frostwire.util.JsonUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class ExtratorrentSearchPerformer extends TorrentJsonSearchPerformer<ExtratorrentItem, ExtratorrentSearchResult> {

    private final Comparator<ExtratorrentItem> itemComparator;
    
    public ExtratorrentSearchPerformer(long token, String keywords, int timeout) {
        super(token, keywords, timeout, 1);
        
        itemComparator = new Comparator<ExtratorrentItem>() {
            @Override
            public int compare(ExtratorrentItem a, ExtratorrentItem b) {
                return b.seeds - a.seeds;
            }
            
        };
    }

    @Override
    protected String getUrl(int page, String encodedKeywords) {
        return "http://extratorrent.com/json/?search=" + encodedKeywords;
    }

    @Override
    protected List<ExtratorrentItem> parseJson(String json) {
        ExtratorrentResponse response = JsonUtils.toObject(json, ExtratorrentResponse.class);
        Collections.sort(response.list,itemComparator);
        return response.list;
    }

    @Override
    protected ExtratorrentSearchResult fromItem(ExtratorrentItem item) {
        return new ExtratorrentSearchResult(item);
    }
    
    
}
