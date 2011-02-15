package com.frostwire.gnutella.gui.skin;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuUI;

import org.limewire.util.OSUtils;
import org.pushingpixels.substance.internal.ui.SubstanceMenuUI;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;

import com.apple.laf.AquaMenuUI;

public class SkinMenuUI extends BasicMenuUI {

	public static ComponentUI createUI(JComponent comp) {
		if (OSUtils.isMacOSX() && !(comp instanceof SkinMenu)) {
			return AquaMenuUI.createUI(comp);
		} else {
			SubstanceCoreUtilities.testComponentCreationThreadingViolation(comp);
			return new SubstanceMenuUI((JMenu) comp);
		}
	}
}
