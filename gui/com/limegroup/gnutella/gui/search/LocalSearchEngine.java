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

package com.limegroup.gnutella.gui.search;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.linkcrawler.LinkCrawler;
import jd.controlling.linkcrawler.PackageInfo;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloader;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderCallBackInterface;
import org.gudy.azureus2.core3.torrentdownloader.TorrentDownloaderFactory;
import org.gudy.azureus2.core3.util.TorrentUtils;
import org.h2.fulltext.FullTextLucene2;
import org.jdownloader.controlling.filter.LinkFilterController;
import org.limewire.concurrent.ExecutorsHelper;
import org.limewire.util.StringUtils;

import com.frostwire.JsonEngine;
import com.frostwire.alexandria.LibraryUtils;
import com.frostwire.bittorrent.websearch.WebSearchResult;
import com.frostwire.gui.filters.SearchFilter;
import com.frostwire.websearch.youtube.YouTubeEntry;
import com.frostwire.websearch.youtube.YouTubeEntryLink;
import com.frostwire.websearch.youtube.YouTubeSearchResult;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.search.db.SmartSearchDB;
import com.limegroup.gnutella.gui.search.db.TorrentDBPojo;
import com.limegroup.gnutella.gui.search.db.TorrentFileDBPojo;
import com.limegroup.gnutella.settings.SearchSettings;

public class LocalSearchEngine {

    private static final Log LOG = LogFactory.getLog(LocalSearchEngine.class);

    private static final ExecutorService DOWNLOAD_TORRENTS_EXECUTOR;
    private static final ExecutorService CRAWL_YOUTUBE_LINKS_EXECUTOR;
    private static final int MAX_TORRENT_DOWNLOADS = 10;

    private static final ExecutorService INDEX_TORRENTS_EXECUTOR;

    static {
        DOWNLOAD_TORRENTS_EXECUTOR = ExecutorsHelper.newFixedSizePriorityThreadPool(MAX_TORRENT_DOWNLOADS, "DownloadTorrentsExecutor");
        CRAWL_YOUTUBE_LINKS_EXECUTOR = ExecutorsHelper.newFixedSizePriorityThreadPool(2, "CRAWL_YOUTUBE_LINKS_EXECUTOR");
        INDEX_TORRENTS_EXECUTOR = ExecutorsHelper.newFixedSizeThreadPool(1, "IndexTorrentsExecutor");
    }

    private final int DEEP_SEARCH_DELAY;
    private final int MAXIMUM_TORRENTS_TO_SCAN;
    private final int DEEP_SEARCH_ROUNDS;
    private final int LOCAL_SEARCH_RESULTS_LIMIT;

    private static LocalSearchEngine INSTANCE;

    private static final Comparator<SearchResultDataLine> TORRENT_SEED_TABLELINE_COMPARATOR = new Comparator<SearchResultDataLine>() {

        @Override
        public int compare(SearchResultDataLine o1, SearchResultDataLine o2) {
            return o2.getSeeds() - o1.getSeeds();
        }
    };

    /**
     * We'll keep here every info hash we've already processed during the
     * session
     */
    private HashSet<String> KNOWN_INFO_HASHES = new HashSet<String>();
    private SmartSearchDB DB;
    private JsonEngine JSON_ENGINE;

    private Queue<IndexTorrentElement> INDEX_TORRENT_QUEUE = new LinkedList<IndexTorrentElement>();

    public LocalSearchEngine() {
        DEEP_SEARCH_DELAY = SearchSettings.SMART_SEARCH_START_DELAY.getValue();
        MAXIMUM_TORRENTS_TO_SCAN = SearchSettings.SMART_SEARCH_MAXIMUM_TORRENTS_TO_SCAN.getValue();
        DEEP_SEARCH_ROUNDS = SearchSettings.SMART_SEARCH_DEEP_SEARCH_ROUNDS.getValue();
        LOCAL_SEARCH_RESULTS_LIMIT = SearchSettings.SMART_SEARCH_FULLTEXT_SEARCH_RESULTS_LIMIT.getValue();

        DB = new SmartSearchDB(SearchSettings.SMART_SEARCH_DATABASE_FOLDER.getValue());
        JSON_ENGINE = new JsonEngine();
    }

