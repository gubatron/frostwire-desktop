package com.aelitis.azureus.plugins.magnet.metadata;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.gudy.azureus2.core3.peer.PEPeerManager;
import org.gudy.azureus2.core3.peer.PEPeerManagerFactory;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.torrent.impl.TOTorrentMetadata;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncer;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerFactory;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.UrlUtils;

import com.aelitis.azureus.plugins.magnet.MagnetPluginProgressListener;

public class MetadataPeerRequester {

    private final MagnetPluginProgressListener listener;
    private final byte[] hash;
    private final String args;
    private final long timeout; // milliseconds

    private byte[] torrentBytes;

    public MetadataPeerRequester(MagnetPluginProgressListener listener, byte[] hash, String args, long timeout) {
        this.listener = listener;
        this.hash = hash;
        this.args = args;
        this.timeout = timeout;
    }

    public byte[] request() {
        try {

            final CountDownLatch signal = new CountDownLatch(1);

            URL[] trackers = getTrackers();
            if (trackers.length == 0) {
                return null;// nothing to do
            }

            List<Torrent> torrents = new ArrayList<Torrent>();
            for (URL tracker : trackers) {
                Torrent torrent = requestSupport(tracker, trackers, signal);
                if (torrent != null) {
                    torrents.add(torrent);
                }
            }

            //signal.await(timeout, TimeUnit.MILLISECONDS);
            signal.await();

            for (Torrent t : torrents) {
                t.stop();
            }

            if (torrentBytes != null) {
                listener.reportCompleteness(100);
            }

            return torrentBytes;

        } catch (Throwable e) {
            Debug.printStackTrace(e);
        }

        return null;
    }

    private Torrent requestSupport(URL tracker, final URL[] trackers, final CountDownLatch signal) {
        try {

            listener.reportActivity("Try to request metadata from peers in tracker: " + tracker);

            Torrent torrent = new Torrent(hash, "", new URL[] { tracker }) {
                @Override
                public void notifyComplete() {
                    try {
                        if (torrentBytes == null) {
                            setAnnounceUrlGroup(trackers);
                            torrentBytes = serialiseToByteArray();
                        }
                    } catch (TOTorrentException e) {
                        Debug.printStackTrace(e);
                    }
                    signal.countDown();
                }
            };

            MetadataTrackerAnnouncerListener tracker_client_listener = new MetadataTrackerAnnouncerListener();

            TRTrackerAnnouncer tracker_client = TRTrackerAnnouncerFactory.create(torrent, null);
            tracker_client.addListener(tracker_client_listener);
            tracker_client.setAnnounceDataProvider(new MetadataTrackerAnnouncerDataProvider());
            torrent.setTrackerAnnouncer(tracker_client);

            MetadataPeerManagerAdapter peerManagerAdapter = new MetadataPeerManagerAdapter(new MetadataPeerListener());
            PEPeerManager peerManager = PEPeerManagerFactory.create(tracker_client.getPeerId(), peerManagerAdapter, new MetadataDiskManager(torrent));
            torrent.setPeerManager(peerManager);
            peerManagerAdapter.setPeerManager(peerManager);

            tracker_client_listener.setPeerManager(peerManager);

            peerManager.start();
            peerManager.setSuperSeedMode(true); // artificial state to hack inner core

            tracker_client.update(true);

            return torrent;

        } catch (Throwable e) {
            Debug.printStackTrace(e);
        }

        return null;
    }

    private URL[] getTrackers() {
        List<URL> trackers = new ArrayList<URL>();
        for (String part : args.split("&")) {
            if (part.startsWith("tr=")) {
                try {
                    trackers.add(new URL(UrlUtils.decode(part.substring(3))));
                } catch (MalformedURLException e) {
                    Debug.printStackTrace(e);
                }
            }
        }

        return trackers.toArray(new URL[0]);
    }

    private static class Torrent extends TOTorrentMetadata {

        private TRTrackerAnnouncer trackerAnnouncer;
        private PEPeerManager peerManager;

        public Torrent(byte[] hash, String displayName, URL[] trackers) {
            super(hash, displayName, trackers);
        }

        public TRTrackerAnnouncer getTrackerAnnouncer() {
            return trackerAnnouncer;
        }

        public void setTrackerAnnouncer(TRTrackerAnnouncer trackerAnnouncer) {
            this.trackerAnnouncer = trackerAnnouncer;
        }

        public PEPeerManager getPeerManager() {
            return peerManager;
        }

        public void setPeerManager(PEPeerManager peerManager) {
            this.peerManager = peerManager;
        }

        public void stop() {
            try {
                if (trackerAnnouncer != null) {
                    trackerAnnouncer.stop(false);
                }
                if (peerManager != null) {
                    peerManager.stopAll();
                }
            } catch (Throwable e) {
                Debug.printStackTrace(e);
            }
        }
    }
}
