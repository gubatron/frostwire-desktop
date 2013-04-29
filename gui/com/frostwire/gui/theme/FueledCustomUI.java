package com.frostwire.gui.theme;

import javax.swing.border.TitledBorder;


public class FueledCustomUI extends SubstanceCustomUI {

    @Override
    public TitledBorder createTitledBorder(String title) {
        return new FueledTitledBorder(title);
    }
}
