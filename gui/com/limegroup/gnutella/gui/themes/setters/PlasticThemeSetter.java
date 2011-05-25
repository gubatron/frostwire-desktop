package com.limegroup.gnutella.gui.themes.setters;

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicListUI;
import javax.swing.plaf.basic.BasicTextAreaUI;

import org.limewire.util.OSUtils;

import com.jgoodies.looks.common.ExtBasicCheckBoxMenuItemUI;
import com.jgoodies.looks.common.ExtBasicMenuItemUI;
import com.jgoodies.looks.common.ExtBasicPopupMenuSeparatorUI;
import com.jgoodies.looks.common.ExtBasicRadioButtonMenuItemUI;
import com.jgoodies.looks.plastic.PlasticComboBoxUI;
import com.jgoodies.looks.plastic.PlasticMenuBarUI;
import com.jgoodies.looks.plastic.PlasticMenuUI;
import com.jgoodies.looks.plastic.PlasticPopupMenuUI;
import com.limegroup.gnutella.gui.themes.LimeLookAndFeel;
import com.limegroup.gnutella.gui.themes.LimePlasticTheme;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeSetter;
import com.limegroup.gnutella.gui.themes.ThemeSettings;

public class PlasticThemeSetter implements ThemeSetter {
    
    public static final PlasticThemeSetter INSTANCE = new PlasticThemeSetter();
    
    private PlasticThemeSetter() {
    }

    public String getName() {
        return "Plastic Blue Skin (classic FrostWire)";
    }

    public void apply() {
        try {
            LimePlasticTheme.installThisTheme();
            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
//            UIManager.put("PopupMenuUI", "com.jgoodies.looks.plastic.PlasticPopupMenuUI");
//            UIManager.put("MenuItemUI", "com.jgoodies.looks.common.ExtBasicMenuItemUI");
//            UIManager.put("MenuUI", "com.jgoodies.looks.plastic.PlasticMenuUI");
//            UIManager.put("CheckBoxMenuItemUI", "com.jgoodies.looks.common.ExtBasicCheckBoxMenuItemUI");
//            UIManager.put("MenuBarUI", "com.jgoodies.looks.plastic.PlasticMenuBarUI");
//            UIManager.put("RadioButtonMenuItemUI", "com.jgoodies.looks.common.ExtBasicRadioButtonMenuItemUI");
//            UIManager.put("PopupMenuSeparatorUI", "com.jgoodies.looks.common.ExtBasicPopupMenuSeparatorUI");
//            UIManager.put("TextAreaUI", "javax.swing.plaf.basic.BasicTextAreaUI");
            ThemeMediator.applyCommonSkinUI();
            LimeLookAndFeel.installUIManagerDefaults();
            

            UIManager.put("Tree.leafIcon", UIManager.getIcon("Tree.closedIcon"));

            // remove split pane borders
            UIManager.put("SplitPane.border", BorderFactory.createEmptyBorder());

            if (!OSUtils.isMacOSX()) {
                UIManager.put("Table.focusRowHighlightBorder", UIManager.get("Table.focusCellHighlightBorder"));
            }

            UIManager.put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder(1, 1, 1, 1));

            // Add a bolded text version of simple text.
            Font normal = UIManager.getFont("Table.font");
            FontUIResource bold = new FontUIResource(normal.getName(), Font.BOLD, normal.getSize());
            UIManager.put("Table.font.bold", bold);
            UIManager.put("Tree.rowHeight", 0);

        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        } catch (InstantiationException e) {
            throw new ExceptionInInitializerError(e);
        } catch (IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        } catch (UnsupportedLookAndFeelException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public ComponentUI createCheckBoxMenuItemUI(JComponent comp) {
        return ExtBasicCheckBoxMenuItemUI.createUI(comp);
    }

    public ComponentUI createMenuBarUI(JComponent comp) {
        return PlasticMenuBarUI.createUI(comp);
    }

    public ComponentUI createMenuItemUI(JComponent comp) {
        return ExtBasicMenuItemUI.createUI(comp);
    }

    public ComponentUI createMenuUI(JComponent comp) {
        return PlasticMenuUI.createUI(comp);
    }

    public ComponentUI createPopupMenuSeparatorUI(JComponent comp) {
        return ExtBasicPopupMenuSeparatorUI.createUI(comp);
    }

    public ComponentUI createPopupMenuUI(JComponent comp) {
        return PlasticPopupMenuUI.createUI(comp);
    }

    public ComponentUI createRadioButtonMenuItemUI(JComponent comp) {
        return ExtBasicRadioButtonMenuItemUI.createUI(comp);
    }

    public ComponentUI createTextAreadUI(JComponent comp) {
        return BasicTextAreaUI.createUI(comp);
    }

    public ComponentUI createListUI(JComponent comp) {
        return BasicListUI.createUI(comp);
    }

    public ComponentUI createComboBoxUI(JComponent comp) {
        return PlasticComboBoxUI.createUI(comp);
    }
}
