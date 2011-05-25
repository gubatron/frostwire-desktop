package com.limegroup.gnutella.gui.themes;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicListUI;

import org.limewire.util.OSUtils;

import com.apple.laf.AquaMenuItemUI;

public class SkinListUI extends BasicListUI {
    public static ComponentUI createUI(JComponent comp) {
        return ThemeMediator.CURRENT_THEME.createListUI(comp);
    }
}
