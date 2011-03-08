package com.frostwire.gnutella.gui.skin;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuItemUI;

import org.limewire.util.OSUtils;
import org.pushingpixels.substance.internal.ui.SubstanceMenuItemUI;

import com.apple.laf.AquaMenuItemUI;

public class SkinMenuItemUI extends BasicMenuItemUI {

	public static ComponentUI createUI(JComponent comp) {
		if (OSUtils.isMacOSX() && !(comp instanceof SkinMenuItem)) {
			return AquaMenuItemUI.createUI(comp);
		} else {
			return SubstanceMenuItemUI.createUI(comp);
		}
	}
}
