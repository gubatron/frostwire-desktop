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
    private static final Icon tvPlusDevices;
    private static final Icon tvMinusDevices;

    static {
        //plus: has children, minus: has no children
        phonePlusDevices = GUIMediator.getThemeImage("phone_small_plus");
        phoneMinusDevices = GUIMediator.getThemeImage("phone_small");
        
        tabletPlusDevices = GUIMediator.getThemeImage("tablet_small_plus");
        tabletMinusDevices = GUIMediator.getThemeImage("tablet_small");
        
        tvPlusDevices = GUIMediator.getThemeImage("tv_small_plus");
        tvMinusDevices = GUIMediator.getThemeImage("tv_small");
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
        switch (getType()) {
        case PHONE:
            return plus ? phonePlusDevices : phoneMinusDevices;
        case TABLET:
            return plus ? tabletPlusDevices : tabletMinusDevices;
        case TV:
            return plus ? tvPlusDevices : tvMinusDevices;
        default:
            return plus ? phonePlusDevices : phoneMinusDevices;
        }
    }

    private DeviceType getType() {
        ScreenMetrics sm = device.getFinger().deviceScreen;

        if (sm == null) {
            return DeviceType.PHONE;
        }

        if (sm.widthPixels > 1000 || sm.heightPixels > 1000) {
            return DeviceType.TABLET;
        }

        return DeviceType.PHONE;
    }

    private enum DeviceType {
        PHONE, TABLET, TV
    }
}
