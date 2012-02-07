package com.limegroup.gnutella.gui.themes.fueled;

import javax.swing.border.Border;

import com.limegroup.gnutella.gui.themes.setters.SubstanceCustomUI;

public class FueledCustomUI extends SubstanceCustomUI {

    @Override
    public Border createTitledBorder(String title) {
        return new FueledTitledBorder(title);
    }
}
