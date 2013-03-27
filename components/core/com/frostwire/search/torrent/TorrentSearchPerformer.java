/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.frostwire.search.torrent;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.util.TorrentUtils;

import com.frostwire.search.CrawlPagedWebSearchPerformer;
import com.frostwire.search.SearchResult;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public abstract class TorrentSearchPerformer extends CrawlPagedWebSearchPerformer<TorrentCrawlableSearchResult> {

    public TorrentSearchPerformer(long token, String keywords, int timeout, int pages) {
        super(token, keywords, timeout, pages);
    }

    @Override
    protected String getCrawlUrl(TorrentCrawlableSearchResult sr) {
        return sr.getTorrentUrl();
    }

    @Override
    protected List<? extends SearchResult> crawlResult(TorrentCrawlableSearchResult sr, byte[] data) throws Exception {
        List<TorrentCrawledSearchResult> list = new LinkedList<TorrentCrawledSearchResult>();

        TOTorrent torrent = TorrentUtils.readFromBEncodedInputStream(new ByteArrayInputStream(data));

        if (torrent != null) {
            TOTorrentFile[] files = torrent.getFiles();

            for (int i = 0; !isStopped() && i < files.length; i++) {
                TOTorrentFile file = files[i];
                list.add(new TorrentCrawledSearchResult(sr, file));
            }
        }

        return list;
    }
}
