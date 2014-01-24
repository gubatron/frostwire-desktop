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
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class LocalPeerManagerImpl implements LocalPeerManager {

    private static final Logger LOG = LoggerFactory.getLogger(LocalPeerManagerImpl.class);

    private static final String LOCK_NAME = "FW_LOCAL_PEER_MANAGER";
    private static final String SERVICE_TYPE = "_workstation._tcp.local.";

    private final ServiceListener serviceListener;
    private final ServiceInfo serviceInfo;

    private MulticastLock lock;
    private JmDNS jmdns;

    public LocalPeerManagerImpl() {

        serviceListener = new ServiceListener() {

            @Override
            public void serviceResolved(ServiceEvent ev) {
            }

            @Override
            public void serviceRemoved(ServiceEvent ev) {
            }

            @Override
            public void serviceAdded(ServiceEvent event) {
                if (jmdns != null) {
                    jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
                }
            }
        };

        serviceInfo = ServiceInfo.create("_fw_local_peer._tcp.local.", "FrostWireLocalPeer", 0, "frostwire local peer service");
    }

    @Override
    public void start(Context ctx) {
        try {
            WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);

            lock = wifi.createMulticastLock(LOCK_NAME);
            lock.setReferenceCounted(true);

            lock.acquire();

            InetAddress address = getInetAddress(wifi);

            jmdns = JmDNS.create(address);
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

    private InetAddress getInetAddress(WifiManager wifi) throws IOException {
        int intaddr = wifi.getConnectionInfo().getIpAddress();
        byte[] byteaddr = new byte[] { (byte) (intaddr & 0xff), (byte) (intaddr >> 8 & 0xff), (byte) (intaddr >> 16 & 0xff), (byte) (intaddr >> 24 & 0xff) };
        return InetAddress.getByAddress(byteaddr);
    }
}
