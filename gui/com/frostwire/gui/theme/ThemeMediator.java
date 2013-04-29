/*
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
import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.InsetsUIResource;

import org.limewire.util.FileUtils;
import org.limewire.util.OSUtils;

import com.limegroup.gnutella.gui.TipOfTheDayMediator;
import com.limegroup.gnutella.gui.notify.NotifyUserProxy;
import com.limegroup.gnutella.settings.ApplicationSettings;

/**
 * Class that mediates between themes and FrostWire.
 */
public class ThemeMediator {

    public static final Font DIALOG_FONT = new Font(Font.DIALOG, Font.PLAIN, 12);

    public static ThemeSetter DEFAULT_THEME;

    public static ThemeSetter CURRENT_THEME;

    private static List<ThemeSetter> THEMES;

    static {
        loadThemes();
    }

    /**
     * <tt>List</tt> of <tt>ThemeObserver</tt> classes to notify of
     * ui components of theme changes.
     */
    private static final List<ThemeObserver> THEME_OBSERVERS = new LinkedList<ThemeObserver>();

    public static void updateComponentHierarchy() {
        //SwingUtilities.updateComponentTreeUI(GUIMediator.getMainOptionsComponent());
        TipOfTheDayMediator.instance().updateComponentTreeUI();
        NotifyUserProxy.instance().updateUI();
        updateThemeObservers();

        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }

    /**
     * Adds the specified <tt>ThemeObserver</tt> instance to the list of
     * <tt>ThemeObserver</tt>s that should be notified whenever the theme
     * changes.
     *
     * @param observer the <tt>ThemeObserver</tt> to add to the notification
     *  list
     */
    public static void addThemeObserver(ThemeObserver observer) {
        THEME_OBSERVERS.add(observer);
    }

    /**
     * Removes the specified <tt>ThemeObserver</tt> instance from the list
     * of <tt>ThemeObserver</tt>s.  This is necessary to allow the removed
     * component to be garbage-collected.
     *
     * @param observer the <tt>ThemeObserver</tt> to remove from the
     *  notification list
     */
    public static void removeThemeObserver(ThemeObserver observer) {
        THEME_OBSERVERS.remove(observer);
    }

    /**
     * Updates all theme observers.
     */
    public static void updateThemeObservers() {
        for (ThemeObserver curObserver : THEME_OBSERVERS) {
            curObserver.updateTheme();
        }

        //GUIMediator.getMainOptionsComponent().validate();
        //GUIMediator.getAppFrame().validate();
    }

