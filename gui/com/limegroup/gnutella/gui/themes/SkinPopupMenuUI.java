package com.limegroup.gnutella.gui.themes;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPopupMenuUI;

import org.limewire.util.OSUtils;
import org.pushingpixels.substance.internal.ui.SubstancePopupMenuUI;

import com.apple.laf.AquaPopupMenuUI;

public class SkinPopupMenuUI extends BasicPopupMenuUI {

	public static ComponentUI createUI(JComponent comp) {
		if (OSUtils.isMacOSX() && !(comp instanceof SkinPopupMenu)) {
			return AquaPopupMenuUI.createUI(comp);
		} else {
			return SubstancePopupMenuUI.createUI(comp);
		}
	}
}