    public static LocalSearchEngine instance() {
        if (INSTANCE == null) {
            INSTANCE = new LocalSearchEngine();
        }

        return INSTANCE;
    }

    public final static HashSet<String> IGNORABLE_KEYWORDS;

    static {
        IGNORABLE_KEYWORDS = new HashSet<String>();
        IGNORABLE_KEYWORDS.addAll(Arrays.asList("me", "you", "he", "she", "they", "them", "we", "us", "my", "your", "yours", "his", "hers", "theirs", "ours", "the", "of", "in", "on", "out", "to", "at", "as", "and", "by", "not", "is", "are", "am", "was", "were", "will", "be", "for", "el", "la",
                "es", "de", "los", "las", "en"));
    }

    /**
     * Avoid possible SQL errors due to escaping. Cleans all double spaces and
     * trims.
     * 
     * @param str
     * @return
     */
    private final static String stringSanitize(String str) {
        str = stripHtml(str);
        str = str.replaceAll("\\.torrent|www\\.|\\.com|[\\\\\\/%_;\\-\\.\\(\\)\\[\\]\\n\\r" + '\uu2013' + "]", " ");
        return StringUtils.removeDoubleSpaces(str);
    }

    /**
     * Very simple html strip routine. Not for a wide use.
     * 
     * @param str
     * @return
     */
    private static String stripHtml(String str) {
        str = str.replaceAll("\\<.*?>", "");
        str = str.replaceAll("\\&.*?\\;", "");
        return str;
    }

    /**
     * Perform a simple Database Search, immediate results should be available
     * if there are matches.
     */
    public List<SmartSearchResult> search(String query) {
        query = LibraryUtils.fuzzyLuceneQuery(query);

        //FULL TEXT SEARCH, Returns the File IDs we care about.
        String fullTextIndexSql = "SELECT * FROM FTL_SEARCH(?, ?, 0)";

        //System.out.println(fullTextIndexSql);
        List<List<Object>> matchedFileRows = DB.query(fullTextIndexSql, query, LOCAL_SEARCH_RESULTS_LIMIT);

        int fileIDStrOffset = " PUBLIC   FILES  WHERE  FILEID =".length();

        StringBuilder fileIDSet = new StringBuilder("(");

        int numFilesFound = matchedFileRows.size();
        int i = 0;

        for (List<Object> row : matchedFileRows) {
            String rowStr = (String) row.get(0);
            fileIDSet.append(rowStr.substring(fileIDStrOffset));

            if (i++ < (numFilesFound - 1)) {
                fileIDSet.append(",");
            }
        }
        fileIDSet.append(")");

        String sql = "SELECT Torrents.json, Files.json, torrentName, fileName FROM Torrents JOIN Files ON Torrents.torrentId = Files.torrentId WHERE Files.fileId IN " + fileIDSet.toString() + " ORDER BY seeds DESC LIMIT " + LOCAL_SEARCH_RESULTS_LIMIT;
        //System.out.println(sql);
        long start = System.currentTimeMillis();
        List<List<Object>> rows = DB.query(sql);
        long delta = System.currentTimeMillis() - start;
        System.out.print("Found " + rows.size() + " local results in " + delta + "ms. ");

        //no query should ever take this long.
        if (delta > 3000) {
            System.out.println("\nWarning: Results took too long, there's something wrong with the database, you might want to delete your 'search_db' folder inside the FrostWire preferences folder.");
        }

        List<SmartSearchResult> results = new ArrayList<SmartSearchResult>();
        Map<Integer, SearchEngine> searchEngines = SearchEngine.getSearchEngineMap();

        // GUBENE
        String torrentJSON = null;
        for (List<Object> row : rows) {
            try {
                torrentJSON = (String) row.get(0);
                String fileJSON = (String) row.get(1);

                TorrentDBPojo torrentPojo = JSON_ENGINE.toObject(torrentJSON, TorrentDBPojo.class);

                if (!searchEngines.get(torrentPojo.searchEngineID).isEnabled()) {
                    continue;
                }

                TorrentFileDBPojo torrentFilePojo = JSON_ENGINE.toObject(fileJSON, TorrentFileDBPojo.class);

                results.add(new SmartSearchResult(torrentPojo, torrentFilePojo));
                KNOWN_INFO_HASHES.add(torrentPojo.hash);
            } catch (Throwable e) {
                // keep going dude
                LOG.error("Issues with POJO deserialization -> " + torrentJSON, e);
            }
        }

        System.out.println("Ended up with " + results.size() + " results");

        return results;
    }

