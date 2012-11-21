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

import javax.swing.Icon;

import com.frostwire.core.Constants;
import com.limegroup.gnutella.gui.GUIMediator;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
public class DeviceNode extends LibraryNode {

    private static final long serialVersionUID = 2733848224018434257L;

    private static final Icon phonePlusDevices;
    private static final Icon phoneMinusDevices;
    private static final Icon tabletPlusDevices;
    private static final Icon tabletMinusDevices;
    private static final Icon myPlusDevices;
    private static final Icon myMinusDevices;
    private static final Icon desktopPlusDevices;
    private static final Icon desktopMinusDevices;

    static {
        //plus: has children, minus: has no children
        phonePlusDevices = GUIMediator.getThemeImage("phone_small_plus");
        phoneMinusDevices = GUIMediator.getThemeImage("phone_small");
        
        tabletPlusDevices = GUIMediator.getThemeImage("tablet_small_plus");
        tabletMinusDevices = GUIMediator.getThemeImage("tablet_small");
        
        myPlusDevices = GUIMediator.getThemeImage("me_small_plus");
        myMinusDevices = GUIMediator.getThemeImage("me_small");

        desktopPlusDevices = GUIMediator.getThemeImage("desktop_small_plus");
        desktopMinusDevices = GUIMediator.getThemeImage("desktop_small");

    }

    private final Device device;

    public DeviceNode(Device device) {
        super(device.getFinger().nickname);
        this.device = device;
    }

    public Device getDevice() {
        return device;
    }

    public Icon getPlusIcon() {
        return getIcon(true);
    }

    public Icon getMinusIcon() {
        return getIcon(false);
    }

    private Icon getIcon(boolean plus) {
        switch (getDevice().getDeviceType()) {
        case Constants.DEVICE_MAJOR_TYPE_PHONE:
            return plus ? phonePlusDevices : phoneMinusDevices;
        case Constants.DEVICE_MAJOR_TYPE_TABLET:
            return plus ? tabletPlusDevices : tabletMinusDevices;
        case Constants.DEVICE_MAJOR_TYPE_DESKTOP:
            if (getDevice().isLocal()) {
                return plus ? myPlusDevices : myMinusDevices;
            }
            return plus ? desktopPlusDevices : desktopMinusDevices;
        default:
            return plus ? phonePlusDevices : phoneMinusDevices;
        }
    }

}
