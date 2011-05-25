package com.limegroup.gnutella.gui.themes;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuItemUI;

import org.limewire.util.OSUtils;

import com.apple.laf.AquaMenuItemUI;

public class SkinMenuItemUI extends BasicMenuItemUI {

	public static ComponentUI createUI(JComponent comp) {
		if (OSUtils.isMacOSX() && !(comp instanceof SkinMenuItem)) {
			return AquaMenuItemUI.createUI(comp);
		} else {
			return ThemeMediator.CURRENT_THEME.createMenuItemUI(comp);
		}
	}
}