    public List<DeepSearchResult> deepSearch(byte[] guid, String query, SearchInformation info) {
        SearchResultMediator rp = null;

        // Let's wait for enough search results from different search engines.
        sleep();

        // Wait for enough results or die if the ResultPanel has been closed.
        int tries = DEEP_SEARCH_ROUNDS;

        boolean scanYouTube = true;

        for (int i = tries; i > 0; i--) {
            if ((rp = SearchMediator.getResultPanelForGUID(new GUID(guid))) == null) {
                return null;
            }

            if (rp.isStopped()) {
                return null;
            }

            scanAvailableResults(guid, query, info, rp, scanYouTube);
            scanYouTube = false;

            sleep();
        }

        // did they close rp? nothing left to do.
        if (rp == null) {
            return null;
        }

        return null;
    }

    public void sleep() {
        try {
            Thread.sleep(DEEP_SEARCH_DELAY);
        } catch (InterruptedException e1) {
        }
    }

    private void scanAvailableResults(byte[] guid, String query, SearchInformation info, SearchResultMediator rp, boolean scanYouTube) {

        int foundTorrents = 0;

        List<SearchResultDataLine> allData = rp.getAllData();
        sortAndStripNonTorrents(allData);

        int order = 0;

        for (int i = 0; i < allData.size(); i++) {

            SearchResultDataLine line = allData.get(i);

            if (line.getInitializeObject() instanceof SearchEngineSearchResult) {
                if (foundTorrents >= MAXIMUM_TORRENTS_TO_SCAN) {
                    if (!scanYouTube) {
                        return;
                    }
                    continue;
                }
                if (!((SearchEngineSearchResult) line.getInitializeObject()).allowDeepSearch()) {
                    continue;
                }

                foundTorrents++;

                WebSearchResult webSearchResult = line.getSearchResult().getWebSearchResult();

                if (!KNOWN_INFO_HASHES.contains(webSearchResult.getHash())) {
                    KNOWN_INFO_HASHES.add(webSearchResult.getHash());
                    SearchEngine searchEngine = line.getSearchEngine();
                    scanDotTorrent(order++, webSearchResult, guid, query, searchEngine, info);
                }
            } else if (line.getInitializeObject() instanceof YouTubePackageSearchResult) {
                if (!scanYouTube) {
                    continue;
                }
                WebSearchResult webSearchResult = line.getSearchResult().getWebSearchResult();
                SearchEngine searchEngine = line.getSearchEngine();

                CrawlYouTubePackage task = new CrawlYouTubePackage(order++, guid, (YouTubeSearchResult) webSearchResult, searchEngine, info);
                CRAWL_YOUTUBE_LINKS_EXECUTOR.execute(task);
            }
        }
    }

    /**
     * Remove all results that are not torrents and sort them by seed (desc. order)
     * @param allData
     */
    private void sortAndStripNonTorrents(List<SearchResultDataLine> allData) {
        Collections.sort(allData, TORRENT_SEED_TABLELINE_COMPARATOR);
        Iterator<SearchResultDataLine> iterator = allData.iterator();
        while (iterator.hasNext()) {
            SearchResultDataLine next = iterator.next();

            if (!next.getExtension().toLowerCase().contains("torrent") && !next.getExtension().toLowerCase().contains("youtube")) {
                iterator.remove();
            }
        }
    }

