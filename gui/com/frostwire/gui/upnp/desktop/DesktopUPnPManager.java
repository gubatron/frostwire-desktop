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

import org.teleal.cling.UpnpService;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;

import com.frostwire.core.ConfigurationManager;
import com.frostwire.core.Constants;
import com.frostwire.gui.Librarian;
import com.frostwire.gui.library.DeviceDiscoveryClerk;
import com.frostwire.gui.library.LibraryMediator;
import com.frostwire.gui.upnp.PingInfo;
import com.frostwire.gui.upnp.UPnPFWDevice;
import com.frostwire.gui.upnp.UPnPFWDeviceInfo;
import com.frostwire.gui.upnp.UPnPManager;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public class DesktopUPnPManager extends UPnPManager {

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
    protected void handlePeerDevice(PingInfo p, InetAddress address, boolean added) {
        DeviceDiscoveryClerk clerk = LibraryMediator.instance().getDeviceDiscoveryClerk();
        clerk.handleDeviceState(address, p.listeningPort, !added);
    }

    @SuppressWarnings("unchecked")
    private LocalService<UPnPFWDeviceInfo> getInfoService() {
        LocalService<UPnPFWDeviceInfo> service = new AnnotationLocalServiceBinder().read(UPnPFWDeviceInfo.class);

        service.setManager(new DefaultServiceManager<UPnPFWDeviceInfo>(service, UPnPFWDeviceInfo.class));

        return service;
    }
}
