package com.limegroup.gnutella.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.actions.AbstractAction;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeSetter;

/**
 * The menu to be used for themes.
 */
final class ThemeMenu extends AbstractMenu {
    
    /**
     * The client property to use for theme changing when using 'other' L&Fs.
     */
    private static final String THEME_OBJECT = "THEME_OBJECT";
    
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
        def.putClientProperty(THEME_OBJECT, ThemeMediator.DEFAULT_THEME);
        
        // Add a listener to set the new theme as selected.
        def.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                setSelection(ThemeMediator.DEFAULT_THEME);
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
            if(value.equals(item.getClientProperty(THEME_OBJECT))) {
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
       
        Set<ThemeSetter> skins = new TreeSet<ThemeSetter>(new ThemeComparator());
        
        skins.addAll(ThemeMediator.loadThemes());
        
        for(ThemeSetter skin : skins) {
            JMenuItem theme;
            
            theme = new JRadioButtonMenuItem(skin.getName());
            theme.putClientProperty(THEME_OBJECT, skin);
            theme.setSelected(skin.equals(ThemeMediator.CURRENT_THEME));
                
            theme.setFont(AbstractMenu.FONT);
            GROUP.add(theme);
            theme.addActionListener(THEME_CHANGER);
            MENU.add(theme);
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
            ThemeSetter theme = (ThemeSetter)item.getClientProperty(THEME_OBJECT);
    	    ThemeMediator.changeTheme(theme);
        }
    }
    
    /**
     * Simple class to sort the theme lists.
     */
    private static class ThemeComparator implements Comparator<Object> {
        public int compare(Object a, Object b) {
            String name1, name2;
            
            name1 = ((ThemeSetter) a).getName();
            name2 = ((ThemeSetter) b).getName();

            return name1.compareTo(name2);
        }
    }
}