    /**
     * Will decide wether or not to fetch the .torrent from the DHT.
     * 
     * If it has to download it, it will use a
     * LocalSearchTorrentDownloaderListener to start scanning, if the torrent
     * has already been fetched, it will perform an immediate search.
     * 
     * @param webSearchResult
     * @param searchEngine
     * @param info
     */
    private void scanDotTorrent(int order, WebSearchResult webSearchResult, byte[] guid, String query, SearchEngine searchEngine, SearchInformation info) {
        if (!torrentHasBeenIndexed(webSearchResult.getHash())) {
            // download the torrent

            SearchResultMediator rp = SearchMediator.getResultPanelForGUID(new GUID(guid));
            if (rp != null) {
                rp.incrementSearchCount();
            }

            DownloadTorrentTask task = new DownloadTorrentTask(order, guid, query, webSearchResult, searchEngine, info);
            DOWNLOAD_TORRENTS_EXECUTOR.execute(task);
        } else {
            //System.out.println(webSearchResult.getHash() + " indexed");
        }
    }

    private boolean torrentHasBeenIndexed(String infoHash) {
        List<List<Object>> rows = DB.query("SELECT indexed FROM Torrents WHERE infoHash LIKE ?", infoHash);
        return rows.size() > 0 && (Boolean) rows.get(0).get(0);
    }

    private void indexTorrent(WebSearchResult searchResult, TOTorrent theTorrent, SearchEngine searchEngine) {
        TorrentDBPojo torrentPojo = new TorrentDBPojo();
        torrentPojo.creationTime = searchResult.getCreationTime();
        torrentPojo.fileName = searchResult.getFileName();
        torrentPojo.hash = searchResult.getHash();
        torrentPojo.searchEngineID = searchEngine.getId();
        torrentPojo.seeds = searchResult.getSeeds();
        torrentPojo.size = searchResult.getSize();
        torrentPojo.torrentDetailsURL = searchResult.getTorrentDetailsURL();
        torrentPojo.torrentURI = searchResult.getTorrentURI();
        torrentPojo.vendor = searchResult.getVendor();

        TOTorrentFile[] files = theTorrent.getFiles();
        TorrentFileDBPojo[] tfPojos = new TorrentFileDBPojo[files.length];

        for (int i = 0; i < files.length; i++) {
            TOTorrentFile f = files[i];
            TorrentFileDBPojo tfPojo = new TorrentFileDBPojo();
            tfPojo.relativePath = f.getRelativePath();
            tfPojo.size = f.getLength();
            tfPojos[i] = tfPojo;
        }

        INDEX_TORRENT_QUEUE.offer(new IndexTorrentElement(torrentPojo, tfPojos));
        INDEX_TORRENTS_EXECUTOR.execute(new IndexTorrentTask());
    }

    private class LocalSearchTorrentDownloaderListener implements TorrentDownloaderCallBackInterface {

        private final AtomicBoolean finished = new AtomicBoolean(false);

        private final byte[] guid;
        private final Set<String> tokens;
        private final SearchEngine searchEngine;
        private final WebSearchResult webSearchResult;
        private final SearchInformation info;
        private final CountDownLatch finishSignal;

        public LocalSearchTorrentDownloaderListener(byte[] guid, String query, WebSearchResult webSearchResult, SearchEngine searchEngine, SearchInformation info, CountDownLatch finishSignal) {
            this.guid = guid;
            this.tokens = new HashSet<String>(Arrays.asList(query.toLowerCase().split(" ")));
            this.searchEngine = searchEngine;
            this.webSearchResult = webSearchResult;
            this.info = info;
            this.finishSignal = finishSignal;
        }

