/*
 * Created by  Alden Torres (aldenml)
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

package com.aelitis.azureus.plugins.magnet.metadata;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.gudy.azureus2.core3.peer.PEPeerManager;
import org.gudy.azureus2.core3.peer.PEPeerManagerFactory;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.torrent.impl.TOTorrentMetadata;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncer;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerFactory;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.UrlUtils;

import com.aelitis.azureus.plugins.magnet.MagnetPluginProgressListener;

/**
 * FrostWire: This is how we use the UT_METADATA Extension on FrostWire.
 * This class basically tries to request the torrent metadata from all trackers specified by the magnet url (tr= params)
 * 
 * We create sort of a virtual TOTorrent only to retrieve the metadata from peers connected to the torrent on a tracker.
 * 
 * We start off by using only ONE tracker per torrent, in parallel. By this I mean, if a magnet URL has say 3 trackers we go ahead
 * and create 3 virtual torrents, each with a different announce url set of only one URL (that tracker), and then we request
 * the metadata from all of them at the same time and once we get the full metadata from any of the 3, we kill the other two,
 * close all the connections, etc.
 *
 * Once we have the complete torrent metadata from the first one we build the real torrent, and we put all the announce urls in it.
 * 
 * @see MetadataPeerRequester#requestSupport(URL, URL[], CountDownLatch)
 * 
 */
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

            String peerInternalHostPort = getPeerInternalHostPort();
            String peerInternalIp = null;
            int peerInternalPort = -1;
            
            if (peerInternalHostPort != null) {
                peerInternalIp = peerInternalHostPort.split(":")[0];
                peerInternalPort = Integer.parseInt(peerInternalHostPort.split(":")[1]);
            }
            
            List<Torrent> torrents = new ArrayList<Torrent>();
            for (URL tracker : trackers) {
                Torrent torrent = requestSupport(tracker, trackers, peerInternalIp, peerInternalPort, signal);
                if (torrent != null) {
                    torrents.add(torrent);
                }
            }

            signal.await(timeout, TimeUnit.MILLISECONDS);
            //signal.await();

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

    /**
     * We used CountDownLatch and not Azureus monitors because we don't feel fully comfortable with them,
     * @parg feel free to change this syncing mechanism if you don't feel a CountDownLatch is good enough,
     * still I feel it'd be overkill to do so, since this is not really that intensive, more likely you're only going to have
     * a handful of threads talking to trackers.
     * 
     * @param tracker
     * @param trackers
     * @param signal
     * @return
     */
    private Torrent requestSupport(URL tracker, final URL[] trackers, String peerInternalHost, int peerInternalPort, final CountDownLatch signal) {
        try {

            listener.reportActivity("Try to request metadata from peers in tracker: " + tracker);

            Torrent torrent = new Torrent(hash, "", new URL[] { tracker }, peerInternalHost, peerInternalPort) {
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

            //note that we use new implementations of TRTrackerAnnouncerListener, DiskManager, PEPeerManagerAdapter, etc. 
            //We looked at DownloadManagerImpl to do this.
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
            
            //add internal peer host:port if available.
            if (peerInternalHost!=null && peerInternalPort != -1) {
                peerManager.addPeer(peerInternalHost, peerInternalPort, 0, false, new HashMap());
            }
            
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
    
    private String getPeerInternalHostPort() {

        for (String part : args.split("&")) {
            if (part.startsWith("iipp=")) {
                    return convertHexToIPPort(part.substring("iipp=".length())); 
            }
        }

        return null;
    }
    
    public static String convertIPPortToHex(String ip, int port) {
        String[] split_ip = ip.split("\\.");
        byte[] octets_n_port = new byte[6];

        int i = 0;
        for (String octet : split_ip){
            octets_n_port[i++]= (byte) Integer.parseInt(octet);
        }
        
        byte[] port_bytes = ByteUtils.smallIntToByteArray(port);
        
        octets_n_port[4]=port_bytes[0];
        octets_n_port[5]=port_bytes[1];
        
        return ByteUtils.encodeHex(octets_n_port);
    }

    // FFFFFFFFFFFF -> 255.255.255.255:65535
    public static String convertHexToIPPort(String ipPortInHex) {
        
        if (ipPortInHex.length()!=12) {
            return null;
        }

        byte[] octets = ByteUtils.decodeHex(ipPortInHex);
        
        StringBuilder ipPortion = new StringBuilder();
        
        ipPortion.append(octets[0] & 0xFF);
        ipPortion.append(".");
        ipPortion.append(octets[1] & 0xFF);
        ipPortion.append(".");
        ipPortion.append(octets[2] & 0xFF);
        ipPortion.append(".");
        ipPortion.append(octets[3] & 0xFF);
        ipPortion.append(":");

        int port = ByteUtils.byteArrayToSmallInt(octets, 4);
        ipPortion.append(port);
        
        return ipPortion.toString();
    }

    private static class Torrent extends TOTorrentMetadata {

        private TRTrackerAnnouncer trackerAnnouncer;
        private PEPeerManager peerManager;
        private String peerInternalIP;
        private int peerInternalPort;

        public Torrent(byte[] hash, String displayName, URL[] trackers, String peerInternalIP, int peerInternalPort) {
            super(hash, displayName, trackers);
            this.peerInternalIP = peerInternalIP;
            this.peerInternalPort = peerInternalPort;
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
        
        @Override
        public Map serialiseToMap() throws TOTorrentException {
           Map map = super.serialiseToMap();

            //add a little something to the torrent map :)
           if (peerInternalIP != null && peerInternalPort != -1) {
               map.put("peerInternalIP", peerInternalIP);
               map.put("peerInternalPort", peerInternalPort);
           }
           
           return map;
        }
    }
}
