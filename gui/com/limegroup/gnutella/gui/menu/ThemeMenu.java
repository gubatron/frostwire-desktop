package com.limegroup.gnutella.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;

import org.limewire.util.OSUtils;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.actions.AbstractAction;
import com.limegroup.gnutella.gui.actions.OpenLinkAction;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeSettings;

/**
 * The menu to be used for themes.
 */
final class ThemeMenu extends AbstractMenu {
    
    /**
     * The client property to use for theme changing items.
     */
    private static final String THEME_PROPERTY = "THEME_NAME";
    
    /**
     * The client property to use for theme changing when using 'other' L&Fs.
     */
    private static final String THEME_CLASSNAME = "THEME_CLASSNAME";
    
    /**
     * The listener for changing the theme.
     */
    private static final Action THEME_CHANGER =  new ThemeChangeAction();
    
    /**
     * The ButtonGroup to store the theme options in.
     */
    private static final ButtonGroup GROUP = new ButtonGroup();
    
    /**
     * Constructs the menu.
     */
    ThemeMenu() {
        super(I18n.tr("&Apply Skins"));
        
        addMenuItem(new OpenLinkAction("http://www.frostwire.com/beta/skins/", 
                I18n.tr("&Get More Skins"),
                I18n.tr("Find more skins from frostwire.com")));
        
        addMenuItem(new RefreshThemesAction());
        
        
        JMenuItem def = addMenuItem(THEME_CHANGER);            
        final Object defaultVal = ThemeSettings.THEME_DEFAULT.getAbsolutePath();
        def.putClientProperty(THEME_PROPERTY, defaultVal);
        
        // Add a listener to set the new theme as selected.
        def.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                setSelection(defaultVal);
            }
        });
        
        addSeparator();
        
        addThemeItems();
    }
    
    /**
     * Sets the default theme.
     */
    private static void setSelection(Object value) {
        Enumeration<AbstractButton> items = GROUP.getElements();
        while(items.hasMoreElements()) {
            JMenuItem item = (JMenuItem)items.nextElement();
            if(value.equals(item.getClientProperty(THEME_PROPERTY))) {
                item.setSelected(true);
                break;
            }
        }
    }        
    
    /**
     * Scans through the theme directory for .lwtp files & adds them
     * as menu items to the menu. Also adds themes inside the themes jar.
     */ 
    private void addThemeItems() {
       
        Set<Object> allThemes = new TreeSet<Object>(new ThemeComparator());
        
        allThemes.add("com.frostwire.gnutella.gui.skin.SeaGlassSkin");
        allThemes.add("org.pushingpixels.substance.api.skin.BusinessSkin");
        allThemes.add("org.pushingpixels.substance.api.skin.GraphiteSkin");
        allThemes.add("org.pushingpixels.substance.api.skin.MarinerSkin");
        allThemes.add("org.pushingpixels.substance.api.skin.NebulaSkin");
        
        addInstalledLFs(allThemes);
        

        if(allThemes.isEmpty())
            return;
        
        for(Object next : allThemes) {
            JMenuItem theme;
            
            if(next instanceof String) {
                theme = new JRadioButtonMenuItem((String) next);
                theme.putClientProperty(THEME_CLASSNAME, (String) next);
            } else {
                UIManager.LookAndFeelInfo lfi = (UIManager.LookAndFeelInfo)next;
                theme = new JRadioButtonMenuItem(lfi.getName());
                theme.putClientProperty(THEME_CLASSNAME, lfi.getClassName());
            }
                
            theme.setFont(AbstractMenu.FONT);
            GROUP.add(theme);
            theme.addActionListener(THEME_CHANGER);
            MENU.add(theme);
        }
    }
    
    /**
     * Removes all items in the group from the menu.  Used for refreshing.
     */
    private void removeThemeItems() {
        Enumeration<AbstractButton> items = GROUP.getElements();
        List<JMenuItem> removed = new LinkedList<JMenuItem>();
        while(items.hasMoreElements()) {
            JMenuItem item = (JMenuItem)items.nextElement();
            MENU.remove(item);
            removed.add(item);
        }
        
        for(JMenuItem item : removed)
            GROUP.remove(item);
    }
    
    /**
     * Refreshes the theme menu options to those on the disk.
     */
    private class RefreshThemesAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3056020209833157854L;

		public RefreshThemesAction() {
            super(I18n.tr("&Refresh Skins"));
            putValue(LONG_DESCRIPTION, I18n.tr("Reload available skins from disk"));
        }
    
    	public void actionPerformed(ActionEvent e) {
            removeThemeItems();
            addThemeItems();
    	}
    }    
    
    /**
     * Action that is also used as action listener.
     */
    protected static class ThemeChangeAction extends AbstractAction {
        
        /**
         * 
         */
        private static final long serialVersionUID = -1905875579976154693L;

        public ThemeChangeAction() {
            super(I18n.tr("Use &Default"));
            putValue(LONG_DESCRIPTION, I18n.tr("Use your default skin"));
        }
        
        public void actionPerformed(ActionEvent e) {
            JMenuItem item = (JMenuItem)e.getSource();
            String className = (String)item.getClientProperty(THEME_CLASSNAME);
    	    ThemeMediator.changeTheme(className);
        }
    }
    
    /**
     * Simple class to sort the theme lists.
     */
    private static class ThemeComparator implements Comparator<Object> {
        public int compare(Object a, Object b) {
            String name1, name2;
            if(a instanceof String)
                name1 = ThemeSettings.formatName((String)a);
            else
                name1 = ((UIManager.LookAndFeelInfo)a).getName();
                
            if(b instanceof String)
                name2 = ThemeSettings.formatName((String)b);
            else
                name2 = ((UIManager.LookAndFeelInfo)b).getName();

            return name1.compareTo(name2);
        }
    }
    
    /**
     * Adds installed LFs (Look and Feels) to the list.
     * FTA: Place to add specific look & feel according to the OS,
     * some outdated themes has been removed since are no longer used.
     * 
     * If users try to use those themes FrostWire will suggest the user
     * to go and download from the website the latest version.
     * 
     */
    private static void addInstalledLFs(Set<Object> themes) {
        UIManager.LookAndFeelInfo[] lfs = UIManager.getInstalledLookAndFeels();
        if(lfs == null)
            return;
            
        for(int i = 0; i < lfs.length; i++) {
            UIManager.LookAndFeelInfo l = lfs[i];
            if(l.getClassName().equals("com.sun.java.swing.plaf.windows.WindowsLookAndFeel") || 
               l.getClassName().contains("javax.swing.plaf.metal.MetalLookAndFeel"))
                continue;
            if(l.getClassName().startsWith("apple"))
                continue;
            if(l.getClassName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel") &&
               OSUtils.isLinux())
                continue;
            if(l.getClassName().equals("com.sun.java.swing.plaf.motif.MotifLookAndFeel") ||
               l.getClassName().equals("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel") ||
               l.getClassName().equals("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel"))
                continue;
            
            /** 
             *  FTA NOTE:
             *  DEC/08/2009
             *  Outdated and non-compatible themes for FrostWire 4.18.x were removed from a weird
             *  "fixed list" frostwire used to have.
             *  
             *  It seems that someone tried was doing the same technique before (to remove unused themes)
             *  but the conditionals were wrong or perhaps it was the way it used to work in a previous
             *  release.
             **/
             //System.out.println("ThemeMenu - Look & Feel: "+ l.getClassName()); // FTA: shown only under Mac or Linux
            themes.add(l);
        }
    }
}
