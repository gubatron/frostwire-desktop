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

import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.limewire.util.OSUtils;

import com.apple.laf.AquaFonts;
import com.limegroup.gnutella.gui.TipOfTheDayMediator;
import com.limegroup.gnutella.gui.notify.NotifyUserProxy;
import com.limegroup.gnutella.settings.ApplicationSettings;

/**
 * Class that mediates between themes and FrostWire.
 */
public class ThemeMediator {

    public static final Font DIALOG_FONT = new Font(Font.DIALOG, Font.PLAIN, 12);

    public static final Color LIGHT_BORDER_COLOR = new Color(0xCDD9DE);

    public static final Color DARK_BORDER_COLOR = new Color(0xA9BDC7);

    public static final Color LIGHT_FOREGROUND_COLOR = new Color(0xFFFFFF);

    public static final Color TAB_BUTTON_FOREGROUND_COLOR = new Color(0x6489a8);

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

    public static void changeTheme() {
        try {

            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {

                    try {
                        UIManager.setLookAndFeel(new NimbusLookAndFeel() {
                            @Override
                            public UIDefaults getDefaults() {
                                UIDefaults defaults = super.getDefaults();

                                defaults.put("control", SkinColors.LIGHT_BACKGROUND_COLOR);

                                // font color
                                defaults.put("text", SkinColors.TEXT_FONT_FREGROUND_COLOR);
                                defaults.put("controlText", SkinColors.TEXT_FONT_FREGROUND_COLOR);
                                defaults.put("infoText", SkinColors.TEXT_FONT_FREGROUND_COLOR);
                                defaults.put("menuText", SkinColors.TEXT_FONT_FREGROUND_COLOR);
                                defaults.put("textForeground", SkinColors.TEXT_FONT_FREGROUND_COLOR);

                                FontUIResource font = getControlFont();
                                if (font != null) {
                                    defaults.put("defaultFont", font);
                                }

                                defaults.put("Panel.background", SkinColors.LIGHT_BACKGROUND_COLOR);

                                // progressbar
                                int paddingEnabled = defaults.getInt("ProgressBar[Enabled+Indeterminate].progressPadding");
                                int paddingDisabled = defaults.getInt("ProgressBar[Disabled+Indeterminate].progressPadding");

                                defaults.put("ProgressBar[Enabled].foregroundPainter", new SkinProgressBarPainter(SkinProgressBarPainter.State.Enabled, paddingEnabled));
                                defaults.put("ProgressBar[Enabled+Finished].foregroundPainter", new SkinProgressBarPainter(SkinProgressBarPainter.State.Enabled, paddingEnabled));
                                defaults.put("ProgressBar[Enabled+Indeterminate].foregroundPainter", new SkinProgressBarPainter(SkinProgressBarPainter.State.EnabledIndeterminate, paddingEnabled));
                                defaults.put("ProgressBar[Disabled].foregroundPainter", new SkinProgressBarPainter(SkinProgressBarPainter.State.Disabled, paddingDisabled));
                                defaults.put("ProgressBar[Disabled+Finished].foregroundPainter", new SkinProgressBarPainter(SkinProgressBarPainter.State.Disabled, paddingDisabled));
                                defaults.put("ProgressBar[Disabled+Indeterminate].foregroundPainter", new SkinProgressBarPainter(SkinProgressBarPainter.State.DisabledIndeterminate, paddingDisabled));

                                // scrollbar
                                defaults.put("ScrollBar:\"ScrollBar.button\".size", Integer.valueOf(18));

                                defaults.put("ScrollBar:\"ScrollBar.button\"[Disabled].foregroundPainter", new SkinScrollBarButtonPainter(SkinScrollBarButtonPainter.State.Disabled));
                                defaults.put("ScrollBar:\"ScrollBar.button\"[Enabled].foregroundPainter", new SkinScrollBarButtonPainter(SkinScrollBarButtonPainter.State.Enabled));
                                defaults.put("ScrollBar:\"ScrollBar.button\"[MouseOver].foregroundPainter", new SkinScrollBarButtonPainter(SkinScrollBarButtonPainter.State.MouseOver));
                                defaults.put("ScrollBar:\"ScrollBar.button\"[Pressed].foregroundPainter", new SkinScrollBarButtonPainter(SkinScrollBarButtonPainter.State.Pressed));

                                defaults.put("ScrollBar:ScrollBarTrack[Disabled].backgroundPainter", new SkinScrollBarTrackPainter(SkinScrollBarTrackPainter.State.Disabled));
                                defaults.put("ScrollBar:ScrollBarTrack[Enabled].backgroundPainter", new SkinScrollBarTrackPainter(SkinScrollBarTrackPainter.State.Enabled));

                                defaults.put("ScrollBar:ScrollBarThumb[Enabled].backgroundPainter", new SkinScrollBarThumbPainter(SkinScrollBarThumbPainter.State.Enabled));
                                defaults.put("ScrollBar:ScrollBarThumb[MouseOver].backgroundPainter", new SkinScrollBarThumbPainter(SkinScrollBarThumbPainter.State.MouseOver));
                                defaults.put("ScrollBar:ScrollBarThumb[Pressed].backgroundPainter", new SkinScrollBarThumbPainter(SkinScrollBarThumbPainter.State.Pressed));

                                // tableheader
                                defaults.put("TableHeader.background", SkinColors.LIGHT_BACKGROUND_COLOR);

                                defaults.put("TableHeader:\"TableHeader.renderer\"[Enabled].backgroundPainter", new SkinTableHeaderPainter(SkinTableHeaderPainter.State.Enabled));
                                defaults.put("TableHeader:\"TableHeader.renderer\"[MouseOver].backgroundPainter", new SkinTableHeaderPainter(SkinTableHeaderPainter.State.MouseOver));
                                defaults.put("TableHeader:\"TableHeader.renderer\"[Pressed].backgroundPainter", new SkinTableHeaderPainter(SkinTableHeaderPainter.State.Pressed));

                                // table
                                defaults.put("Table.alternateRowColor", new Color(SkinColors.TABLE_ALTERNATE_ROW_COLOR.getRGB()));
                                defaults.put("Table[Enabled+Selected].textBackground", new Color(SkinColors.TABLE_SELECTED_BACKGROUND_ROW_COLOR.getRGB()));
                                defaults.put("Table[Enabled+Selected].textForeground", SkinColors.TABLE_SELECTED_FOREGROUND_ROW_COLOR);

                                // splitter
                                defaults.put("SplitPane:SplitPaneDivider[Enabled].backgroundPainter", new SkinSplitPaneDividerBackgroundPainter(SkinSplitPaneDividerBackgroundPainter.State.Enabled));

                                // tabbedpanetab
                                defaults.put("TabbedPane:TabbedPaneTab[Selected].backgroundPainter", new SkinTabbedPaneTabBackgroundPainter(SkinTabbedPaneTabBackgroundPainter.State.Selected));

                                return defaults;
                            }
                        });
                        new SubstanceThemeSetter().apply();

                        //updateComponentHierarchy();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
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
        UIManager.put("FileChooserUI", "com.frostwire.gui.theme.SkinFileChooserUI");
        UIManager.put("TabbedPaneUI", "com.frostwire.gui.theme.SkinTabbedPaneUI");
        UIManager.put("OptionPaneUI", "com.frostwire.gui.theme.SkinOptionPaneUI");
        UIManager.put("LabelUI", "com.frostwire.gui.theme.SkinLabelUI");
        UIManager.put("ProgressBarUI", "com.frostwire.gui.theme.SkinProgressBarUI");
        UIManager.put("PanelUI", "com.frostwire.gui.theme.SkinPanelUI");
        UIManager.put("ScrollBarUI", "com.frostwire.gui.theme.SkinScrollBarUI");
        UIManager.put("ScrollPaneUI", "com.frostwire.gui.theme.SkinScrollPaneUI");
        UIManager.put("SplitPaneUI", "com.frostwire.gui.theme.SkinSplitPaneUI");
        UIManager.put("ApplicationHeaderUI", "com.frostwire.gui.theme.SkinApplicationHeaderUI");

        UIManager.put("ComboBox.editorInsets", new InsetsUIResource(2, 2, 3, 2));
    }

    private static FontUIResource getControlFont() {
        FontUIResource font = null;
        if (OSUtils.isWindows()) {
            throw new RuntimeException("Need to fix");
        } else if (OSUtils.isMacOSX()) {
            font = AquaFonts.getControlTextFont();
        } else if (OSUtils.isLinux()) {
            throw new RuntimeException("Need to fix");
        }

        return font;
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

    public static TitledBorder createTitledBorder(String title) {
        return new FueledTitledBorder(title);
    }

    static void testComponentCreationThreadingViolation() {
        if (!SwingUtilities.isEventDispatchThread()) {
            UiThreadingViolationException uiThreadingViolationError = new UiThreadingViolationException("Component creation must be done on Event Dispatch Thread");
            uiThreadingViolationError.printStackTrace(System.err);
            throw uiThreadingViolationError;
        }
    }
}