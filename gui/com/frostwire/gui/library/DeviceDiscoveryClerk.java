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
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.frostwire.HttpFetcher;
import com.frostwire.JsonEngine;
import com.frostwire.core.Constants;
import com.frostwire.gui.library.Device.OnActionFailedListener;
import com.frostwire.gui.upnp.PingInfo;
import com.frostwire.gui.upnp.UPnPManager;
import com.frostwire.localpeer.DesktopMulticastLock;
import com.frostwire.localpeer.LocalPeerManager;
import com.frostwire.localpeer.LocalPeerManagerImpl;

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
        this.peerManager = new LocalPeerManagerImpl(new DesktopMulticastLock(), getMulticastAddress(), Constants.EXTERNAL_CONTROL_LISTENING_PORT);
        deviceCache = Collections.synchronizedMap(new HashMap<String, Device>());
        jsonEngine = new JsonEngine();

        peerManager.start();
    }

    public void handleDeviceState(String key, InetAddress address, int listeningPort, boolean bye, PingInfo pinfo) {
        if (!bye) {
            retrieveFinger(key, address, listeningPort, pinfo);
        } else {
            if (deviceCache.containsKey(key)) {
                Device device = deviceCache.get(key);
                handleDeviceStale(key, address, device);
            }
        }
    }

    private boolean retrieveFinger(final String key, final InetAddress address, int listeningPort, PingInfo pinfo) {
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
                UPnPManager.instance().removeRemoteDevice(device.getUdn());
            }
        });
    }

    private InetAddress getMulticastAddress() {
        try {
            return InetAddress.getByName("0.0.0.0");
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
