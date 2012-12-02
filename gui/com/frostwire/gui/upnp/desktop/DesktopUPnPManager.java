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

package com.frostwire.gui.upnp.desktop;

import java.net.InetAddress;
import java.util.logging.Logger;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;

import com.frostwire.core.ConfigurationManager;
import com.frostwire.core.Constants;
import com.frostwire.gui.Librarian;
import com.frostwire.gui.library.DeviceDiscoveryClerk;
import com.frostwire.gui.library.LibraryMediator;
import com.frostwire.gui.upnp.PingInfo;
import com.frostwire.gui.upnp.UPnPFWDevice;
import com.frostwire.gui.upnp.UPnPFWDeviceInfo;
import com.frostwire.gui.upnp.UPnPManager;
import com.limegroup.gnutella.settings.LibrarySettings;
import com.limegroup.gnutella.util.FrostWireUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public class DesktopUPnPManager extends UPnPManager {

    private static final Logger LOG = Logger.getLogger(DesktopUPnPManager.class.getName());

    private final UPnPService service;

    public DesktopUPnPManager() {
        this.service = new UPnPService(registryListener);
    }

    public void start() {
        service.start();
    }

    @Override
    public UpnpService getService() {
        return service.getService();
    }

    @Override
    public LocalDevice getLocalDevice() {
        return UPnPService.getLocalDevice();
    }

    @Override
    public UPnPFWDevice getUPnPLocalDevice() {
        DesktopUPnPFWDeviceDesc desc = new DesktopUPnPFWDeviceDesc();

        LocalService<?>[] services = new LocalService<?>[] { getInfoService() };

        return new UPnPFWDevice(desc, services);
    }

    @Override
    public PingInfo getLocalPingInfo() {
        PingInfo p = new PingInfo();

        p.uuid = ConfigurationManager.instance().getUUIDString();
        p.listeningPort = Constants.EXTERNAL_CONTROL_LISTENING_PORT;
        p.numSharedFiles = Librarian.instance().getNumSharedFiles();
        p.nickname = ConfigurationManager.instance().getNickname();
        p.deviceMajorType = Constants.DEVICE_MAJOR_TYPE_DESKTOP;
        p.clientVersion = FrostWireUtils.getFrostWireVersion();

        return p;
    }

    @Override
    public void refreshPing() {
        UpnpService service = getService();
        LocalDevice device = UPnPService.getLocalDevice();

        if (service != null && device != null) {
            invokeSetPingInfo(service, device);
        }
    }

    @Override
    protected void handlePeerDevice(String udn, PingInfo p, InetAddress address, boolean added) {
        if (!LibrarySettings.LIBRARY_WIFI_SHARING_ENABLED.getValue() && added) {
            return;
        }
        LOG.info("Device UDN: " + udn + ", added: " + added);
        DeviceDiscoveryClerk clerk = LibraryMediator.instance().getDeviceDiscoveryClerk();

        clerk.handleDeviceState(udn, address, p != null ? p.listeningPort : 0, !added, p);
    }

    @SuppressWarnings("unchecked")
    private LocalService<UPnPFWDeviceInfo> getInfoService() {
        LocalService<UPnPFWDeviceInfo> service = new AnnotationLocalServiceBinder().read(UPnPFWDeviceInfo.class);

        service.setManager(new DefaultServiceManager<UPnPFWDeviceInfo>(service, UPnPFWDeviceInfo.class));

        return service;
    }
}
