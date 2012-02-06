package com.limegroup.gnutella.gui.themes;

import java.awt.Color;

public interface SkinCustomColors {

    public static final String CLIENT_PROPERTY_DARK_NOISE = "CLIENT_PROPERTY_DARK_NOISE";
    public static final String CLIENT_PROPERTY_LIGHT_NOISE = "CLIENT_PROPERTY_LIGHT_NOISE";

    public Color getDarkNoiseColor();
    
    public Color getLightNoiseColor();
}
