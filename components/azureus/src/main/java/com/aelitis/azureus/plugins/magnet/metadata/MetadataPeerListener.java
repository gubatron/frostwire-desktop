package com.aelitis.azureus.plugins.magnet.metadata;

import org.gudy.azureus2.core3.peer.PEPeer;
import org.gudy.azureus2.core3.peer.PEPeerListener;
import org.gudy.azureus2.core3.peer.impl.transport.PEPeerTransportProtocol;

import com.aelitis.azureus.core.peermanager.piecepicker.util.BitFlags;

public class MetadataPeerListener implements PEPeerListener {

    @Override
    public void stateChanged(PEPeer peer, int new_state) {
        if (new_state == PEPeer.READY_TO_ASK_FOR_METADATA) {
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
