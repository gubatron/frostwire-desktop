package com.frostwire.gnutella.gui.skin;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPopupMenuUI;

import org.limewire.util.OSUtils;
import org.pushingpixels.substance.internal.ui.SubstancePopupMenuUI;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;

import com.apple.laf.AquaPopupMenuUI;

public class SkinPopupMenuUI extends BasicPopupMenuUI {

	public static ComponentUI createUI(JComponent comp) {
		if (OSUtils.isMacOSX() && !(comp instanceof SkinPopupMenu)) {
			return AquaPopupMenuUI.createUI(comp);
		} else {
			SubstanceCoreUtilities.testComponentCreationThreadingViolation(comp);
			return new SubstancePopupMenuUI();
		}
	}
}
