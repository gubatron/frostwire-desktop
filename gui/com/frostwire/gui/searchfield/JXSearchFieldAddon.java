package com.frostwire.gui.searchfield;

import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.plaf.IconUIResource;
import javax.swing.plaf.InsetsUIResource;

import com.frostwire.gui.searchfield.JXSearchField.LayoutStyle;
import com.limegroup.gnutella.gui.GUIMediator;

public class JXSearchFieldAddon {
    public static final String SEARCH_FIELD_SOURCE = "searchField";

    public JXSearchFieldAddon() {
    }

    public void addDefaults() {
        UIManager.put("SearchField.layoutStyle", LayoutStyle.MAC);
        UIManager.put("SearchField.icon", getIcon("search.png"));
        UIManager.put("SearchField.rolloverIcon", getIcon("search.png"));
        UIManager.put("SearchField.pressedIcon", getIcon("search.png"));
        UIManager.put("SearchField.popupIcon", getIcon("search_popup.png"));
        UIManager.put("SearchField.popupRolloverIcon", getIcon("search_popup.png"));
        UIManager.put("SearchField.popupPressedIcon", getIcon("search_popup.png"));
        UIManager.put("SearchField.clearIcon", getIcon("clear.png"));
        UIManager.put("SearchField.clearRolloverIcon", getIcon("clear_rollover.png"));
        UIManager.put("SearchField.clearPressedIcon", getIcon("clear_pressed.png"));
        UIManager.put("SearchField.buttonMargin", new InsetsUIResource(0, 0, 0, 0));
        UIManager.put("SearchField.popupSource", SEARCH_FIELD_SOURCE);
    }

    private IconUIResource getIcon(String iconName) {
        ImageIcon icon = GUIMediator.getThemeImage("searchfield_" + iconName);
        return icon != null ? new IconUIResource(icon) : null;
    }
}
