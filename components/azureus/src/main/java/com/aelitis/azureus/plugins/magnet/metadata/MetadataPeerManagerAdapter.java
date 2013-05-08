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

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gudy.azureus2.core3.disk.DiskManagerReadRequest;
import org.gudy.azureus2.core3.disk.DiskManagerReadRequestListener;
import org.gudy.azureus2.core3.logging.LogRelation;
import org.gudy.azureus2.core3.peer.PEPeer;
import org.gudy.azureus2.core3.peer.PEPeerManager;
import org.gudy.azureus2.core3.peer.PEPeerManagerAdapter;
import org.gudy.azureus2.core3.peer.PEPiece;
import org.gudy.azureus2.core3.peer.impl.PEPeerControl;
import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.tracker.client.TRTrackerScraperResponse;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.utils.Utilities;
import org.gudy.azureus2.pluginsimpl.local.PluginInitializer;

import com.aelitis.azureus.core.networkmanager.impl.tcp.TCPNetworkManager;
import com.aelitis.azureus.core.peermanager.PeerManagerRegistration;
import com.aelitis.azureus.plugins.upnp.UPnPPlugin;
import com.aelitis.azureus.plugins.upnp.UPnPPluginService;

/**
 * 
 * @see MetadataPeerManagerAdapter#addPeer(PEPeer) for the meat of this
 *
 */
public class MetadataPeerManagerAdapter implements PEPeerManagerAdapter {

    private final MetadataPeerListener peerListener;
    private final PeerManagerRegistration peerMangerRegistration;
    private final String externalIp;
    private final Set<String> manualIps;
    private final int localTcpPort;

    private PEPeerManager peerManager;

