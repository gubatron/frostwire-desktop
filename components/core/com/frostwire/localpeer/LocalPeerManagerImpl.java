/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.frostwire.localpeer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.frostwire.util.JsonUtils;

/**
 * 
 * Not thread safe.
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class LocalPeerManagerImpl implements LocalPeerManager {

    private static final Logger LOG = LoggerFactory.getLogger(LocalPeerManagerImpl.class);

    private static final String SERVICE_TYPE = "_fw_local_peer._tcp.local.";
    private static final String SERVICE_NAME = "FrostWire Local Peer";
    private static final String PEER_PROPERTY = "peer";

    private final MulticastLock lock;
    private final LocalPeer localPeer;

    private final ServiceListener serviceListener;
    private final ServiceInfo serviceInfo;

    private JmDNS jmdns;
    private LocalPeerManagerListener listener;

    public LocalPeerManagerImpl(LocalPeer localPeer, MulticastLock lock) {
        this.localPeer = localPeer;
        this.lock = lock;

        this.serviceListener = new JmDNSServiceListener();
        this.serviceInfo = createService(localPeer);
    }

    public LocalPeerManagerImpl(LocalPeer localPeer) {
        this(localPeer, null);
    }

    public LocalPeer getLocalPeer() {
        return localPeer;
    }

    public LocalPeerManagerListener getListener() {
        return listener;
    }

    public void setListener(LocalPeerManagerListener listener) {
        this.listener = listener;
    }

    @Override
    public void start() {
        try {
            if (jmdns != null) {
                LOG.warn("JmDNS already working, review the logic");
                stop();
            }

            if (lock != null) {
                lock.acquire();
            }

            jmdns = JmDNS.create();
            jmdns.addServiceListener(SERVICE_TYPE, serviceListener);

            jmdns.registerService(serviceInfo);

        } catch (Throwable e) {
            LOG.error("Unable to start local peer manager", e);
        }
    }

    @Override
    public void stop() {
        try {
            if (jmdns != null) {
                jmdns.removeServiceListener(SERVICE_TYPE, serviceListener);
                jmdns.unregisterAllServices();

                try {
                    jmdns.close();
                } catch (IOException e) {
                    LOG.error("Error closing JmDNS", e);
                }

                jmdns = null;
            }

            if (lock != null) {
                lock.release();
            }
        } catch (Throwable e) {
            LOG.error("Error stopping local peer manager", e);
        }
    }

    private ServiceInfo createService(LocalPeer peer) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(PEER_PROPERTY, JsonUtils.toJson(peer));
        return ServiceInfo.create(SERVICE_TYPE, SERVICE_NAME, peer.listeningPort, 0, 0, false, map);
    }

    private final class JmDNSServiceListener implements ServiceListener {

        @Override
        public void serviceResolved(ServiceEvent event) {
            if (listener != null) {
                LocalPeer peer = getPeer(event);
                if (peer != null) {
                    listener.peerResolved(peer);
                }
            }
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            if (listener != null) {
                LocalPeer peer = getPeer(event);
                if (peer != null) {
                    listener.peerRemoved(peer);
                }
            }
        }

        @Override
        public void serviceAdded(ServiceEvent event) {
            if (jmdns != null) {
                jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
            }
        }

        private LocalPeer getPeer(ServiceEvent event) {
            LocalPeer peer = null;

            try {
                String json = event.getInfo().getPropertyString(PEER_PROPERTY);
                peer = JsonUtils.toObject(json, LocalPeer.class);
            } catch (Throwable e) {
                LOG.error("Unable to extract peer info from service event", e);
            }

            return peer;
        }
    }
}
