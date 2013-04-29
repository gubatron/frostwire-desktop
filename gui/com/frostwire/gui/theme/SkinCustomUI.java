package com.frostwire.gui.theme;

import java.awt.Color;

import javax.swing.border.TitledBorder;

public interface SkinCustomUI {

    public Color getDarkBorder();
    
    public TitledBorder createTitledBorder(String title);

    public Color getLightForegroundColor();
    
    public Color getTabButtonForegroundColor();
    
    public Color getFilterTitleTopColor();
    
    public Color getFilterTitleColor();
}
