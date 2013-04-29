package com.frostwire.gui.theme;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPopupMenuUI;

import org.limewire.util.OSUtils;

import com.apple.laf.AquaPopupMenuUI;

public class SkinPopupMenuUI extends BasicPopupMenuUI {

	public static ComponentUI createUI(JComponent comp) {
		if (OSUtils.isMacOSX() && !(comp instanceof SkinPopupMenu)) {
			return AquaPopupMenuUI.createUI(comp);
		} else {
			return ThemeMediator.CURRENT_THEME.createPopupMenuUI(comp);
		}
	}
}
