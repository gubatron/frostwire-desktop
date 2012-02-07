package com.limegroup.gnutella.gui.themes.setters;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import com.limegroup.gnutella.gui.themes.SkinCustomUI;

public class SubstanceCustomUI implements SkinCustomUI {

    public static final Color DARK_DARK_NOISE = new Color(0xC4D6E0);

    public static final Color DARK_NOISE = new Color(0xD5E5ED);

    public static final Color LIGHT_NOISE = new Color(0xF2FBFF);

    public static final Color DARK_BORDER = new Color(0xA9BDC7);
    
    public static final Color LIGHT_FOREGROUND = new Color(0xFFFFFF);

    public Color getDarkDarkNoise() {
        return DARK_DARK_NOISE;
    }

    @Override
    public Color getDarkNoise() {
        return DARK_NOISE;
    }

    @Override
    public Color getLightNoise() {
        return LIGHT_NOISE;
    }

    @Override
    public Color getDarkBorder() {
        return DARK_BORDER;
    }

    public TitledBorder createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(title);
    }

    @Override
    public Color getLightForegroundColor() {
        return LIGHT_FOREGROUND;
    }
}