        @Override
        public void TorrentDownloaderEvent(int state, TorrentDownloader inf) {

            // index the torrent (insert it's structure in the local DB)
            if (state == TorrentDownloader.STATE_FINISHED && finished.compareAndSet(false, true)) {
                try {
                    File torrentFile = inf.getFile();
                    TOTorrent theTorrent = TorrentUtils.readFromFile(torrentFile, false);

                    // search right away on this torrent.
                    matchResults(theTorrent);

                    indexTorrent(webSearchResult, theTorrent, searchEngine);

                    torrentFile.delete();
                } catch (Throwable e) {
                    LOG.error("Error during torrent indexing", e);
                }

                finishSignal.countDown();
            }

            switch (state) {
            case TorrentDownloader.STATE_FINISHED:
            case TorrentDownloader.STATE_ERROR:
            case TorrentDownloader.STATE_DUPLICATE:
            case TorrentDownloader.STATE_CANCELLED:
                SearchResultMediator rp = SearchMediator.getResultPanelForGUID(new GUID(guid));
                if (rp != null) {
                    rp.decrementSearchCount();
                }
                finishSignal.countDown();
                break;
            }
        }

        private void matchResults(TOTorrent theTorrent) {

            if (!searchEngine.isEnabled()) {
                return;
            }

            final SearchResultMediator rp = SearchMediator.getResultPanelForGUID(new GUID(guid));

            // user closed the tab.
            if (rp == null || rp.isStopped()) {
                return;
            }

            rp.decrementSearchCount();

            SearchFilter filter = SearchMediator.getSearchFilterFactory().createFilter();

            TOTorrentFile[] fs = theTorrent.getFiles();
            for (int i = 0; i < fs.length; i++) {
                try {
                    final DeepSearchResult result = new DeepSearchResult(fs[i], webSearchResult, searchEngine, info);

                    if (!filter.allow(result))
                        continue;

                    boolean foundMatch = true;

                    String keywords = stringSanitize(result.getFileName() + " " + fs[i].getRelativePath()).toLowerCase();

                    for (String token : tokens) {
                        if (!keywords.contains(token)) {
                            foundMatch = false;
                            break;
                        }
                    }

                    if (foundMatch) {
                        GUIMediator.safeInvokeAndWait(new Runnable() {
                            public void run() {
                                SearchMediator.getSearchResultDisplayer().addQueryResult(guid, result, rp);
                            }
                        });
                    }
                } catch (Throwable e) {
                    LOG.error("Error analysing torrent file", e);
                }
            }
        }
    }

    public void shutdown() {
        DB.close();
    }

    public long getTotalTorrents() {
        List<List<Object>> query = DB.query("SELECT COUNT(*) FROM Torrents");
        return query.size() > 0 ? (Long) query.get(0).get(0) : 0;
    }

    public long getTotalFiles() {
        List<List<Object>> query = DB.query("SELECT COUNT(*) FROM Files");
        return query.size() > 0 ? (Long) query.get(0).get(0) : 0;
    }

    public void resetDB() {
        DB.reset();
        KNOWN_INFO_HASHES.clear();
    }

    private interface DeepTask extends Runnable, Comparable<DeepTask> {
        public int getOrder();
    }

    private class DownloadTorrentTask implements DeepTask {

        private final int order;
        private final byte[] guid;
        private final String query;
        private final SearchEngine searchEngine;
        private final WebSearchResult webSearchResult;
        private final SearchInformation info;

        public DownloadTorrentTask(int order, byte[] guid, String query, WebSearchResult webSearchResult, SearchEngine searchEngine, SearchInformation info) {
            this.order = order;
            this.guid = guid;
            this.query = query;
            this.searchEngine = searchEngine;
            this.webSearchResult = webSearchResult;
            this.info = info;
        }

        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public int compareTo(DeepTask o) {
            return Integer.valueOf(getOrder()).compareTo(Integer.valueOf(o.getOrder()));
        }

