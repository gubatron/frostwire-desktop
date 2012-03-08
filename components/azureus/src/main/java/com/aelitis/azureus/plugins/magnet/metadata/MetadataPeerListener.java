/*
 * Created by  Alden Torres (aldenml)
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

package com.aelitis.azureus.plugins.magnet.metadata;

import org.gudy.azureus2.core3.peer.PEPeer;
import org.gudy.azureus2.core3.peer.PEPeerListener;
import org.gudy.azureus2.core3.peer.impl.transport.PEPeerTransportProtocol;

import com.aelitis.azureus.core.peermanager.piecepicker.util.BitFlags;

public class MetadataPeerListener implements PEPeerListener {

    public MetadataPeerListener() {
    }

    /**
     * @see {@link MetadataPeerManagerAdapter#addPeer(PEPeer)} 
     */
    @Override
    public void stateChanged(PEPeer peer, int new_state) {
        if (new_state == PEPeer.READY_FOR_PEER_METADATA_REQUEST) {
            if (peer instanceof PEPeerTransportProtocol) {
                requestMetadata((PEPeerTransportProtocol) peer);
            }
        }
    }

    @Override
    public void sentBadChunk(PEPeer peer, int piece_num, int total_bad_chunks) {
    }

    @Override
    public void removeAvailability(PEPeer peer, BitFlags peerHavePieces) {
    }

    @Override
    public void addAvailability(PEPeer peer, BitFlags peerHavePieces) {
    }

    private void requestMetadata(final PEPeerTransportProtocol peer) {
        if (peer.supportsUTMETADATA()) {
            peer.sendMetadataRequest(0);
        }
    }
}
