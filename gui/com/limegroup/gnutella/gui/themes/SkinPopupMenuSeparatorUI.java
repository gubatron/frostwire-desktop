package com.limegroup.gnutella.gui.themes;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPopupMenuSeparatorUI;

import org.limewire.util.OSUtils;

import com.apple.laf.AquaPopupMenuSeparatorUI;

public class SkinPopupMenuSeparatorUI extends BasicPopupMenuSeparatorUI {

	public static ComponentUI createUI(JComponent comp) {
		if (OSUtils.isMacOSX() && !(comp instanceof SkinPopupMenu.Separator)) {
			return AquaPopupMenuSeparatorUI.createUI(comp);
		} else {
		    return ThemeMediator.CURRENT_THEME.createPopupMenuSeparatorUI(comp);
		}
	}
}