    public static void changeTheme(final ThemeSetter theme) {
        try {

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {

                    try {

                        theme.apply();

                        CURRENT_THEME = theme;

                        saveCurrentTheme(theme);

                        updateComponentHierarchy();

                    } catch (Exception e) {
                        System.out.println("Theme '" + theme.getName() + "' failed to apply: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<ThemeSetter> loadThemes() {

        if (THEMES != null) {
            return THEMES;
        }

        List<ThemeSetter> themes = new ArrayList<ThemeSetter>();

        // from FrostWire
        themes.add(SubstanceThemeSetter.SEA_GLASS);
        themes.add(SubstanceThemeSetter.FUELED);

        // from Substance
        themes.add(SubstanceThemeSetter.AUTUMN);
        themes.add(SubstanceThemeSetter.BUSINESS_BLACK_STEEL);
        themes.add(SubstanceThemeSetter.BUSINESS_BLUE_STEEL);
        themes.add(SubstanceThemeSetter.BUSINESS);
        themes.add(SubstanceThemeSetter.CHALLENGER_DEEP);
        themes.add(SubstanceThemeSetter.CREME_COFFEE);
        themes.add(SubstanceThemeSetter.CREME);
        themes.add(SubstanceThemeSetter.DUST_COFFEE);
        themes.add(SubstanceThemeSetter.DUST);
        themes.add(SubstanceThemeSetter.EMERALD_DUSK);
        themes.add(SubstanceThemeSetter.GEMINI);
        themes.add(SubstanceThemeSetter.GRAPHITE_AQUA);
        themes.add(SubstanceThemeSetter.GRAPHITE_GLASS);
        themes.add(SubstanceThemeSetter.GRAPHITE);
        themes.add(SubstanceThemeSetter.MAGELLAN);
        themes.add(SubstanceThemeSetter.MARINER);
        themes.add(SubstanceThemeSetter.MIST_AQUA);
        themes.add(SubstanceThemeSetter.MIST_SILVER);
        themes.add(SubstanceThemeSetter.MODERATE);
        themes.add(SubstanceThemeSetter.NEBULA_BRICK_WALL);
        themes.add(SubstanceThemeSetter.NEBULA);
        themes.add(SubstanceThemeSetter.OFFICE_BLACK_2007);
        themes.add(SubstanceThemeSetter.OFFICE_BLUE_2007);
        themes.add(SubstanceThemeSetter.OFFICE_SILVER_2007);
        themes.add(SubstanceThemeSetter.RAVEN);
        themes.add(SubstanceThemeSetter.SAHARA);
        themes.add(SubstanceThemeSetter.TWILIGHT);

        DEFAULT_THEME = SubstanceThemeSetter.FUELED;
        THEMES = themes;
        CURRENT_THEME = loadCurrentTheme();

        return THEMES;
    }

    private static ThemeSetter loadCurrentTheme() {
        ThemeSetter currentTheme = DEFAULT_THEME;

        BufferedReader input = null;

        try {

            File skinsFile = ThemeSettings.SKINS_FILE;

            if (skinsFile.exists()) {
                input = new BufferedReader(new FileReader(skinsFile));
                String name = input.readLine();
                for (int i = 0; i < THEMES.size(); i++) {
                    if (THEMES.get(i).getName().equals(name)) {
                        currentTheme = THEMES.get(i);
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtils.close(input);
        }

        return currentTheme;
    }

    private static void saveCurrentTheme(ThemeSetter theme) throws IOException {
        File skinsFile = ThemeSettings.SKINS_FILE;

        BufferedWriter output = new BufferedWriter(new FileWriter(skinsFile));

        try {

            output.write(theme.getName());

        } finally {
            FileUtils.close(output);
        }
    }

    public static void applyCommonSkinUI() {
        UIManager.put("PopupMenuUI", "com.frostwire.gui.theme.SkinPopupMenuUI");
        UIManager.put("MenuItemUI", "com.frostwire.gui.theme.SkinMenuItemUI");
        UIManager.put("MenuUI", "com.frostwire.gui.theme.SkinMenuUI");
        UIManager.put("CheckBoxMenuItemUI", "com.frostwire.gui.theme.SkinCheckBoxMenuItemUI");
        UIManager.put("MenuBarUI", "com.frostwire.gui.theme.SkinMenuBarUI");
        UIManager.put("RadioButtonMenuItemUI", "com.frostwire.gui.theme.SkinRadioButtonMenuItemUI");
        UIManager.put("PopupMenuSeparatorUI", "com.frostwire.gui.theme.SkinPopupMenuSeparatorUI");
        UIManager.put("TextAreaUI", "com.frostwire.gui.theme.SkinTextAreaUI");
        UIManager.put("ListUI", "com.frostwire.gui.theme.SkinListUI");
        UIManager.put("ComboBoxUI", "com.frostwire.gui.theme.SkinComboBoxUI");
        UIManager.put("TreeUI", "com.frostwire.gui.theme.SkinTreeUI");
        UIManager.put("TableUI", "com.frostwire.gui.theme.SkinTableUI");
        UIManager.put("RangeSliderUI", "com.frostwire.gui.theme.SkinRangeSliderUI");
        UIManager.put("FileChooserUI", "com.frostwire.gui.theme.SkinFileChooserUI");
        UIManager.put("TabbedPaneUI", "com.frostwire.gui.theme.SkinTabbedPaneUI");
        UIManager.put("ProgressBarUI", "com.frostwire.gui.theme.SkinProgressBarUI");
        UIManager.put("OptionPaneUI", "com.frostwire.gui.theme.SkinOptionPaneUI");
        UIManager.put("LabelUI", "com.frostwire.gui.theme.SkinLabelUI");

        UIManager.put("ComboBox.editorInsets", new InsetsUIResource(2, 2, 3, 2));
    }

    public static String getRecommendedFontName() {
        String fontName = null;

        String language = ApplicationSettings.getLanguage();
        if (language != null) {
            if (language.startsWith("ja")) {
                //Meiryo for Japanese
                fontName = "Meiryo";
            } else if (language.startsWith("ko")) {
                //Malgun Gothic for Korean
                fontName = "Malgun Gothic";
            } else if (language.startsWith("zh")) {
                //Microsoft JhengHei for Chinese (Traditional)
                //Microsoft YaHei for Chinese (Simplified)
                fontName = "Microsoft JhengHei";
            } else if (language.startsWith("he")) {
                //Gisha for Hebrew
                fontName = "Gisha";
            } else if (language.startsWith("th")) {
                //Leelawadee for Thai
                fontName = "Leelawadee";
            }
        }
        return fontName;
    }

    public static Font fixLabelFont(JLabel label) {
        return ThemeMediator.fixComponentFont(label, label.getText());
    }

    public static Font fixComponentFont(JComponent c, Object msg) {
        Font oldFont = null;

        if (c != null && OSUtils.isWindows()) {
            Font currentFont = c.getFont();
            if (currentFont != null && !canDisplayMessage(currentFont, msg)) {
                oldFont = currentFont;
                c.setFont(ThemeMediator.DIALOG_FONT);
            }
        }

        return oldFont;
    }

    private static boolean canDisplayMessage(Font f, Object msg) {
        boolean result = true;

        if (msg instanceof String) {
            String s = (String) msg;
            result = f.canDisplayUpTo(s) == -1;
        }

        return result;
    }
}