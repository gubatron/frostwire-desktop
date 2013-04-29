package com.frostwire.gui.theme;

import java.awt.Color;

import javax.swing.border.TitledBorder;

public interface SkinCustomUI {

    public static final String CLIENT_PROPERTY_DARK_DARK_NOISE = "CLIENT_PROPERTY_DARK_DARK_NOISE";
    public static final String CLIENT_PROPERTY_DARK_NOISE = "CLIENT_PROPERTY_DARK_NOISE";
    public static final String CLIENT_PROPERTY_LIGHT_NOISE = "CLIENT_PROPERTY_LIGHT_NOISE";

    public Color getDarkDarkNoise();
    
    public Color getDarkNoise();
    
    public Color getLightNoise();

    public Color getDarkBorder();
    
    public Color getLightBorder();
    
    public TitledBorder createTitledBorder(String title);

    public Color getLightForegroundColor();
    
    public Color getTabButtonForegroundColor();
    
    public Color getFilterTitleTopColor();
    
    public Color getFilterTitleColor();
}
