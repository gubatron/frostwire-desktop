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

import org.limewire.util.FileUtils;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.TipOfTheDayMediator;
import com.limegroup.gnutella.gui.notify.NotifyUserProxy;


/**
 * Class that mediates between themes and FrostWire.
 */
public class ThemeMediator {
    
    /**
     * <tt>List</tt> of <tt>ThemeObserver</tt> classes to notify of
     * ui components of theme changes.
     */
    private static final List<ThemeObserver> THEME_OBSERVERS = new LinkedList<ThemeObserver>();
    
    public static void updateComponentHierarchy() {
        SwingUtilities.updateComponentTreeUI(GUIMediator.getMainOptionsComponent());
        TipOfTheDayMediator.instance().updateComponentTreeUI();
        SwingUtilities.updateComponentTreeUI(GUIMediator.getAppFrame());
        NotifyUserProxy.instance().updateUI();
        updateThemeObservers();
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

        GUIMediator.getMainOptionsComponent().validate();
        GUIMediator.getAppFrame().validate();
    }
    
    public static List<SkinInfo> loadSkins() {
        
        List<SkinInfo> skins = new ArrayList<SkinInfo>();
        BufferedReader input = null;
        
        try {
            
            File skinsFile = ThemeSettings.SKINS_FILE;
            
            if (!skinsFile.exists()) {
                createDefaultSkinsFile();
            }
            
            input = new BufferedReader(new FileReader(skinsFile));

            String line = null;

            while ((line = input.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                SkinInfo skin = new SkinInfo();
                String[] arr = line.split(",");
                for (int i = 0; i < arr.length; i++) {
                    String[] kv = arr[i].split(":");
                    if (kv[0].equals("name")) {
                        skin.name = kv[1];
                    }
                    if (kv[0].equals("className")) {
                        skin.className = kv[1];
                    }
                    if (kv[0].equals("default")) {
                        skin._default = Boolean.parseBoolean(kv[1]);
                    }
                    if (kv[0].equals("current")) {
                        skin.current = Boolean.parseBoolean(kv[1]);
                    }
                }
                skins.add(skin);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtils.close(input);
        }
        
        return skins;
    }
    
    public static SkinInfo getDefaultTheme() {
        
        List<SkinInfo> skins = loadSkins();
        
        for (int i = 0; i < skins.size(); i++) {
            SkinInfo skin = skins.get(i);
            
            if (skin._default) {
                return skin;
            }
        }
        
        return null;
    }
    
    public static void setCurrentOrDefaultTheme(boolean setDefault) throws IOException {
        List<SkinInfo> skins = loadSkins();
        
        if (setDefault) {
            for (int i = 0; i < skins.size(); i++) {
                SkinInfo skin = skins.get(i);
                
                if (setDefault && skin._default) {
                    changeTheme(skin.className);
                    break;
                }
            }
        } else {
            
            SkinInfo defaultSkin = null;
            
            for (int i = 0; i < skins.size(); i++) {
                SkinInfo skin = skins.get(i);
                
                if (skin.current) {
                    defaultSkin = null;
                    changeTheme(skin.className);
                    break;
                }
                
                if (skin._default) {
                    defaultSkin = skin;
                }
            }
            
            if (defaultSkin != null) {
                changeTheme(defaultSkin.className);
            }
        }
    }
    
    public static void changeTheme(final String skinClassName) {
        try {
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {

                    try {

                        SubstanceLookAndFeel.setSkin(skinClassName);
                        UIManager.put("PopupMenuUI", "com.frostwire.gnutella.gui.skin.SkinPopupMenuUI");
                        UIManager.put("MenuItemUI", "com.frostwire.gnutella.gui.skin.SkinMenuItemUI");
                        UIManager.put("MenuUI", "com.frostwire.gnutella.gui.skin.SkinMenuUI");
                        UIManager.put("CheckBoxMenuItemUI", "com.frostwire.gnutella.gui.skin.SkinCheckBoxMenuItemUI");
                        UIManager.put("MenuBarUI", "com.frostwire.gnutella.gui.skin.SkinMenuBarUI");
                        UIManager.put("RadioButtonMenuItemUI", "com.frostwire.gnutella.gui.skin.SkinRadioButtonMenuItemUI");
                        UIManager.put("PopupMenuSeparatorUI", "com.frostwire.gnutella.gui.skin.SkinPopupMenuSeparatorUI");

                        for (Window window : Window.getWindows()) {
                            SwingUtilities.updateComponentTreeUI(window);
                        }
                        
                        List<SkinInfo> skins = loadSkins();
                        
                        for (int i = 0; i < skins.size(); i++) {
                            SkinInfo skin = skins.get(i);
                            skin.current = false;
                            if (skin.className.equals(skinClassName)) {
                                skin.current = true;
                            }
                        }
                        
                        saveSkins(skins);

                    } catch (Exception e) {
                        System.out.println("Substance engine failed to initialize");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createDefaultSkinsFile() throws IOException {
        List<SkinInfo> skins = new ArrayList<SkinInfo>();
        
        // from FrostWire
        skins.add(new SkinInfo("Sea Glass", "com.frostwire.gnutella.gui.skin.SeaGlassSkin", true, false));
        
        // from Substance
        skins.add(new SkinInfo("Autumn", "org.pushingpixels.substance.api.skin.AutumnSkin", false, false));
        skins.add(new SkinInfo("Business Black Steel", "org.pushingpixels.substance.api.skin.BusinessBlackSteelSkin", false, false));
        skins.add(new SkinInfo("Business Blue Steel", "org.pushingpixels.substance.api.skin.BusinessBlueSteelSkin", false, false));
        skins.add(new SkinInfo("Business", "org.pushingpixels.substance.api.skin.BusinessSkin", false, false));
        skins.add(new SkinInfo("Challenger Deep", "org.pushingpixels.substance.api.skin.ChallengerDeepSkin", false, false));
        skins.add(new SkinInfo("CremeCoffee", "org.pushingpixels.substance.api.skin.CremeCoffeeSkin", false, false));
        skins.add(new SkinInfo("Creme", "org.pushingpixels.substance.api.skin.CremeSkin", false, false));
        skins.add(new SkinInfo("Dust Coffee", "org.pushingpixels.substance.api.skin.DustCoffeeSkin", false, false));
        skins.add(new SkinInfo("Dust", "org.pushingpixels.substance.api.skin.DustSkin", false, false));
        skins.add(new SkinInfo("Emerald Dusk", "org.pushingpixels.substance.api.skin.EmeraldDuskSkin", false, false));
        skins.add(new SkinInfo("Gemini", "org.pushingpixels.substance.api.skin.GeminiSkin", false, false));
        skins.add(new SkinInfo("Graphite Aqua", "org.pushingpixels.substance.api.skin.GraphiteAquaSkin", false, false));
        skins.add(new SkinInfo("Graphite Glass", "org.pushingpixels.substance.api.skin.GraphiteGlassSkin", false, false));
        skins.add(new SkinInfo("Graphite", "org.pushingpixels.substance.api.skin.GraphiteSkin", false, false));
        skins.add(new SkinInfo("Magellan", "org.pushingpixels.substance.api.skin.MagellanSkin", false, false));
        skins.add(new SkinInfo("Mariner", "org.pushingpixels.substance.api.skin.MarinerSkin", false, false));
        skins.add(new SkinInfo("Mist Aqua", "org.pushingpixels.substance.api.skin.MistAquaSkin", false, false));
        skins.add(new SkinInfo("Mist Silver", "org.pushingpixels.substance.api.skin.MistSilverSkin", false, false));
        skins.add(new SkinInfo("Moderate", "org.pushingpixels.substance.api.skin.ModerateSkin", false, false));
        skins.add(new SkinInfo("Nebula Brick Wall", "org.pushingpixels.substance.api.skin.NebulaBrickWallSkin", false, false));
        skins.add(new SkinInfo("Nebula", "org.pushingpixels.substance.api.skin.NebulaSkin", false, false));
        skins.add(new SkinInfo("Office Black 2007", "org.pushingpixels.substance.api.skin.OfficeBlack2007Skin", false, false));
        skins.add(new SkinInfo("Office Blue 2007", "org.pushingpixels.substance.api.skin.OfficeBlue2007Skin", false, false));
        skins.add(new SkinInfo("Office Silver 2007", "org.pushingpixels.substance.api.skin.OfficeSilver2007Skin", false, false));
        skins.add(new SkinInfo("Raven", "org.pushingpixels.substance.api.skin.RavenSkin", false, false));
        skins.add(new SkinInfo("Sahara", "org.pushingpixels.substance.api.skin.SaharaSkin", false, false));
        skins.add(new SkinInfo("Twilight", "org.pushingpixels.substance.api.skin.TwilightSkin", false, false));
        
        saveSkins(skins);
    }
    
    private static void saveSkins(List<SkinInfo> skins) throws IOException {
        File skinsFile = ThemeSettings.SKINS_FILE;
        
        BufferedWriter output = new BufferedWriter(new FileWriter(skinsFile));
        
        try {
            
            for (SkinInfo skin : skins) {
                String line = "name:" + skin.name + ",className:" + skin.className + ",default:" + skin._default + ",current:" + skin.current;
                output.write(line);
                output.newLine();
            }
            
        } finally {
            FileUtils.close(output);
        }
    }
    
    public static final class SkinInfo {
        public String name;
        public String className;
        public boolean _default;
        public boolean current;
        
        public SkinInfo() {   
        }
        
        public SkinInfo(String name, String className, boolean _default, boolean current) {
            this.name = name;
            this.className = className;
            this._default = _default;
            this.current = current;
        }
    }
}
