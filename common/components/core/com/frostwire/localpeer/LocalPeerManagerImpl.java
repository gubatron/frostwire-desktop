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

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class LocalPeerManagerImpl implements LocalPeerManager {

    private static final Logger LOG = LoggerFactory.getLogger(LocalPeerManagerImpl.class);

    private static final String SERVICE_TYPE = "_fw_local_peer._tcp.local.";
    private static final String SERVICE_NAME = "TESTFROMANDROID"; // check if this is the nickname
    private static final String SERVICE_TEXT = "FrostWire local peer service";

    private final MulticastLock lock;
    private final InetAddress address;

    private final ServiceListener serviceListener;
    private final ServiceInfo serviceInfo;

    private JmDNS jmdns;

    public LocalPeerManagerImpl(MulticastLock lock, InetAddress address, int port) {

        this.lock = lock;
        this.address = address;

        this.serviceListener = new ServiceListener() {

            @Override
            public void serviceResolved(ServiceEvent event) {
                System.out.println("Service resolved: " + event.getInfo().getQualifiedName() + " port:" + event.getInfo().getPort());
            }

            @Override
            public void serviceRemoved(ServiceEvent event) {
                System.out.println("Service removed: " + event.getName());
            }

            @Override
            public void serviceAdded(ServiceEvent event) {
                if (jmdns != null) {
                    jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
                }
            }
        };

        this.serviceInfo = ServiceInfo.create(SERVICE_TYPE, SERVICE_NAME, port, SERVICE_TEXT);
    }

    @Override
    public void start() {
        try {
            lock.acquire();

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
}
