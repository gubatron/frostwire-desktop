/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
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

package com.frostwire.gui.library;

import java.net.InetAddress;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.frostwire.HttpFetcher;
import com.frostwire.JsonEngine;
import com.frostwire.core.ConfigurationManager;
import com.frostwire.core.Constants;
import com.frostwire.gui.Librarian;
import com.frostwire.gui.library.Device.OnActionFailedListener;
import com.frostwire.localpeer.LocalPeer;
import com.frostwire.localpeer.LocalPeerManager;
import com.frostwire.localpeer.LocalPeerManagerImpl;
import com.frostwire.localpeer.LocalPeerManagerListener;
import com.limegroup.gnutella.util.FrostWireUtils;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
public class DeviceDiscoveryClerk {

    private static final Log LOG = LogFactory.getLog(DeviceDiscoveryClerk.class);

    private final LocalPeerManager peerManager;

    private Map<String, Device> deviceCache;

    private JsonEngine jsonEngine;

    public DeviceDiscoveryClerk() {
        LocalPeer p = new LocalPeer();

        p.uuid = ConfigurationManager.instance().getUUIDString();
        p.listeningPort = Constants.EXTERNAL_CONTROL_LISTENING_PORT;
        p.numSharedFiles = Librarian.instance().getNumSharedFiles();
        p.nickname = ConfigurationManager.instance().getNickname();
        p.deviceMajorType = Constants.DEVICE_MAJOR_TYPE_DESKTOP;
        p.clientVersion = FrostWireUtils.getFrostWireVersion();

        this.peerManager = new LocalPeerManagerImpl(p);
        this.peerManager.setListener(new LocalPeerManagerListener() {

            @Override
            public void peerResolved(LocalPeer peer) {
                System.out.println("Peer found: " + peer.nickname);
            }

            @Override
            public void peerRemoved(LocalPeer peer) {
                System.out.println("Peer removed: " + peer.nickname);
            }
        });
        deviceCache = Collections.synchronizedMap(new HashMap<String, Device>());
        jsonEngine = new JsonEngine();

        peerManager.start();
    }
    
    public LocalPeerManager getPeerManager() {
        return peerManager;
    }

    public void handleDeviceState(String key, InetAddress address, int listeningPort, boolean bye, LocalPeer pinfo) {
        if (!bye) {
            retrieveFinger(key, address, listeningPort, pinfo);
        } else {
            if (deviceCache.containsKey(key)) {
                Device device = deviceCache.get(key);
                handleDeviceStale(key, address, device);
            }
        }
    }

    private boolean retrieveFinger(final String key, final InetAddress address, int listeningPort, LocalPeer pinfo) {
        try {
            URI uri = new URI("http://" + address.getHostAddress() + ":" + listeningPort + "/finger");

            HttpFetcher fetcher = new HttpFetcher(uri);

            byte[] jsonBytes = fetcher.fetch();

            if (jsonBytes == null) {
                LOG.error("Failed to connnect to " + uri);
                return false;
            }

            String json = new String(jsonBytes);

            Finger finger = jsonEngine.toObject(json, Finger.class);

            synchronized (deviceCache) {
                if (deviceCache.containsKey(key)) {
                    Device device = deviceCache.get(key);
                    device.setFinger(finger);
                    handleDeviceAlive(address, device);
                } else {
                    Device device = new Device(key, address, listeningPort, finger, pinfo);
                    device.setOnActionFailedListener(new OnActionFailedListener() {
                        public void onActionFailed(Device device, int action, Exception e) {
                            handleDeviceStale(key, address, device);
                        }
                    });
                    handleDeviceNew(key, address, device);
                }
            }

            return true;
        } catch (Throwable e) {
            LOG.error("Failed to connnect to " + address);
        }

        return false;
    }

    private void handleDeviceNew(String key, InetAddress address, final Device device) {
        deviceCache.put(key, device);
        device.setTimestamp(System.currentTimeMillis());

        //LOG.info("Device New: " + device);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LibraryMediator.instance().handleDeviceNew(device);
            }
        });
    }

    private void handleDeviceAlive(InetAddress address, final Device device) {
        device.setTimestamp(System.currentTimeMillis());

        //LOG.info("Device Alive: " + device);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LibraryMediator.instance().handleDeviceAlive(device);
            }
        });
    }

    private void handleDeviceStale(String key, InetAddress address, final Device device) {
        deviceCache.remove(key);

        LOG.info("Device Slate: " + device);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LibraryMediator.instance().handleDeviceStale(device);
                //UPnPManager.instance().removeRemoteDevice(device.getUdn());
            }
        });
    }
}
