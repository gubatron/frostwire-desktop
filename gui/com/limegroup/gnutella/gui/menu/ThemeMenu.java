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
import com.limegroup.gnutella.gui.themes.ThemeMediator.SkinInfo;
import com.limegroup.gnutella.gui.themes.ThemeSettings;

/**
 * The menu to be used for themes.
 */
final class ThemeMenu extends AbstractMenu {
    
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
        
        
        JMenuItem def = addMenuItem(THEME_CHANGER);            
        final String defaultVal = ThemeMediator.getDefaultTheme().className;
        def.putClientProperty(THEME_CLASSNAME, defaultVal);
        
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
            if(value.equals(item.getClientProperty(THEME_CLASSNAME))) {
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
       
        Set<SkinInfo> skins = new TreeSet<SkinInfo>(new ThemeComparator());
        
        skins.addAll(ThemeMediator.loadSkins());
        
        for(SkinInfo skin : skins) {
            JMenuItem theme;
            
            theme = new JRadioButtonMenuItem(skin.name);
            theme.putClientProperty(THEME_CLASSNAME, skin.className);
            theme.setSelected(skin.current);
                
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
            
            name1 = ((SkinInfo) a).name;
            name2 = ((SkinInfo) b).name;

            return name1.compareTo(name2);
        }
    }
}