    public MetadataPeerManagerAdapter(MetadataPeerListener peerListener) {
        this.peerListener = peerListener;
        this.peerMangerRegistration = new PeerManagerRegistration() {

            @Override
            public void unregister() {
            }

            @Override
            public void removeLink(String link) {
            }

            @Override
            public TOTorrentFile getLink(String link) {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void deactivate() {
            }

            @Override
            public void addLink(String link, TOTorrentFile target) throws Exception {
            }

            @Override
            public void activate(PEPeerControl peer_control) {
            }
        };

        this.externalIp = getExternalIp();
        this.localTcpPort = TCPNetworkManager.getSingleton().getTCPListeningPortNumber();
        this.manualIps = new HashSet<String>();
    }

    public PEPeerManager getPeerManager() {
        return peerManager;
    }

    public void setPeerManager(PEPeerManager peerManager) {
        this.peerManager = peerManager;
    }

    @Override
    public void statsRequest(PEPeer originator, Map request, Map reply) {
    }

    @Override
    public void setTrackerRefreshDelayOverrides(int percent) {
    }

    @Override
    public void setStateSeeding(boolean never_downloaded) {
    }

    @Override
    public void setStateFinishing() {
    }

    @Override
    public void restartDownload(boolean forceRecheck) {
    }

    @Override
    public void removePiece(PEPiece piece) {
    }

    @Override
    public void removePeer(PEPeer peer) {
        //System.out.println(peer.getIp() + ":" + peer.getPort() + " removed");
    }

    @Override
    public void protocolBytesSent(PEPeer peer, int bytes) {
    }

    @Override
    public void protocolBytesReceived(PEPeer peer, int bytes) {
    }

    @Override
    public void priorityConnectionChanged(boolean added) {
    }

    @Override
    public void permittedSendBytesUsed(int bytes) {
    }

    @Override
    public void permittedReceiveBytesUsed(int bytes) {
    }

    @Override
    public boolean isPeriodicRescanEnabled() {
        return false;
    }

    @Override
    public boolean isPeerSourceEnabled(String peer_source) {
        return false;
    }

    @Override
    public boolean isPeerExchangeEnabled() {
        return true;
    }

    @Override
    public boolean isNATHealthy() {
        return false;
    }

    @Override
    public boolean isExtendedMessagingEnabled() {
        return true;
    }

    @Override
    public boolean hasPriorityConnection() {
        return false;
    }

    @Override
    public int getUploadRateLimitBytesPerSecond() {
        return 0;
    }

    @Override
    public TRTrackerScraperResponse getTrackerScrapeResponse() {
        return null;
    }

    @Override
    public String getTrackerClientExtensions() {
        return null;
    }

    @Override
    public byte[][] getSecrets(int crypto_level) {
        return null;
    }

    @Override
    public long getRandomSeed() {
        return 0;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public int getPermittedBytesToSend() {
        return 0;
    }

    @Override
    public int getPermittedBytesToReceive() {
        return 0;
    }

    @Override
    public PeerManagerRegistration getPeerManagerRegistration() {
        return peerMangerRegistration;
    }

    @Override
    public int getMaxUploads() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxSeedConnections() {
        return 0;
    }

    @Override
    public int getMaxConnections() {
        return 0;
    }

    @Override
    public LogRelation getLogRelation() {
        return null;
    }

    @Override
    public int getDownloadRateLimitBytesPerSecond() {
        return 0;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public int getCryptoLevel() {
        return 0;
    }

    @Override
    public void enqueueReadRequest(PEPeer peer, DiskManagerReadRequest request, DiskManagerReadRequestListener listener) {
    }

    @Override
    public void discarded(PEPeer peer, int bytes) {
    }

    @Override
    public void dataBytesSent(PEPeer peer, int bytes) {
    }

    @Override
    public void dataBytesReceived(PEPeer peer, int bytes) {
    }

    @Override
    public void addPiece(PEPiece piece) {
    }

    @Override
    public void addPeer(PEPeer peer) {
        peer.addListener(peerListener);

        if (peerManager != null) {
            /*
             * Someone on my local network might be sending me a file.
             * We both have the same IP as listed on the tracker, yet we don't have the same port.
             * I'll refresh my UPnP cache and get his internal IP see MetadataPeerManager#getPeerLocalIp(int  port)
             */
            if (peer.getIp().equals(externalIp) && peer.getPort() != localTcpPort && !manualIps.contains(peer.getIp())) {
                // what are the odds?
                // I will not hack the vuze core for this, since it is a very delicate change,
                // this is more to support our Send File feature.

                // It will not work for more than one leaf double nat.
                // in this case, better to rely in the DHT NAT traversal magic for now

                // It will work only for TCP peers.

                String ip = getPeerLocalIp(peer.getPort());
                
                if (ip == null) {
                    // maybe the mapping cache is too old
                    // doing this with an ugly logic, no room for listener here due to lack of a public api
                    refreshMappings();
                    ip = getPeerLocalIp(peer.getPort());
                }
                
                if (ip != null) {
                    manualIps.add(ip); //this will break future recursion
                    peerManager.addPeer(ip, peer.getPort(), 0, false, new HashMap());
                }
            }
        }
    }

    @Override
    public void addHTTPSeed(String address, int port) {
    }

    private String getExternalIp() {
        Utilities utils = PluginInitializer.getDefaultInterface().getUtilities();

        InetAddress address = utils.getPublicAddress();

        return address != null ? address.getHostAddress() : null;
    }

    private String getPeerLocalIp(int port) {
        if (port == 0) {
            return null;
        }

        PluginInterface upnp_pi = PluginInitializer.getDefaultInterface().getPluginManager().getPluginInterfaceByClass(UPnPPlugin.class);

        if (upnp_pi != null) {
            UPnPPlugin upnp = (UPnPPlugin) upnp_pi.getPlugin();

            UPnPPluginService[] services = upnp.getServices();

            for (UPnPPluginService service : services) {
                for (UPnPPluginService.serviceMapping mapping : service.getMappings()) {
                    if (mapping.isTCP() && mapping.getPort() == port) {
                        return mapping.getInternalHost();
                    }
                }
            }

            return null;
        } else {
            return null;
        }
    }
    
    private void refreshMappings() {
        try {
            
            System.out.println("Refreshig UPnP mappings");
            
            PluginInterface upnp_pi = PluginInitializer.getDefaultInterface().getPluginManager().getPluginInterfaceByClass(UPnPPlugin.class);

            if (upnp_pi != null) {
                UPnPPlugin upnp = (UPnPPlugin) upnp_pi.getPlugin();

                upnp.refreshMappings(true);
                Thread.sleep(10000); // simply wait 10 seconds
            }
            
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
