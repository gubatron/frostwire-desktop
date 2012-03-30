/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
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

    public void start() {
        try {
            startBroadcast();
        } catch (Throwable e) {
            LOG.error("Error starting broadcast", e);
        }

        try {
            startMulticast();
        } catch (Exception e) {
            LOG.error("Error starting multicast", e);
        }

        new Thread(new CleanStaleDevices(), "CleanStaleDevices").start();
    }

    private void startBroadcast() throws Exception {

        final DatagramSocket socket = new DatagramSocket(null);
        socket.setReuseAddress(true);
        socket.setBroadcast(true);
        socket.setSoTimeout(60000);

        socket.bind(new InetSocketAddress(DeviceConstants.PORT_BROADCAST));

        new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] data = new byte[65535];

                    while (true) {
                        try {

                            DatagramPacket packet = new DatagramPacket(data, data.length);
                            socket.receive(packet);
                            handleDatagramPacket(packet, false);

                        } catch (InterruptedIOException e) {
                        }
                    }

                } catch (Throwable e) {
                    LOG.error("Error receiving broadcast", e);
                } finally {
                    socket.close();
                    socket.disconnect();
                }
            }
        }, "BroadcastClerk").start();
    }

    private void startMulticast() throws Exception {

        final InetAddress groupInetAddress = InetAddress.getByAddress(new byte[] { (byte) 224, 0, 1, 16 });

        final MulticastSocket socket = new MulticastSocket(DeviceConstants.PORT_MULTICAST);
        socket.setSoTimeout(60000);
        socket.setTimeToLive(254);
        socket.setReuseAddress(true);

        InetAddress address = null;

        if (!ConnectionSettings.CUSTOM_INETADRESS.isDefault()) {
            address = InetAddress.getByName(ConnectionSettings.CUSTOM_INETADRESS.getValue());
        } else {
            address = NetworkUtils.getLocalAddress();
        }
        socket.setNetworkInterface(NetworkInterface.getByInetAddress(address));

        socket.joinGroup(groupInetAddress);

        new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] data = new byte[65535];

                    while (true) {
                        try {

                            DatagramPacket packet = new DatagramPacket(data, data.length);
                            socket.receive(packet);
                            handleDatagramPacket(packet, true);

                        } catch (InterruptedIOException e) {
                        }
                    }

                } catch (Throwable e) {
                    LOG.error("Error receiving broadcast", e);
                } finally {
                    socket.close();
                    socket.disconnect();
                }
            }
        }, "MulticastClerk").start();
    }

    private void handleDatagramPacket(DatagramPacket packet, boolean multicast) {

        InetAddress address = packet.getAddress();

        byte[] data = packet.getData();

        int listeningPort = ((data[0x1e] & 0xFF) << 8) + (data[0x1f] & 0xFF);
        boolean bye = (data[0x33] & 0xFF) != 0;

        handleDeviceState(address, listeningPort, bye);
    }

    private void handleDeviceState(InetAddress address, int listeningPort, boolean bye) {
        String key = address.getHostAddress() + ":" + listeningPort;

        if (!bye) {
            retrieveFinger(key, address, listeningPort);
        } else {
            if (deviceCache.containsKey(key)) {
                Device device = deviceCache.get(key);
                handleDeviceStale(key, device);
            }
        }
    }

    private boolean retrieveFinger(final String key, InetAddress address, int listeningPort) {
        try {
            URI uri = new URI("http://" + key + "/finger");

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
                    handleDeviceAlive(key, device);
                } else {
                    Device device = new Device(address, listeningPort, finger);
                    device.setOnActionFailedListener(new OnActionFailedListener() {
                        public void onActionFailed(Device device, int action, Exception e) {
                            handleDeviceStale(key, device);
                        }
                    });
                    handleDeviceNew(key, device);
                }
            }

            return true;
        } catch (Throwable e) {
            LOG.error("Failed to connnect to " + key);
        }

        return false;
    }

    private void handleDeviceNew(String key, final Device device) {
        deviceCache.put(key, device);
        device.setTimestamp(System.currentTimeMillis());

        //LOG.info("Device New: " + device);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LibraryMediator.instance().handleDeviceNew(device);
            }
        });
    }

    private void handleDeviceAlive(String key, final Device device) {
        device.setTimestamp(System.currentTimeMillis());

        //LOG.info("Device Alive: " + device);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LibraryMediator.instance().handleDeviceAlive(device);
            }
        });
    }

    private void handleDeviceStale(String key, final Device device) {
        deviceCache.remove(key);

        LOG.info("Device Slate: " + device);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LibraryMediator.instance().handleDeviceStale(device);
            }
        });
    }

    private final class CleanStaleDevices implements Runnable {
        public void run() {
            while (true) {
                try {
                    long now = System.currentTimeMillis();

                    for (Device device : new ArrayList<Device>(deviceCache.values())) {
                        if (device.getTimestamp() + STALE_DEVICE_TIMEOUT < now) {

                            // last chance
                            if (!retrieveFinger(device.getKey(), device.getAddress(), device.getPort())) {
                                handleDeviceStale(device.getKey(), device);
                            }
                        }
                    }
                } catch (Throwable e) {
                    LOG.error("Error performing clean device stale routine", e);
                }

                try {
                    Thread.sleep(STALE_DEVICE_TIMEOUT);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
