package com.aelitis.azureus.plugins.magnet.metadata;

import java.net.URL;

import org.gudy.azureus2.core3.peer.PEPeerManager;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncer;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerListener;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerResponse;

public class MetadataTrackerAnnouncerListener implements TRTrackerAnnouncerListener {

    private PEPeerManager peerManager;

    public PEPeerManager getPeerManager() {
        return peerManager;
    }

    public void setPeerManager(PEPeerManager peerManager) {
        this.peerManager = peerManager;
    }

    public void receivedTrackerResponse(TRTrackerAnnouncerResponse response) {
        if (peerManager != null) {
            peerManager.processTrackerResponse(response);
        }
    }

    public void urlChanged(final TRTrackerAnnouncer announcer, final URL old_url, URL new_url, boolean explicit) {
    }

    public void urlRefresh() {
    }
}
