package com.frostwire.gui.theme;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.synth.SynthMenuUI;

import org.limewire.util.OSUtils;

import com.apple.laf.AquaMenuUI;

public class SkinMenuUI extends SynthMenuUI {

	public static ComponentUI createUI(JComponent comp) {
		if (OSUtils.isMacOSX() && !(comp instanceof SkinMenu)) {
			return AquaMenuUI.createUI(comp);
		} else {
			return new SkinMenuUI();
		}
	}
}
