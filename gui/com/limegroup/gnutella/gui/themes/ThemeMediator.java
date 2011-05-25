package com.limegroup.gnutella.gui.themes;

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

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.InsetsUIResource;

import org.limewire.util.FileUtils;
import org.limewire.util.OSUtils;

import com.limegroup.gnutella.gui.TipOfTheDayMediator;
import com.limegroup.gnutella.gui.notify.NotifyUserProxy;
import com.limegroup.gnutella.gui.themes.setters.AquaThemeSetter;
import com.limegroup.gnutella.gui.themes.setters.PlasticThemeSetter;
import com.limegroup.gnutella.gui.themes.setters.SubstanceThemeSetter;

/**
 * Class that mediates between themes and FrostWire.
 */
public class ThemeMediator {
    
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
        for(ThemeObserver curObserver : THEME_OBSERVERS) {
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
        
        themes.add(PlasticThemeSetter.INSTANCE);
        
        if (OSUtils.isMacOSX()) {
            themes.add(AquaThemeSetter.INSTANCE);
        }
        
        // from FrostWire
        themes.add(SubstanceThemeSetter.SEA_GLASS);
        
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
        
        // from Substance extras
        themes.add(SubstanceThemeSetter.FIELD_OF_WHEAT);
        themes.add(SubstanceThemeSetter.FINDING_NEMO);
        themes.add(SubstanceThemeSetter.GREEN_MAGIC);
        themes.add(SubstanceThemeSetter.MAGMA);
        themes.add(SubstanceThemeSetter.MANGO);
        themes.add(SubstanceThemeSetter.STREETLIGHTS);
        
        if (OSUtils.isMacOSX()) {
            //DEFAULT_THEME = AquaThemeSetter.INSTANCE;
        	DEFAULT_THEME = SubstanceThemeSetter.SEA_GLASS; //to make FrostWire 5 statement, plus looks a lot better.
        } else {
            DEFAULT_THEME = SubstanceThemeSetter.SEA_GLASS;
        }
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
        UIManager.put("PopupMenuUI", "com.limegroup.gnutella.gui.themes.SkinPopupMenuUI");
        UIManager.put("MenuItemUI", "com.limegroup.gnutella.gui.themes.SkinMenuItemUI");
        UIManager.put("MenuUI", "com.limegroup.gnutella.gui.themes.SkinMenuUI");
        UIManager.put("CheckBoxMenuItemUI", "com.limegroup.gnutella.gui.themes.SkinCheckBoxMenuItemUI");
        UIManager.put("MenuBarUI", "com.limegroup.gnutella.gui.themes.SkinMenuBarUI");
        UIManager.put("RadioButtonMenuItemUI", "com.limegroup.gnutella.gui.themes.SkinRadioButtonMenuItemUI");
        UIManager.put("PopupMenuSeparatorUI", "com.limegroup.gnutella.gui.themes.SkinPopupMenuSeparatorUI");
        UIManager.put("TextAreaUI", "com.limegroup.gnutella.gui.themes.SkinTextAreaUI");
        UIManager.put("ListUI", "com.limegroup.gnutella.gui.themes.SkinListUI");
        UIManager.put("ComboBoxUI", "com.limegroup.gnutella.gui.themes.SkinComboBoxUI");
        
        UIManager.put("ComboBox.editorInsets", new InsetsUIResource(2, 2, 3, 2));
    }
}
