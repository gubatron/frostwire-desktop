package com.aelitis.azureus.plugins.magnet.metadata;

import org.gudy.azureus2.core3.peer.PEPeer;
import org.gudy.azureus2.core3.peer.PEPeerListener;
import org.gudy.azureus2.core3.peer.impl.transport.PEPeerTransportProtocol;
import org.gudy.azureus2.core3.torrent.TOTorrent;

import com.aelitis.azureus.core.peermanager.piecepicker.util.BitFlags;

public class MetadataPeerListener implements PEPeerListener {

    @Override
    public void stateChanged(PEPeer peer, int new_state) {
        if (new_state == PEPeer.READY_TO_ASK_FOR_METADATA) {
            //if (peer instanceof PEPeerTransportProtocol)
            //tryMetadata(null, (PEPeerTransportProtocol)peer);
        }
        /*
         static boolean metatada_requested = false;
        
        private static void tryMetadata(TOTorrent torrent, final PEPeerTransportProtocol peer) {
        if (metatada_requested) {
            return;
        }
        
        if (peer.supportsUTMETADATA()) {
            metatada_requested = true;
            peer.sendMetadataRequest(0);
        }
        
        }
         */
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
}
