package com.frostwire.gnutella.gui.skin;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPopupMenuSeparatorUI;

import org.limewire.util.OSUtils;
import org.pushingpixels.substance.internal.ui.SubstancePopupMenuSeparatorUI;

import com.apple.laf.AquaPopupMenuSeparatorUI;

public class SkinPopupMenuSeparatorUI extends BasicPopupMenuSeparatorUI {

	public static ComponentUI createUI(JComponent comp) {
		if (OSUtils.isMacOSX() && !(comp instanceof SkinPopupMenu.Separator)) {
			return AquaPopupMenuSeparatorUI.createUI(comp);
		} else {
			return SubstancePopupMenuSeparatorUI.createUI(comp);
		}
	}
}
