package com.limegroup.gnutella.gui.themes;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

public interface ThemeSetter {

    public String getName();
    
    public void apply();

    public ComponentUI createCheckBoxMenuItemUI(JComponent comp);

    public ComponentUI createMenuBarUI(JComponent comp);

    public ComponentUI createMenuItemUI(JComponent comp);

    public ComponentUI createMenuUI(JComponent comp);

    public ComponentUI createPopupMenuSeparatorUI(JComponent comp);

    public ComponentUI createPopupMenuUI(JComponent comp);

    public ComponentUI createRadioButtonMenuItemUI(JComponent comp);

    public ComponentUI createTextAreadUI(JComponent comp);

    public ComponentUI createListUI(JComponent comp);

    public ComponentUI createComboBoxUI(JComponent comp);
}
