package com.limegroup.gnutella.gui;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.SwingConstants;

import com.frostwire.gui.theme.ThemeObserver;
import com.frostwire.gui.theme.ThemeSettings;

/**
 * This class is really just a hack to make it easier to get the media player 
 * buttons to display correctly.
 */
public final class MediaButton extends JButton implements ThemeObserver {
        
    /**
     * 
     */
    private static final long serialVersionUID = -5372863777384403103L;
    private final String TIP_TEXT;
    private final String UP_NAME;
    private final String DOWN_NAME;

    public MediaButton(String tipText, String upName, String downName) {
        TIP_TEXT = tipText;
        UP_NAME = upName;
        DOWN_NAME = downName;
        updateTheme();
    }

    // inherit doc comment
    public void updateTheme() {
        setContentAreaFilled(false);
        setBorderPainted(ThemeSettings.isNativeOSXTheme());
        setRolloverEnabled(true);
        setIcon(GUIMediator.getThemeImage(UP_NAME));
        setHorizontalAlignment(SwingConstants.CENTER);
        setPressedIcon(GUIMediator.getThemeImage(DOWN_NAME));
        setPreferredSize(new Dimension(
            getIcon().getIconWidth(), getIcon().getIconHeight()));
        setMargin(new Insets(0,0,0,0));
        setToolTipText(TIP_TEXT);        
    }
}
