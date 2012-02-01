package com.frostwire.gui.library;

import javax.swing.Icon;

import com.limegroup.gnutella.gui.GUIMediator;

public class DevicesNode extends LibraryNode {

    private static final long serialVersionUID = -1188052890239765855L;
    
    private static final Icon plusDevices;
    private static final Icon minusDevices;

    static {
        //plus: has children
        plusDevices = GUIMediator.getThemeImage("android_small");
        
        //minus: has no children
        minusDevices = GUIMediator.getThemeImage("android_small");
    }

    public DevicesNode(String text) {
        super(text);
    }
    
    public Icon getPlusDevices() {
        return plusDevices;
    }
    
    public Icon getMinusDevices() {
        return minusDevices;
    }
}
