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

import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.ManufacturerDetails;
import org.teleal.cling.model.meta.ModelDetails;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDN;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public class UPnPFWDevice {

    private final DeviceIdentity identity;
    private final DeviceType type;
    private final DeviceDetails details;

    private final LocalService<?>[] services;

    public UPnPFWDevice(UPnPFWDeviceDesc desc, LocalService<?>[] services) {
        this.identity = new DeviceIdentity(UDN.uniqueSystemIdentifier(desc.getIdentitySalt()));
        this.type = new UDADeviceType(desc.getType(), desc.getVersion());

        ManufacturerDetails manufacturer = new ManufacturerDetails(desc.getManufacturer());
        ModelDetails model = new ModelDetails(desc.getModelName(), desc.getModelDescription(), desc.getModelNumber());
        this.details = new DeviceDetails(desc.getFriendlyName(), manufacturer, model);

        this.services = services;
    }

    public DeviceIdentity getIdentity() {
        return identity;
    }

    public DeviceType getType() {
        return type;
    }

    public DeviceDetails getDetails() {
        return details;
    }

    public Icon getIcon() {
        return null;
    }

    public LocalService<?>[] getServices() {
        return services;
    }
}