        @Override
        public void run() {

            SearchResultMediator rp = SearchMediator.getResultPanelForGUID(new GUID(guid));

            // user closed the tab.
            if (rp == null || rp.isStopped()) {
                return;
            }

            String saveDir = SearchSettings.SMART_SEARCH_DATABASE_FOLDER.getValue().getAbsolutePath();

            CountDownLatch finishSignal = new CountDownLatch(1);

            TorrentDownloaderFactory.create(new LocalSearchTorrentDownloaderListener(guid, query, webSearchResult, searchEngine, info, finishSignal), webSearchResult.getTorrentURI(), webSearchResult.getTorrentDetailsURL(), saveDir).start();

            try {
                finishSignal.await();
            } catch (InterruptedException e) {
                LOG.error("Error during await in DownloadTorrentTask", e);
            }
        }
    }

    private class CrawlYouTubePackage implements DeepTask {

        private final int order;
        private final byte[] guid;
        private final SearchEngine searchEngine;
        private final YouTubeSearchResult webSearchResult;
        private final SearchInformation info;

        public CrawlYouTubePackage(int order, byte[] guid, YouTubeSearchResult webSearchResult, SearchEngine searchEngine, SearchInformation info) {
            this.order = order;
            this.guid = guid;
            this.searchEngine = searchEngine;
            this.webSearchResult = webSearchResult;
            this.info = info;
        }

        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public int compareTo(DeepTask o) {
            return Integer.valueOf(getOrder()).compareTo(Integer.valueOf(o.getOrder()));
        }

        @Override
        public void run() {
            try {
                SearchResultMediator rp = SearchMediator.getResultPanelForGUID(new GUID(guid));

                // user closed the tab.
                if (rp == null || rp.isStopped()) {
                    return;
                }

                LinkCollector collector = LinkCollector.getInstance();
                LinkCrawler crawler = new LinkCrawler();
                crawler.setFilter(LinkFilterController.getInstance());
                crawler.crawl(readVideoUrl(webSearchResult.getYouTubeEntry()));
                crawler.waitForCrawling();

                final List<FilePackage> packages = new ArrayList<FilePackage>();

                for (CrawledLink link : crawler.getCrawledLinks()) {
                    CrawledPackage parent = PackageInfo.createCrawledPackage(link);
                    parent.setControlledBy(collector);
                    link.setParentNode(parent);
                    ArrayList<CrawledLink> links = new ArrayList<CrawledLink>();
                    links.add(link);
                    packages.add(createFilePackage(parent, links));
                }

                matchResults(packages);
            } catch (Throwable e) {
                LOG.error("Error crawling youtube: " + webSearchResult.getFilenameNoExtension(), e);
            }
        }

        private String readVideoUrl(YouTubeEntry entry) {
            String url = null;

            for (YouTubeEntryLink link : entry.link) {
                if (link.rel.equals("alternate")) {
                    url = link.href;
                }
            }

            url = url.replace("https://", "http://").replace("&feature=youtube_gdata", "");

            return url;
        }

        private FilePackage createFilePackage(final CrawledPackage pkg, ArrayList<CrawledLink> plinks) {
            FilePackage ret = FilePackage.getInstance();
            /* set values */
            ret.setName(pkg.getName());
            ret.setDownloadDirectory(pkg.getDownloadFolder());
            ret.setCreated(pkg.getCreated());
            ret.setExpanded(pkg.isExpanded());
            ret.setComment(pkg.getComment());
            synchronized (pkg) {
                /* add Children from CrawledPackage to FilePackage */
                ArrayList<DownloadLink> links = new ArrayList<DownloadLink>(pkg.getChildren().size());
                List<CrawledLink> pkgLinks = pkg.getChildren();
                if (plinks != null && plinks.size() > 0)
                    pkgLinks = new ArrayList<CrawledLink>(plinks);
                for (CrawledLink link : pkgLinks) {
                    /* extract DownloadLink from CrawledLink */
                    DownloadLink dl = link.getDownloadLink();
                    if (dl != null) {
                        /*
                         * change filename if it is different than original
                         * downloadlink
                         */
                        if (link.isNameSet())
                            dl.forceFileName(link.getName());
                        /* set correct enabled/disabled state */
                        //dl.setEnabled(link.isEnabled());
                        /* remove reference to crawledLink */
                        dl.setNodeChangeListener(null);
                        dl.setCreated(link.getCreated());
                        links.add(dl);
                        /* set correct Parent node */
                        dl.setParentNode(ret);
                    }
                }
                /* add all children to FilePackage */
                ret.getChildren().addAll(links);
            }
            return ret;
        }

