package com.frostwire.gui.components.searchfield;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.plaf.IconUIResource;
import javax.swing.plaf.InsetsUIResource;

import com.frostwire.gui.components.searchfield.JXSearchField.LayoutStyle;

public class JXSearchFieldAddon {
	public static final String SEARCH_FIELD_SOURCE = "searchField";
	public static final String BUTTON_SOURCE = "button";

	public JXSearchFieldAddon() {
	}

	protected void addBasicDefaults(
			List<Object> defaults) {
		defaults
				.addAll(Arrays.asList(new Object[] { "SearchField.layoutStyle",
						LayoutStyle.MAC, "SearchField.icon",
						getIcon("basic/resources/search.gif"),
						"SearchField.rolloverIcon",
						getIcon("basic/resources/search_rollover.gif"),
						"SearchField.pressedIcon",
						getIcon("basic/resources/search.gif"),
						"SearchField.popupIcon",
						getIcon("basic/resources/search_popup.gif"),
						"SearchField.popupRolloverIcon",
						getIcon("basic/resources/search_popup_rollover.gif"),
						"SearchField.clearIcon",
						getIcon("basic/resources/clear.gif"),
						"SearchField.clearRolloverIcon",
						getIcon("basic/resources/clear_rollover.gif"),
						"SearchField.clearPressedIcon",
						getIcon("basic/resources/clear_pressed.gif"),
						"SearchField.buttonMargin",
						new InsetsUIResource(1, 1, 1, 1),
						"SearchField.popupSource", BUTTON_SOURCE}));
		
		//webstart fix
		//UIManagerExt.addResourceBundle("org.jdesktop.xswingx.plaf.basic.resources.SearchField");
//		UIManager.getDefaults().addResourceBundle("org.jdesktop.xswingx.plaf.basic.resources.SearchField");
	}


	protected void addMacDefaults(List<Object> defaults) {
		defaults
				.addAll(Arrays.asList(new Object[] { "SearchField.icon",
						getIcon("macosx/resources/search.png"),
						"SearchField.rolloverIcon",
						getIcon("macosx/resources/search.png"),
						"SearchField.pressedIcon",
						getIcon("macosx/resources/search.png"),
						"SearchField.popupIcon",
						getIcon("macosx/resources/search_popup.png"),
						"SearchField.popupRolloverIcon",
						getIcon("macosx/resources/search_popup.png"),
						"SearchField.popupPressedIcon",
						getIcon("macosx/resources/search_popup.png"),
						"SearchField.clearIcon",
						getIcon("macosx/resources/clear.png"),
						"SearchField.clearRolloverIcon",
						getIcon("macosx/resources/clear_rollover.png"),
						"SearchField.clearPressedIcon",
						getIcon("macosx/resources/clear_pressed.png"),
						"SearchField.buttonMargin",
						new InsetsUIResource(0, 0, 0, 0),
						"SearchField.popupSource", SEARCH_FIELD_SOURCE}));

	}

	private IconUIResource getIcon(String resourceName) {
		URL url = getClass().getResource(resourceName);
		if (url == null) {
			return null;
		} else {
			return new IconUIResource(new ImageIcon(url));
		}
	}
}
