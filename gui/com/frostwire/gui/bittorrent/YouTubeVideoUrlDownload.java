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

package com.frostwire.gui.bittorrent;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.linkcrawler.LinkCrawler;
import jd.controlling.linkcrawler.PackageInfo;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.jdownloader.controlling.filter.LinkFilterController;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class YouTubeVideoUrlDownload implements BTDownload {

    private static final Log LOG = LogFactory.getLog(YouTubeVideoUrlDownload.class);

    private static final String STATE_CRAWLING = I18n.tr("Crawling");
    private static final String STATE_FINISHED = I18n.tr("Finished");
    private static final String STATE_ERROR = I18n.tr("Error");
    private static final String STATE_STOPPED = I18n.tr("Stopped");

    private final String videoUrl;
    private final Date dateCreated;

    private String _state;
    private int progress;

    public YouTubeVideoUrlDownload(String videoUrl) {
        if (!videoUrl.startsWith("http://")) {
            videoUrl = "http://" + videoUrl;
        }
        this.videoUrl = videoUrl;
        this.dateCreated = new Date();
        _state = STATE_CRAWLING;
        start();
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public long getSize(boolean update) {
        return 0;
    }

    @Override
    public String getDisplayName() {
        return videoUrl;
    }

    @Override
    public boolean isResumable() {
        return false;
    }

    @Override
    public boolean isPausable() {
        return false;
    }

    @Override
    public boolean isCompleted() {
        return _state.equals(STATE_FINISHED);
    }

    @Override
    public int getState() {
        return -1;
    }

    @Override
    public void remove() {
        _state = STATE_STOPPED;
    }

    @Override
    public void pause() {
    }

    @Override
    public File getSaveLocation() {
        return null;
    }

    @Override
    public void resume() {
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public String getStateString() {
        return _state;
    }

    @Override
    public long getBytesReceived() {
        return 0;
    }

    @Override
    public long getBytesSent() {
        return 0;
    }

    @Override
    public double getDownloadSpeed() {
        return 0;
    }

    @Override
    public double getUploadSpeed() {
        return 0;
    }

    @Override
    public long getETA() {
        return 0;
    }

    @Override
    public DownloadManager getDownloadManager() {
        return null;
    }

    @Override
    public String getPeersString() {
        return "";
    }

    @Override
    public String getSeedsString() {
        return "";
    }

    @Override
    public boolean isDeleteTorrentWhenRemove() {
        return false;
    }

    @Override
    public void setDeleteTorrentWhenRemove(boolean deleteTorrentWhenRemove) {
    }

    @Override
    public boolean isDeleteDataWhenRemove() {
        return false;
    }

    @Override
    public void setDeleteDataWhenRemove(boolean deleteDataWhenRemove) {
    }

    @Override
    public String getHash() {
        return null;
    }

    @Override
    public String getSeedToPeerRatio() {
        return "";
    }

    @Override
    public String getShareRatio() {
        return "";
    }

    @Override
    public boolean isPartialDownload() {
        return false;
    }

    @Override
    public void updateDownloadManager(DownloadManager downloadManager) {
    }

    @Override
    public Date getDateCreated() {
        return dateCreated;
    }

    private void start() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    LinkCollector collector = LinkCollector.getInstance();
                    LinkCrawler crawler = new LinkCrawler();
                    crawler.setFilter(LinkFilterController.getInstance());
                    crawler.crawl(videoUrl);
                    crawler.waitForCrawling();

                    if (_state.equals(STATE_STOPPED)) {
                        return;
                    }

                    _state = STATE_FINISHED;
                    progress = 100;

                    final List<FilePackage> packages = new ArrayList<FilePackage>();

                    for (CrawledLink link : crawler.getCrawledLinks()) {
                        CrawledPackage parent = PackageInfo.createCrawledPackage(link);
                        parent.setControlledBy(collector);
                        link.setParentNode(parent);
                        ArrayList<CrawledLink> links = new ArrayList<CrawledLink>();
                        links.add(link);
                        packages.add(createFilePackage(parent, links));
                    }

                    /*
                    for (CrawledPackage pkg : new ArrayList<CrawledPackage>(collector.getPackages())) {
                        for (CrawledLink link : new ArrayList<CrawledLink>(pkg.getChildren())) {
                            ArrayList<CrawledLink> links = new ArrayList<CrawledLink>();
                            links.add(link);
                            packages.addAll(collector.removeAndConvert(links));
                        }
                    }*/

                    GUIMediator.safeInvokeAndWait(new Runnable() {
                        public void run() {
                            try {

                                PartialYouTubePackageDialog dlg = new PartialYouTubePackageDialog(GUIMediator.getAppFrame(), videoUrl, packages);

                                dlg.setVisible(true);
                                if (dlg.getFilesSelection() != null) {
                                    for (FilePackage filePackage : dlg.getFilesSelection()) {
                                        //BTDownloadMediator.instance().openYouTubeItem(filePackage);
                                    }
                                }
                            } catch (Throwable e) {
                                LOG.error("Error reading youtube package:" + e.getMessage(), e);
                                _state = STATE_ERROR;
                            }
                        }
                    });

                    GUIMediator.safeInvokeAndWait(new Runnable() {
                        public void run() {
                            BTDownloadMediator.instance().remove(YouTubeVideoUrlDownload.this);
                        }
                    });
                } catch (Throwable e) {
                    LOG.error("Error crawling youtube: " + videoUrl, e);
                    _state = STATE_ERROR;
                }
            }
        });
        t.setDaemon(true);
        t.setName("YouTube Crawl: " + videoUrl);
        t.start();
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
}
