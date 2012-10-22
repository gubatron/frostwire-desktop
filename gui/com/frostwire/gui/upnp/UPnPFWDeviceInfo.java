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

package com.frostwire.gui.upnp;

import java.beans.PropertyChangeSupport;

import org.teleal.cling.binding.annotations.UpnpAction;
import org.teleal.cling.binding.annotations.UpnpOutputArgument;
import org.teleal.cling.binding.annotations.UpnpService;
import org.teleal.cling.binding.annotations.UpnpServiceId;
import org.teleal.cling.binding.annotations.UpnpServiceType;
import org.teleal.cling.binding.annotations.UpnpStateVariable;

import com.frostwire.util.JsonUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
@UpnpService(serviceId = @UpnpServiceId("UPnPFWDeviceInfo"), serviceType = @UpnpServiceType(value = "UPnPFWDeviceInfo", version = 1))
public class UPnPFWDeviceInfo {

    private final PropertyChangeSupport propertyChangeSupport;

    @UpnpStateVariable(defaultValue = "")
    private String pingInfo;

    public UPnPFWDeviceInfo() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "RetPingInfo"))
    public String getPingInfo() {
        PingInfo p = UPnPManager.instance().getLocalPingInfo();

        pingInfo = JsonUtils.toJson(p);

        return pingInfo;
    }

    @UpnpAction
    public void setPingInfo() {

        String pingInfoOldValue = pingInfo;
        pingInfo = getPingInfo();

        getPropertyChangeSupport().firePropertyChange("PingInfo", pingInfoOldValue, pingInfo);
    }
}
