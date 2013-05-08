/*
 * Created by  Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
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

import java.net.URL;

import org.gudy.azureus2.core3.peer.PEPeerManager;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncer;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerListener;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerResponse;

/**
 * 
 * We fetch peers here using the PEPeerManager from the tracker response.
 *
 */
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
