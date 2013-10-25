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

package com.frostwire.gui.upnp.desktop;

import com.frostwire.core.ConfigurationManager;
import com.frostwire.gui.upnp.UPnPFWDeviceDesc;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public class DesktopUPnPFWDeviceDesc implements UPnPFWDeviceDesc {

    private final String deviceType;
    private final int deviceVersion;
    private final String friendlyName;
    private final String manufacturer;
    private final String modelName;
    private final String modelDescription;
    private final String modelNumber;

    public DesktopUPnPFWDeviceDesc() {
        this.deviceType = "UPnPFWDevice";
        this.deviceVersion = 1;
        this.friendlyName = "FrostWire Desktop";
        this.manufacturer = "FrostWire";
        this.modelName = "FrostWire Desktop";
        this.modelDescription = "FrostWire Desktop device";
        this.modelNumber = "v1";
    }

    @Override
    public String getIdentitySalt() {
        return ConfigurationManager.instance().getUUIDString();
    }

    @Override
    public String getType() {
        return deviceType;
    }

    @Override
    public int getVersion() {
        return deviceVersion;
    }

    @Override
    public String getFriendlyName() {
        return friendlyName;
    }

    @Override
    public String getManufacturer() {
        return manufacturer;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public String getModelDescription() {
        return modelDescription;
    }

    @Override
    public String getModelNumber() {
        return modelNumber;
    }
}
