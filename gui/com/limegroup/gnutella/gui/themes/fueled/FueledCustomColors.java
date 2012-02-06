package com.limegroup.gnutella.gui.themes.fueled;

import java.awt.Color;

import com.limegroup.gnutella.gui.themes.SkinCustomColors;

public class FueledCustomColors implements SkinCustomColors {

    private static final Color DARK_NOISE = new Color(0xD5E5ED);
    
    private static final Color LIGHT_NOISE = new Color(0xF2FBFF);

    @Override
    public Color getDarkNoiseColor() {
        return DARK_NOISE;
    }

    @Override
    public Color getLightNoiseColor() {
        return LIGHT_NOISE;
    }
}
