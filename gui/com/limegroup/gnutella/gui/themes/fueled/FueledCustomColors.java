package com.limegroup.gnutella.gui.themes.fueled;

import java.awt.Color;

import com.limegroup.gnutella.gui.themes.SkinCustomColors;

public class FueledCustomColors implements SkinCustomColors {

    private static final Color DARK_DARK_NOISE = new Color(0xC4D6E0);//new Color(0xABBDC6);
    
    private static final Color DARK_NOISE = new Color(0xD5E5ED);
    
    private static final Color LIGHT_NOISE = new Color(0xF2FBFF);
    
    private static final Color DARK_BORDER = new Color(0xA9BDC7);
    
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
}
