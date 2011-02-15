package com.frostwire.gnutella.gui.skin;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuItemUI;

import org.limewire.util.OSUtils;
import org.pushingpixels.substance.internal.ui.SubstanceMenuItemUI;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;

import com.apple.laf.AquaMenuItemUI;

public class SkinMenuItemUI extends BasicMenuItemUI {

	public static ComponentUI createUI(JComponent comp) {
		if (OSUtils.isMacOSX() && !(comp instanceof SkinMenuItem)) {
			return AquaMenuItemUI.createUI(comp);
		} else {
			SubstanceCoreUtilities.testComponentCreationThreadingViolation(comp);
			return new SubstanceMenuItemUI((JMenuItem) comp);
		}
	}
}
