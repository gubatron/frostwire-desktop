package com.frostwire.gui.components;

import javax.swing.UIManager;
import javax.swing.plaf.InsetsUIResource;

import com.frostwire.gui.components.searchfield.JXSearchField;

public class SearchField extends JXSearchField {

    private static final long serialVersionUID = 3506693592729700194L;

    static {
        setupSearchFieldUI();
    }

    public SearchField() {

    }

    private static void setupSearchFieldUI() {
        UIManager.put("SearchField.layoutStyle", LayoutStyle.MAC);
        UIManager.put("SearchField.icon", getIcon("basic/resources/search.gif"));
        UIManager.put("SearchField.rolloverIcon", getIcon("basic/resources/search_rollover.gif"));
        UIManager.put("SearchField.pressedIcon", getIcon("basic/resources/search.gif"));
        UIManager.put("SearchField.popupIcon", getIcon("basic/resources/search_popup.gif"));
        UIManager.put("SearchField.popupRolloverIcon", getIcon("basic/resources/search_popup_rollover.gif"));
        UIManager.put("SearchField.clearIcon", getIcon("basic/resources/clear.gif"));
        UIManager.put("SearchField.clearRolloverIcon", getIcon("basic/resources/clear_rollover.gif"));
        UIManager.put("SearchField.clearPressedIcon", getIcon("basic/resources/clear_pressed.gif"));
        UIManager.put("SearchField.buttonMargin", new InsetsUIResource(1, 1, 1, 1));
    }

    private static Object getIcon(String string) {
        // TODO Auto-generated method stub
        return null;
    }
}
