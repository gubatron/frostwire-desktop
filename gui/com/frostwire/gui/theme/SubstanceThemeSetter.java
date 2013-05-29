/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.gui.theme;

import java.awt.Font;
import java.awt.Toolkit;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.limewire.util.OSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.swing.SwingUtilities2;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class SubstanceThemeSetter {

    private static final Logger LOG = LoggerFactory.getLogger(SubstanceThemeSetter.class);

    SubstanceThemeSetter() {
    }

    public void apply() {
        //SubstanceLookAndFeel.setSkin(_skinClassName);
        ThemeMediator.applyCommonSkinUI();

//        if (OSUtils.isWindows()) {
//            fixWindowsOSFont();
//        } else if (OSUtils.isLinux()) {
//            fixLinuxOSFont();
//        }

        fixAAFontSettings();

        UIManager.put("Tree.leafIcon", UIManager.getIcon("Tree.closedIcon"));

        // remove split pane borders
        UIManager.put("SplitPane.border", BorderFactory.createEmptyBorder());

        if (!OSUtils.isMacOSX()) {
            UIManager.put("Table.focusRowHighlightBorder", UIManager.get("Table.focusCellHighlightBorder"));
        }

        UIManager.put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder(1, 1, 1, 1));

        // Add a bold text version of simple text.
        Font normal = UIManager.getFont("Table.font");
        FontUIResource bold = new FontUIResource(normal.getName(), Font.BOLD, normal.getSize());
        UIManager.put("Table.font.bold", bold);
        UIManager.put("Tree.rowHeight", 0);
    }

    // windows font policy http://msdn.microsoft.com/en-us/library/windows/desktop/aa511282.aspx
    // table of languages http://msdn.microsoft.com/en-us/library/ee825488(v=cs.20).aspx
    private void fixWindowsOSFont() {
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();

            Method method = Toolkit.class.getDeclaredMethod("setDesktopProperty", String.class, Object.class);
            method.setAccessible(true);

            String fontName = ThemeMediator.getRecommendedFontName();

            if (fontName != null) {
                Font font = new Font(fontName, Font.PLAIN, 12);
                method.invoke(toolkit, "win.icon.font", font);
                //SubstanceLookAndFeel.setFontPolicy(SubstanceFontUtilities.getDefaultFontPolicy());
            }
        } catch (Throwable e) {
            LOG.error("Error fixing font", e);
        }
    }

    private void fixLinuxOSFont() {
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();

            Method method = Toolkit.class.getDeclaredMethod("setDesktopProperty", String.class, Object.class);
            method.setAccessible(true);

            String fontName = ThemeMediator.getRecommendedFontName();

            if (fontName != null) {
                // linux is hardcoded to Dialog
                fontName = "Dialog";
                method.invoke(toolkit, "gnome.Gtk/FontName", fontName);
                //SubstanceLookAndFeel.setFontPolicy(SubstanceFontUtilities.getDefaultFontPolicy());
            }
        } catch (Throwable e) {
            LOG.error("Error fixing font", e);
        }
    }

    private void fixAAFontSettings() {
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        boolean lafCond = SwingUtilities2.isLocalDisplay();
        Object aaTextInfo = SwingUtilities2.AATextInfo.getAATextInfo(lafCond);
        defaults.put(SwingUtilities2.AA_TEXT_PROPERTY_KEY, aaTextInfo);
    }
}
