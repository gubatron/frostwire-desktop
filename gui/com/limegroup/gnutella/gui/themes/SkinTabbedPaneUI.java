package com.limegroup.gnutella.gui.themes;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.View;

public class SkinTabbedPaneUI extends BasicTabbedPaneUI {
    
    private final JTabbedPane tabbedPane;
    
    public static ComponentUI createUI(JComponent comp) {
        return ThemeMediator.CURRENT_THEME.createTabbedPaneUI(comp);
    }
    
    public SkinTabbedPaneUI(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }
    
//    @Override
//    protected int getTabExtraWidth(int tabPlacement, int tabIndex) {
//        int extraWidth = super.getTabExtraWidth(tabPlacement, tabIndex);
//        if (tabbedPane instanceof SkinTabbedPane && ((SkinTabbedPane) tabbedPane).isExtraIconActiveAt(tabIndex)) {
//            Icon extraIcon = ((SkinTabbedPane) tabbedPane).getExtraIcon();
//            extraWidth += extraIcon != null ? extraIcon.getIconWidth() : 0;
//        }        
//        return extraWidth;
//    }
    
    @Override
    protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon, Rectangle tabRect, Rectangle iconRect,
            Rectangle textRect, boolean isSelected) {
        Icon extraIcon = null;
        if (tabbedPane instanceof SkinTabbedPane && ((SkinTabbedPane) tabbedPane).isExtraIconActiveAt(tabIndex)) {
            extraIcon = ((SkinTabbedPane) tabbedPane).getExtraIcon();
        }
        if (extraIcon == null) {
            super.layoutLabel(tabPlacement, metrics, tabIndex, title, icon, tabRect, iconRect, textRect, isSelected);
            return;
        }
        
        textRect.x = textRect.y = iconRect.x = iconRect.y = 0;

        View v = getTextViewForTab(tabIndex);
        if (v != null) {
            tabPane.putClientProperty("html", v);
        }

        SwingUtilities.layoutCompoundLabel((JComponent) tabPane,
                                           metrics, title, icon,
                                           SwingUtilities.CENTER,
                                           SwingUtilities.LEFT,
                                           SwingUtilities.CENTER,
                                           SwingUtilities.TRAILING,
                                           tabRect,
                                           iconRect,
                                           textRect,
                                           textIconGap);

        tabPane.putClientProperty("html", null);

        int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
        int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
        iconRect.x += xNudge + 6;
        iconRect.y += yNudge;
        textRect.x += xNudge;
        textRect.y += yNudge;
    }
    
    @Override
    protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
        super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
        Icon extraIcon = null;
        if (tabbedPane instanceof SkinTabbedPane && ((SkinTabbedPane) tabbedPane).isExtraIconActiveAt(tabIndex)) {
            extraIcon = ((SkinTabbedPane) tabbedPane).getExtraIcon();
        }
        if (extraIcon != null) {
           extraIcon.paintIcon(tabPane, g, textRect.x + textRect.width + 4, textRect.y);
        }
    }
}
