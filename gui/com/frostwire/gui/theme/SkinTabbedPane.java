package com.frostwire.gui.theme;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JTabbedPane;

public class SkinTabbedPane extends JTabbedPane {

    /**
     * 
     */
    private static final long serialVersionUID = 3129021781954063887L;
    
    private Icon extraIcon;
    private Map<Integer, Boolean> activeExtraIconMap;
    
    public SkinTabbedPane(Icon extraIcon) {
        this.extraIcon = extraIcon;
        activeExtraIconMap = new HashMap<Integer, Boolean>();
    }
    
    public Icon getExtraIcon() {
        return extraIcon;
    }
    
    public void setExtraIcon(Icon extraIcon) {
        this.extraIcon = extraIcon;
        repaint();
    }

    public boolean isExtraIconActiveAt(int tabIndex) {
        return activeExtraIconMap.containsKey(tabIndex) ? activeExtraIconMap.get(tabIndex) : false;
    }
    
    public void setExtraIconActiveAt(int tabIndex, boolean active) {
        activeExtraIconMap.put(tabIndex, active);
        repaint();
    }
}
