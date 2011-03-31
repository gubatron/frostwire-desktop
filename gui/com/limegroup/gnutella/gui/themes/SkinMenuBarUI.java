package com.limegroup.gnutella.gui.themes;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuBarUI;

import org.limewire.util.OSUtils;
import org.pushingpixels.substance.internal.ui.SubstanceMenuBarUI;

import com.apple.laf.AquaMenuBarUI;

public class SkinMenuBarUI extends BasicMenuBarUI {

	public static ComponentUI createUI(JComponent comp) {
		if (OSUtils.isMacOSX()) {
			return AquaMenuBarUI.createUI(comp);
		} else {
			return SubstanceMenuBarUI.createUI(comp);
		}
	}
}
