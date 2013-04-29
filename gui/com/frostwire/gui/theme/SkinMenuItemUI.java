package com.frostwire.gui.theme;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.synth.SynthMenuItemUI;

import org.limewire.util.OSUtils;

import com.apple.laf.AquaMenuItemUI;

public class SkinMenuItemUI extends SynthMenuItemUI {

	public static ComponentUI createUI(JComponent comp) {
		if (OSUtils.isMacOSX() && !(comp instanceof SkinMenuItem)) {
			return AquaMenuItemUI.createUI(comp);
		} else {
			return new SkinMenuItemUI();
		}
	}
}
