package com.limegroup.gnutella.gui.themes;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;

import org.limewire.util.OSUtils;
import org.pushingpixels.substance.internal.ui.SubstanceCheckBoxMenuItemUI;

import com.apple.laf.AquaMenuItemUI;

public class SkinCheckBoxMenuItemUI extends BasicCheckBoxMenuItemUI {

	public static ComponentUI createUI(JComponent comp) {
		if (OSUtils.isMacOSX() && !(comp instanceof SkinCheckBoxMenuItem)) {
			return AquaMenuItemUI.createUI(comp);
		} else {
			return SubstanceCheckBoxMenuItemUI.createUI(comp);
		}
	}
}