        private void matchResults(List<FilePackage> packages) {

            if (!searchEngine.isEnabled()) {
                return;
            }

            final SearchResultMediator rp = SearchMediator.getResultPanelForGUID(new GUID(guid));

            // user closed the tab.
            if (rp == null || rp.isStopped()) {
                return;
            }

            rp.decrementSearchCount();

            SearchFilter filter = SearchMediator.getSearchFilterFactory().createFilter();

            for (FilePackage p : packages) {
                try {
                    final YouTubePackageItemSearchResult result = new YouTubePackageItemSearchResult(webSearchResult, p, searchEngine, info);

                    //youtube mp3 filter
                    if (p.getChildren().get(0).getFileOutput().endsWith(".mp3")) {
                        continue;
                    }

                    if (!filter.allow(result))
                        continue;

                    GUIMediator.safeInvokeAndWait(new Runnable() {
                        public void run() {
                            SearchMediator.getSearchResultDisplayer().addQueryResult(guid, result, rp);
                        }
                    });
                } catch (Throwable e) {
                    LOG.error("Error analysing youtube package", e);
                }
            }
        }
    }

    private class IndexTorrentTask implements Runnable {

        @Override
        public void run() {
            try {
                int n = 0;

                ArrayList<IndexTorrentElement> list = new ArrayList<IndexTorrentElement>();
                while (n < 1000) {
                    IndexTorrentElement e = INDEX_TORRENT_QUEUE.poll();
                    if (e != null) {
                        list.add(e);
                        n += e.files.length;
                    } else {
                        break;
                    }
                }

                indexElements(list);

                if (n > 0) {
                    Thread.sleep(1000);
                }
            } catch (Throwable e) {
                LOG.error("General error in torrent index task", e);
            }
        }

        private void indexElements(ArrayList<IndexTorrentElement> list) {
            // disable lucene indexing
            FullTextLucene2.enableIndexing("FILES", false);
            for (int i = 0; i < list.size(); i++) {
                indexElement(list.get(i), i == list.size() - 1);
            }
        }

        private void indexElement(IndexTorrentElement indexTorrentElement, boolean enableIndexing) {
            TorrentDBPojo torrent = indexTorrentElement.torrent;
            TorrentFileDBPojo[] files = indexTorrentElement.files;

            String torrentJSON = JSON_ENGINE.toJson(torrent);

            int torrentID = DB.insert("INSERT INTO Torrents (infoHash, timestamp, torrentName, seeds, indexed, json) VALUES (?, ?, LEFT(?, 10000), ?, ?, ?)", torrent.hash, System.currentTimeMillis(), torrent.fileName.toLowerCase(), torrent.seeds, false, torrentJSON);

            for (int i = 0; i < files.length; i++) {
                if (enableIndexing && i == indexTorrentElement.files.length - 1) {
                    // enable lucene indexing
                    FullTextLucene2.enableIndexing("FILES", true);
                }

                TorrentFileDBPojo file = files[i];

                String fileJSON = JSON_ENGINE.toJson(file);
                String keywords = stringSanitize(torrent.fileName + " " + file.relativePath).toLowerCase();

                DB.insert("INSERT INTO Files (torrentId, fileName, json, keywords) VALUES (?, LEFT(?, 10000), ?, ?)", torrentID, file.relativePath, fileJSON, keywords);
            }

            DB.update("UPDATE Torrents SET indexed=? WHERE torrentId=?", true, torrentID);
        }
    }

    private static class IndexTorrentElement {
        public final TorrentDBPojo torrent;
        public final TorrentFileDBPojo files[];

        public IndexTorrentElement(TorrentDBPojo torrent, TorrentFileDBPojo[] files) {
            this.torrent = torrent;
            this.files = files;
        }
    }
}
