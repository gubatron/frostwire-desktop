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

package com.frostwire.search;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class SearchManagerImpl implements SearchManager {

    private static final Logger LOG = LoggerFactory.getLogger(SearchManagerImpl.class);

    private static final int DEFAULT_NTHREADS = 4;

    private final ExecutorService executor;
    private final List<SearchTask> tasks;

    private SearchManagerListener listener;

    public SearchManagerImpl(int nThreads) {
        this.executor = Executors.newFixedThreadPool(nThreads);
        this.tasks = Collections.synchronizedList(new LinkedList<SearchTask>());
    }

    public SearchManagerImpl() {
        this(DEFAULT_NTHREADS);
    }

    @Override
    public void registerListener(SearchManagerListener listener) {
        this.listener = listener;
    }

    @Override
    public void perform(SearchPerformer performer) {
        if (performer != null) {
            performer.registerListener(new PerformerResultListener(this));

            SearchTask task = new PerformTask(this, performer);

            tasks.add(task);
            executor.execute(task);
        } else {
            LOG.warn("Search performer is null, review your logic");
        }
    }

    @Override
    public void stop() {
        stopTasks(0);
    }

    @Override
    public void stop(long token) {
        stopTasks(token);
    }

    @Override
    public boolean shutdown(long timeout, TimeUnit unit) {
        stop();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeout, unit)) {
                executor.shutdownNow();
                // wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(timeout, unit)) {
                    LOG.error("Pool did not terminate");
                    return false;
                }
            }
        } catch (InterruptedException ie) {
            // (re-)cancel if current thread also interrupted
            executor.shutdownNow();
            // preserve interrupt status
            Thread.currentThread().interrupt();
        }

        return tasks.isEmpty();
    }

    protected void onResults(SearchPerformer performer, List<? extends SearchResult> results) {
        try {
            if (listener != null) {
                listener.onResults(performer, results);
            }
        } catch (Throwable e) {
            LOG.warn("Error sending results back to receiver: " + e.getMessage());
        }
    }

    protected void onFinished() {
        try {
            if (listener != null) {
                listener.onFinished();
            }
        } catch (Throwable e) {
            LOG.warn("Error sending results back to receiver: " + e.getMessage());
        }
    }

    private void stopTasks(long token) {
        synchronized (tasks) {
            Iterator<SearchTask> it = tasks.iterator();
            while (it.hasNext()) {
                SearchTask task = it.next();
                if (token == 0 || task.performer.getToken() == token) {
                    task.stop();
                    it.remove();
                }
            }
        }
    }

    private void crawl(SearchPerformer performer, CrawlableSearchResult sr) {
        if (performer != null && !performer.isStopped()) {
            try {
                SearchTask task = new CrawlTask(this, performer, sr);
                tasks.add(task);
                executor.execute(task);
            } catch (Throwable e) {
                LOG.warn("Error scheduling crawling of search result: " + sr);
            }
        } else {
            LOG.warn("Search performer is null or stopped, review your logic");
        }
    }

    private void checkIfFinished(SearchPerformer performer) {
        SearchTask pendingTask = null;
        synchronized (tasks) {
            Iterator<SearchTask> it = tasks.iterator();

            while (it.hasNext() && pendingTask == null) {
                SearchTask task = it.next();
                if (task.getToken() == performer.getToken()) {
                    pendingTask = task;
                }
            }
        }
        if (pendingTask == null) {
            onFinished();
        }
    }

    private static final class PerformerResultListener implements SearchListener {

        private final SearchManagerImpl manager;

        public PerformerResultListener(SearchManagerImpl manager) {
            this.manager = manager;
        }

        @Override
        public void onResults(SearchPerformer performer, List<? extends SearchResult> results) {
            List<SearchResult> list = new LinkedList<SearchResult>();

            for (SearchResult sr : results) {
                if (sr instanceof CrawlableSearchResult) {
                    CrawlableSearchResult csr = (CrawlableSearchResult) sr;

                    if (csr.isComplete()) {
                        list.add(sr);
                    }

                    manager.crawl(performer, csr);
                } else {
                    list.add(sr);
                }
            }

            if (!list.isEmpty()) {
                manager.onResults(performer, list);
            }
        }
    }

    private static abstract class SearchTask implements Runnable {

        protected final SearchManagerImpl manager;
        protected final SearchPerformer performer;

        public SearchTask(SearchManagerImpl manager, SearchPerformer performer) {
            this.manager = manager;
            this.performer = performer;
        }

        public long getToken() {
            return performer.getToken();
        }

        public void stop() {
            performer.stop();
        }
    }

    private static final class PerformTask extends SearchTask {

        public PerformTask(SearchManagerImpl manager, SearchPerformer performer) {
            super(manager, performer);
        }

        @Override
        public void run() {
            try {
                if (!performer.isStopped()) {
                    performer.perform();
                }
            } catch (Throwable e) {
                LOG.warn("Error performing search: " + performer + ", e=" + e.getMessage());
            } finally {
                manager.tasks.remove(this);
                manager.checkIfFinished(performer);
            }
        }
    }

    private static final class CrawlTask extends SearchTask {

        private final CrawlableSearchResult sr;

        public CrawlTask(SearchManagerImpl manager, SearchPerformer performer, CrawlableSearchResult sr) {
            super(manager, performer);
            this.sr = sr;
        }

        @Override
        public void run() {
            try {
                if (!performer.isStopped()) {
                    performer.crawl(sr);
                }
            } catch (Throwable e) {
                LOG.warn("Error performing crawling of: " + sr);
            } finally {
                manager.tasks.remove(this);
                manager.checkIfFinished(performer);
            }
        }
    }
}
