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
import com.frostwire.core.ConfigurationManager;
import com.frostwire.core.Constants;
import com.frostwire.gui.Librarian;
import com.frostwire.gui.httpserver.HttpServerManager;
import com.frostwire.gui.library.Device.OnActionFailedListener;
import com.frostwire.localpeer.Finger;
import com.frostwire.localpeer.LocalPeer;
import com.frostwire.localpeer.LocalPeerManager;
import com.frostwire.localpeer.LocalPeerManagerImpl;
import com.frostwire.localpeer.LocalPeerManagerListener;
import com.limegroup.gnutella.settings.LibrarySettings;
import com.limegroup.gnutella.util.FrostWireUtils;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
public class DeviceDiscoveryClerk {

    private static final Log LOG = LogFactory.getLog(DeviceDiscoveryClerk.class);

    private final HttpServerManager httpServerManager;
    private final LocalPeerManager peerManager;

    private Map<String, Device> deviceCache;

    private JsonEngine jsonEngine;

    public DeviceDiscoveryClerk() {
        this.httpServerManager = new HttpServerManager();

        this.peerManager = new LocalPeerManagerImpl();
        this.peerManager.setListener(new LocalPeerManagerListener() {

            private String getKey(LocalPeer peer) {
                return peer.address + ":" + peer.port;
            }

            @Override
            public void peerResolved(LocalPeer peer) {
                try {
                    handleDeviceState(getKey(peer), InetAddress.getByName(peer.address), peer.port, false, peer);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void peerRemoved(LocalPeer peer) {
                try {
                    handleDeviceState(getKey(peer), InetAddress.getByName(peer.address), peer.port, true, peer);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });
        deviceCache = Collections.synchronizedMap(new HashMap<String, Device>());
        jsonEngine = new JsonEngine();

        if (LibrarySettings.LIBRARY_WIFI_SHARING_ENABLED.getValue()) {
            start();
        }
    }

    public void updateLocalPeer() {
        peerManager.update(createLocalPeer());
    }

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                httpServerManager.start(Constants.EXTERNAL_CONTROL_LISTENING_PORT);
                peerManager.start(createLocalPeer());
            }
        }).start();
    }

    public void stop() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                httpServerManager.stop();
                peerManager.stop();
            }
        }).start();
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

    private LocalPeer createLocalPeer() {
        String address = "0.0.0.0";
        int port = Constants.EXTERNAL_CONTROL_LISTENING_PORT;
        int numSharedFiles = Librarian.instance().getNumSharedFiles();
        String nickname = ConfigurationManager.instance().getNickname();
        int deviceType = Constants.DEVICE_MAJOR_TYPE_DESKTOP;
        String clientVersion = FrostWireUtils.getFrostWireVersion();

        return new LocalPeer(address, port, nickname, numSharedFiles, deviceType, clientVersion);
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
            }
        });
    }
}
