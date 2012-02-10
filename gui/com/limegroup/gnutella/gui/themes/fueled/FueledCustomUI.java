package com.limegroup.gnutella.gui.themes.fueled;

import java.awt.Color;

import javax.swing.border.TitledBorder;

import com.limegroup.gnutella.gui.themes.setters.SubstanceCustomUI;

public class FueledCustomUI extends SubstanceCustomUI {

    @Override
    public TitledBorder createTitledBorder(String title) {
        return new FueledTitledBorder(title);
    }
    
    @Override
    public Color getFilterTitleColor() {
        return getLightNoise();
    }
}
