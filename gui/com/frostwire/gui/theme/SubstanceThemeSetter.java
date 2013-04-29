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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.FontUIResource;

import org.limewire.util.OSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.swing.SwingUtilities2;

import com.frostwire.gui.components.RangeSlider;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class SubstanceThemeSetter implements ThemeSetter {

    private static final Logger LOG = LoggerFactory.getLogger(SubstanceThemeSetter.class);

    private final String _name;
    private final String _skinClassName;
    private final SkinCustomUI customUI;

    private SubstanceThemeSetter(String name, String skinClassName, SkinCustomUI customUI) {
        _name = name;
        _skinClassName = skinClassName;
        this.customUI = customUI;
    }

    private SubstanceThemeSetter(String name, String skinClassName) {
        this(name, skinClassName, new SubstanceCustomUI());
    }

    public String getName() {
        return _name;
    }

    public void apply() {
        //SubstanceLookAndFeel.setSkin(_skinClassName);
        ThemeMediator.applyCommonSkinUI();

        //        if (LookUtils.IS_OS_WINDOWS) {
        //            fixWindowsOSFont();
        //        } else if (LookUtils.IS_OS_LINUX) {
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

    // from FrostWire
    public static final SubstanceThemeSetter SEA_GLASS = new SubstanceThemeSetter("Sea Glass", "com.limegroup.gnutella.gui.themes.SeaGlassSkin");
    public static final SubstanceThemeSetter FUELED = new SubstanceThemeSetter("Fueled", "com.limegroup.gnutella.gui.themes.fueled.FueledSkin", new FueledCustomUI());

    // from Substance
    public static final SubstanceThemeSetter AUTUMN = new SubstanceThemeSetter("Autumn", "org.pushingpixels.substance.api.skin.AutumnSkin");
    public static final SubstanceThemeSetter BUSINESS_BLACK_STEEL = new SubstanceThemeSetter("Business Black Steel", "org.pushingpixels.substance.api.skin.BusinessBlackSteelSkin");
    public static final SubstanceThemeSetter BUSINESS_BLUE_STEEL = new SubstanceThemeSetter("Business Blue Steel", "org.pushingpixels.substance.api.skin.BusinessBlueSteelSkin");
    public static final SubstanceThemeSetter BUSINESS = new SubstanceThemeSetter("Business", "org.pushingpixels.substance.api.skin.BusinessSkin");
    public static final SubstanceThemeSetter CHALLENGER_DEEP = new SubstanceThemeSetter("Challenger Deep", "org.pushingpixels.substance.api.skin.ChallengerDeepSkin");
    public static final SubstanceThemeSetter CREME_COFFEE = new SubstanceThemeSetter("Creme Coffee", "org.pushingpixels.substance.api.skin.CremeCoffeeSkin");
    public static final SubstanceThemeSetter CREME = new SubstanceThemeSetter("Creme", "org.pushingpixels.substance.api.skin.CremeSkin");
    public static final SubstanceThemeSetter DUST_COFFEE = new SubstanceThemeSetter("Dust Coffee", "org.pushingpixels.substance.api.skin.DustCoffeeSkin");
    public static final SubstanceThemeSetter DUST = new SubstanceThemeSetter("Dust", "org.pushingpixels.substance.api.skin.DustSkin");
    public static final SubstanceThemeSetter EMERALD_DUSK = new SubstanceThemeSetter("Emerald Dusk", "org.pushingpixels.substance.api.skin.EmeraldDuskSkin");
    public static final SubstanceThemeSetter GEMINI = new SubstanceThemeSetter("Gemini", "org.pushingpixels.substance.api.skin.GeminiSkin");
    public static final SubstanceThemeSetter GRAPHITE_AQUA = new SubstanceThemeSetter("Graphite Aqua", "org.pushingpixels.substance.api.skin.GraphiteAquaSkin");
    public static final SubstanceThemeSetter GRAPHITE_GLASS = new SubstanceThemeSetter("Graphite Glass", "org.pushingpixels.substance.api.skin.GraphiteGlassSkin");
    public static final SubstanceThemeSetter GRAPHITE = new SubstanceThemeSetter("Graphite", "org.pushingpixels.substance.api.skin.GraphiteSkin");
    public static final SubstanceThemeSetter MAGELLAN = new SubstanceThemeSetter("Magellan", "org.pushingpixels.substance.api.skin.MagellanSkin");
    public static final SubstanceThemeSetter MARINER = new SubstanceThemeSetter("Mariner", "org.pushingpixels.substance.api.skin.MarinerSkin");
    public static final SubstanceThemeSetter MIST_AQUA = new SubstanceThemeSetter("Mist Aqua", "org.pushingpixels.substance.api.skin.MistAquaSkin");
    public static final SubstanceThemeSetter MIST_SILVER = new SubstanceThemeSetter("Mist Silver", "org.pushingpixels.substance.api.skin.MistSilverSkin");
    public static final SubstanceThemeSetter MODERATE = new SubstanceThemeSetter("Moderate", "org.pushingpixels.substance.api.skin.ModerateSkin");
    public static final SubstanceThemeSetter NEBULA_BRICK_WALL = new SubstanceThemeSetter("Nebula Brick Wall", "org.pushingpixels.substance.api.skin.NebulaBrickWallSkin");
    public static final SubstanceThemeSetter NEBULA = new SubstanceThemeSetter("Nebula", "org.pushingpixels.substance.api.skin.NebulaSkin");
    public static final SubstanceThemeSetter OFFICE_BLACK_2007 = new SubstanceThemeSetter("Office Black 2007", "org.pushingpixels.substance.api.skin.OfficeBlack2007Skin");
    public static final SubstanceThemeSetter OFFICE_BLUE_2007 = new SubstanceThemeSetter("Office Blue 2007", "org.pushingpixels.substance.api.skin.OfficeBlue2007Skin");
    public static final SubstanceThemeSetter OFFICE_SILVER_2007 = new SubstanceThemeSetter("Office Silver 2007", "org.pushingpixels.substance.api.skin.OfficeSilver2007Skin");
    public static final SubstanceThemeSetter RAVEN = new SubstanceThemeSetter("Raven", "org.pushingpixels.substance.api.skin.RavenSkin");
    public static final SubstanceThemeSetter SAHARA = new SubstanceThemeSetter("Sahara", "org.pushingpixels.substance.api.skin.SaharaSkin");
    public static final SubstanceThemeSetter TWILIGHT = new SubstanceThemeSetter("Twilight", "org.pushingpixels.substance.api.skin.TwilightSkin");

    public SkinCustomUI getCustomUI() {
        return customUI;
    }

    private static ComponentUI createUI(String name, JComponent comp) {
        try {
            String className = (String) UIManager.get(name);
            Class<?> clazz = Class.forName(className);
            Method m = clazz.getDeclaredMethod("createUI", JComponent.class);
            return (ComponentUI) m.invoke(null, comp);
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public ComponentUI createCheckBoxMenuItemUI(JComponent comp) {
        return createUI("CheckBoxMenuItemUI", comp);
    }

    public ComponentUI createMenuBarUI(JComponent comp) {
        return createUI("MenuBarUI", comp);
    }

    public ComponentUI createMenuItemUI(JComponent comp) {
        return new SkinMenuItemUI();
    }

    public ComponentUI createMenuUI(JComponent comp) {
        return new SkinMenuUI();
    }

    public ComponentUI createPopupMenuSeparatorUI(JComponent comp) {
        return createUI("PopupMenuSeparatorUI", comp);
    }

    public ComponentUI createPopupMenuUI(JComponent comp) {
        return new SkinPopupMenuUI();
    }

    public ComponentUI createRadioButtonMenuItemUI(JComponent comp) {
        return createUI("RadioButtonMenuItemUI", comp);
    }

    public ComponentUI createTextAreaUI(JComponent comp) {
        return createUI("TextAreaUI", comp);
    }

    public ComponentUI createListUI(JComponent comp) {
        return createUI("SkinListUI", comp);
    }

    public ComponentUI createComboBoxUI(JComponent comp) {
        return createUI("ComboBoxUI", comp);
    }

    public ComponentUI createTreeUI(JComponent comp) {
        return createUI("TreeUI", comp);
    }

    public ComponentUI createTableUI(JComponent comp) {
        return createUI("TableUI", comp);
    }

    public ComponentUI createTabbedPaneUI(JComponent comp) {
        //SubstanceCoreUtilities.testComponentCreationThreadingViolation(comp);
        return new SkinTabbedPaneUI((JTabbedPane) comp);
    }

    public ComponentUI createRangeSliderUI(JComponent comp) {
        //SubstanceCoreUtilities.testComponentCreationThreadingViolation(comp);
        return new SkinRangeSliderUI((RangeSlider) comp);
    }

    public ComponentUI createProgressBarUI(JComponent comp) {
        //SubstanceCoreUtilities.testComponentCreationThreadingViolation(comp);
        return new SkinProgressBarUI();
    }

    @Override
    public ComponentUI createOptionPaneUI(JComponent comp) {
        //SubstanceCoreUtilities.testComponentCreationThreadingViolation(comp);
        return new SkinOptionPaneUI();
    }

    @Override
    public ComponentUI createLabelUI(JComponent comp) {
        //SubstanceCoreUtilities.testComponentCreationThreadingViolation(comp);
        return new SkinLabelUI();
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
