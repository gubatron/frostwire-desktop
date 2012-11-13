/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
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

package com.frostwire.gui.library;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.util.NetworkUtils;

import com.frostwire.HttpFetcher;
import com.frostwire.JsonEngine;
import com.frostwire.gui.library.Device.OnActionFailedListener;
import com.frostwire.gui.upnp.UPnPManager;
import com.limegroup.gnutella.settings.ConnectionSettings;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
public class DeviceDiscoveryClerk {

    private static final Log LOG = LogFactory.getLog(DeviceDiscoveryClerk.class);

    private static final long STALE_DEVICE_TIMEOUT = 14000;

    private Map<String, Device> deviceCache;

    private JsonEngine jsonEngine;

    public DeviceDiscoveryClerk() {
        deviceCache = Collections.synchronizedMap(new HashMap<String, Device>());
        jsonEngine = new JsonEngine();
    }

    public void handleDeviceState(String key, InetAddress address, int listeningPort, boolean bye) {
        if (!bye) {
            retrieveFinger(key, address, listeningPort);
        } else {
            if (deviceCache.containsKey(key)) {
                Device device = deviceCache.get(key);
                handleDeviceStale(key, address, device);
            }
        }
    }

    private boolean retrieveFinger(final String key, final InetAddress address, int listeningPort) {
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
                    Device device = new Device(key, address, listeningPort, finger);
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
}
