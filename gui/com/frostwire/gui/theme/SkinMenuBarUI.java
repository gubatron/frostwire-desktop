package com.frostwire.gui.theme;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.synth.SynthMenuBarUI;

import org.limewire.util.OSUtils;

import com.apple.laf.AquaMenuBarUI;

public class SkinMenuBarUI extends SynthMenuBarUI {

	public static ComponentUI createUI(JComponent comp) {
		if (OSUtils.isMacOSX()) {
			return AquaMenuBarUI.createUI(comp);
		} else {
			return new SynthMenuBarUI();
		}
	}
}
