package com.limegroup.gnutella.gui.themes;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicRadioButtonMenuItemUI;

import org.limewire.util.OSUtils;
import org.pushingpixels.substance.internal.ui.SubstanceRadioButtonMenuItemUI;

import com.apple.laf.AquaMenuItemUI;

public class SkinRadioButtonMenuItemUI extends BasicRadioButtonMenuItemUI {

	public static ComponentUI createUI(JComponent comp) {
		if (OSUtils.isMacOSX() && !(comp instanceof SkinRadioButtonMenuItem)) {
			return AquaMenuItemUI.createUI(comp);
		} else {
			return SubstanceRadioButtonMenuItemUI.createUI(comp);
		}
	}
}
