package com.frostwire.gui.theme;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;


public class SubstanceCustomUI implements SkinCustomUI {

    public static final Color DARK_DARK_NOISE = new Color(0xC4D6E0);

    public static final Color DARK_NOISE = new Color(0xD5E5ED);

    public static final Color LIGHT_NOISE = new Color(0xF2FBFF);

    public static final Color DARK_BORDER = new Color(0xA9BDC7);

    public static final Color LIGHT_BORDER = new Color(0xCDD9DE);

    public static final Color LIGHT_FOREGROUND = new Color(0xFFFFFF);

    public static final Color TAB_BUTTON_FOREGROUND = new Color(0x6489a8);

    @Override
    public Color getDarkBorder() {
        return DARK_BORDER;
    }

    @Override
    public Color getLightBorder() {
        return LIGHT_BORDER;
    }

    public TitledBorder createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(title);
    }

    @Override
    public Color getLightForegroundColor() {
        return LIGHT_FOREGROUND;
    }

    public Color getTabButtonForegroundColor() {
        return TAB_BUTTON_FOREGROUND;
    }

    public Color getFilterTitleTopColor() {
        return new Color(0xffffff);
    }

    public Color getFilterTitleColor() {
        //0xfdf899 - yellow
        return new Color(0xfdf899);
    }
}