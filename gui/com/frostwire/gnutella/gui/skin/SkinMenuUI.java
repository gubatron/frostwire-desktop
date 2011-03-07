package com.frostwire.gnutella.gui.skin;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuUI;

import org.limewire.util.OSUtils;
import org.pushingpixels.substance.internal.ui.SubstanceMenuUI;

import com.apple.laf.AquaMenuUI;

public class SkinMenuUI extends BasicMenuUI {

	public static ComponentUI createUI(JComponent comp) {
		if (OSUtils.isMacOSX() && !(comp instanceof SkinMenu)) {
			return AquaMenuUI.createUI(comp);
		} else {
			return SubstanceMenuUI.createUI(comp);
		}
	}
}
