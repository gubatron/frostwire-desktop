package com.limegroup.gnutella.gui.themes;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTableUI;

public class SkinTableUI extends BasicTableUI {
   
    public static ComponentUI createUI(JComponent comp) {
        return ThemeMediator.CURRENT_THEME.createTableUI(comp);
    }
}
