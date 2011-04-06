package com.limegroup.gnutella.gui.themes.setters;

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;

import org.limewire.util.OSUtils;

import com.limegroup.gnutella.gui.themes.LimeLookAndFeel;
import com.limegroup.gnutella.gui.themes.LimePlasticTheme;
import com.limegroup.gnutella.gui.themes.ThemeSetter;

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
            UIManager.setLookAndFeel("com.jgoodies.plaf.plastic.PlasticXPLookAndFeel");
            UIManager.put("PopupMenuUI", "com.jgoodies.plaf.plastic.PlasticPopupMenuUI");
            UIManager.put("MenuItemUI", "com.jgoodies.plaf.common.ExtBasicMenuItemUI");
            UIManager.put("MenuUI", "com.jgoodies.plaf.plastic.PlasticMenuUI");
            UIManager.put("CheckBoxMenuItemUI", "com.jgoodies.plaf.common.ExtBasicCheckBoxMenuItemUI");
            UIManager.put("MenuBarUI", "com.jgoodies.plaf.plastic.PlasticMenuBarUI");
            UIManager.put("RadioButtonMenuItemUI", "com.jgoodies.plaf.common.ExtBasicRadioButtonMenuItemUI");
            UIManager.put("PopupMenuSeparatorUI", "com.jgoodies.plaf.common.ExtBasicPopupMenuSeparatorUI");
            UIManager.put("TextAreaUI", "javax.swing.plaf.basic.BasicTextAreaUI");
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
}
