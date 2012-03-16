package jd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jd.controlling.IOEQ;
import jd.controlling.downloadcontroller.DownloadController;
import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.controlling.downloadcontroller.SingleDownloadController;
import jd.controlling.linkcollector.LinkCollectingJob;
import jd.controlling.linkcollector.LinkCollector;
import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.linkcrawler.LinkCrawler;
import jd.controlling.linkcrawler.LinkCrawlerHandler;
import jd.controlling.packagecontroller.AbstractNode;
import jd.plugins.FilePackage;

public class Launcher {

    public static void main(String[] args) throws Exception {
        
        LinkCollector lcollector = LinkCollector.getInstance();
        LinkCrawler lcrawler = lcollector.addCrawlerJob(new LinkCollectingJob("http://www.youtube.com/watch?v=EtsXgODHMWk&feature=g-vrec&context=G242f161RVAAAAAAAAAQ"));
        
        lcrawler.waitForCrawling();
        
        System.out.println(lcrawler.crawledLinksFound());
        
        List<CrawledPackage> ps = new ArrayList<CrawledPackage>();
        
        for (CrawledPackage p : lcollector.getPackages()) {
            System.out.println(p.getName());
            for (CrawledLink l : p.getChildren()) {
                System.out.println(l);
            }
            
            ps.add(p);
        }

        
        start(new ArrayList<AbstractNode>(ps));
        
        System.in.read();
    }

    protected static void download(CrawledLink link) {
        SingleDownloadController d = new SingleDownloadController(link.getDownloadLink(), null);
        d.run();
    }

    private static void start(final List<AbstractNode> values) {
        IOEQ.add(new Runnable() {

            public void run() {
                ArrayList<FilePackage> fpkgs = new ArrayList<FilePackage>();
                ArrayList<CrawledLink> clinks = new ArrayList<CrawledLink>();
                for (AbstractNode node : values) {
                    if (node instanceof CrawledPackage) {
                        /* first convert all CrawledPackages to FilePackages */
                        ArrayList<CrawledLink> links = new ArrayList<CrawledLink>(((CrawledPackage) node).getChildren());
                        ArrayList<FilePackage> packages = LinkCollector.getInstance().removeAndConvert(links);
                        if (packages != null) fpkgs.addAll(packages);
                    } else if (node instanceof CrawledLink) {
                        /* collect all CrawledLinks */
                        clinks.add((CrawledLink) node);
                    }
                }
                /* convert all selected CrawledLinks to FilePackages */
                ArrayList<FilePackage> frets = LinkCollector.getInstance().removeAndConvert(clinks);
                if (frets != null) fpkgs.addAll(frets);
                /* add the converted FilePackages to DownloadController */
                DownloadController.getInstance().addAllAt(fpkgs, -(fpkgs.size() + 10));
                //if (autostart) {
                    IOEQ.add(new Runnable() {

                        public void run() {
                            /* start DownloadWatchDog if wanted */
                            DownloadWatchDog.getInstance().startDownloads();
                        }

                    }, true);
                //}
            }

        }, true);
    }
}
