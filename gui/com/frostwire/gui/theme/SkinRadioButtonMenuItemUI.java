package com.frostwire.gui.theme;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.synth.SynthRadioButtonMenuItemUI;

import org.limewire.util.OSUtils;

import com.apple.laf.AquaMenuItemUI;

public class SkinRadioButtonMenuItemUI extends SynthRadioButtonMenuItemUI {

	public static ComponentUI createUI(JComponent comp) {
		if (OSUtils.isMacOSX() && !(comp instanceof SkinRadioButtonMenuItem)) {
			return AquaMenuItemUI.createUI(comp);
		} else {
			return new SkinRadioButtonMenuItemUI();
		}
	}
}
