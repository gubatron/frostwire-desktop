package com.limegroup.gnutella.gui.themes;

import java.awt.Color;

public class SkinHandler {

    /**
     * Setting for the table odd row Color Color.
     */
    public static Color getTableBackgroundColor() {
        return new Color(255, 255, 255);
    }

    /**
     * Setting for the table even row Color.
     */
    public static Color getTableAlternateColor() {
        return new Color(248, 248, 255);
    }

    /**
     * Setting for the window 8 Color.
     */
    public static Color getWindow8Color() {
        return new Color(0, 0, 0);
    }

    /**
     * Setting for the table header background Color.
     */
    public static final Color getTableHeaderBackgroundColor() {
        return new Color(117, 142, 197);
    }

    /**
     * Setting for the top search panel background color.
     */
    public static final Color getSearchPanelBG1() {
        return getTableHeaderBackgroundColor();
    }

    /**
     * Setting for the bottom search panel background color.
     */
    public static final Color getSearchPanelBG2() {
        return getTableHeaderBackgroundColor();
    }
}
