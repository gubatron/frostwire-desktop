package com.limegroup.gnutella.gui.themes;

import java.awt.Color;

public class SkinHandler {

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

    /**
     * Setting for the search result speed Color.
     */
    public static final Color getSearchResultSpeedColor() {
        return new Color(7, 170, 0);
    }

    /**
     * Setting for the search ip private address Color.
     */
    public static final Color getSearchPrivateIPColor() {
        return new Color(255, 0, 0);
    }

    /**
     * Setting for the search ip selected private address Color.
     */
    public static final Color getSearchSelectedPrivateIPColor() {
        return getSearchPrivateIPColor();
    }

    /**
     * Setting for the top of the filter title color.
     */
    public static final Color getFilterTitleTopColor() {
        return getTableHeaderBackgroundColor();
    }

    /**
     * Setting for the filter title color.
     */
    public static final Color getFilterTitleColor() {
        return getTableHeaderBackgroundColor();
    }

    /**
     * Setting for the background grid color.
     */
    public static final Color getSearchGridColor() {
        return new Color(0, 0, 0);
    }

    /**
     * Setting for the not sharing label Color.
     */
    public static final Color getNotSharingLabelColor() {
        return new Color(208, 0, 5);
    }

    /**
     * Setting for the window 4 Color.
     */
    public static final Color getWindow4Color() {
        return new Color(0, 0, 0);
    }

    /**
     * Setting for the table odd row Color Color for special search results.
     */
    public static final Color getTableSpecialBackgroundColor() {
        return new Color(255, 243, 193);
    }

    /**
     * Setting for the table even row Color for special search results.
     */
    public static final Color getTableSpecialAlternateColor() {
        return new Color(255, 222, 102);
    }
}
