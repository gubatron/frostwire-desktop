package com.frostwire.gui.bittorrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.LinkCrawler;

import org.jdownloader.controlling.filter.LinkFilterController;

public final class StreamUrlCrawler {

    private final ExecutorService executor;

    private String currentUrl;

    private static StreamUrlCrawler instance;

    public StreamUrlCrawler instance() {
        if (instance == null) {
            instance = new StreamUrlCrawler();
        }
        return instance;
    }

    private StreamUrlCrawler() {
        executor = Executors.newSingleThreadExecutor();
    }

    public void play(String url) {
        currentUrl = url;
        executor.execute(new PlayTask(url));
    }

    private final class PlayTask implements Runnable {

        private final String url;

        public PlayTask(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            LinkCrawler crawler = new LinkCrawler();
            crawler.setFilter(LinkFilterController.getInstance());
            crawler.crawl(url);
            crawler.waitForCrawling();

            for (CrawledLink link : crawler.getCrawledLinks()) {
                String url = link.getDownloadLink().getDownloadURL();
                if (url.equals(currentUrl)) {
                    System.out.println(url);
                }
            }
        }
    }
}
