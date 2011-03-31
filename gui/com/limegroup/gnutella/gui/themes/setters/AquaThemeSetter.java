package com.limegroup.gnutella.gui.themes.setters;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;

import org.limewire.util.OSUtils;

import com.limegroup.gnutella.gui.themes.ThemeSetter;

public class AquaThemeSetter implements ThemeSetter {
    
    public static final AquaThemeSetter INSTANCE = new AquaThemeSetter();

    private AquaThemeSetter() {
        if (!OSUtils.isMacOSX()) {
            throw new ExceptionInInitializerError("Not OS supported");
        }
    }

    public String getName() {
        return "Mac OS X Aqua Look and Feel";
    }

    public void apply() {
        try {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            reduceFont("Label.font");
            reduceFont("Table.font");

            UIManager.put("List.focusCellHighlightBorder", BorderFactory.createEmptyBorder(1, 1, 1, 1));
            UIManager.put("ScrollPane.border", BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));

            UIManager.put("Tree.leafIcon", UIManager.getIcon("Tree.closedIcon"));

            // remove split pane borders
            UIManager.put("SplitPane.border", BorderFactory.createEmptyBorder());

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

    /**
     * Reduces the size of a font in UIManager.
     */
    private static void reduceFont(String name) {
        Font oldFont = UIManager.getFont(name);
        FontUIResource newFont = new FontUIResource(oldFont.getName(), oldFont.getStyle(), oldFont.getSize() - 2);
        UIManager.put(name, newFont);
    }
}
