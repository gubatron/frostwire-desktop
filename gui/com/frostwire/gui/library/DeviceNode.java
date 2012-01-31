package com.frostwire.gui.library;

import javax.swing.Icon;

import com.limegroup.gnutella.gui.GUIMediator;

public class DeviceNode extends LibraryNode {

    private static final long serialVersionUID = 2733848224018434257L;

    private static final Icon phonePlusDevices;
    private static final Icon phoneMinusDevices;
    private static final Icon tabletPlusDevices;
    private static final Icon tabletMinusDevices;
    private static final Icon tvPlusDevices;
    private static final Icon tvMinusDevices;

    static {
        phonePlusDevices = GUIMediator.getThemeImage("phone_small");
        phoneMinusDevices = GUIMediator.getThemeImage("phone_small");
        tabletPlusDevices = GUIMediator.getThemeImage("tablet_small");
        tabletMinusDevices = GUIMediator.getThemeImage("tablet_small");
        tvPlusDevices = GUIMediator.getThemeImage("tv_small");
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
