package com.limegroup.gnutella.gui.menu;

import java.awt.event.ActionEvent;

import org.limewire.setting.BooleanSetting;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LanguageWindow;
import com.limegroup.gnutella.gui.ResourceManager;
import com.limegroup.gnutella.gui.actions.AbstractAction;
import com.limegroup.gnutella.gui.actions.ToggleSettingAction;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeSettings;
import com.limegroup.gnutella.settings.UISettings;

/**
 * This class manages the "view" menu that allows the user to dynamically select
 * which tabs should be viewable at runtime & themes to use.
 */
final class ViewMenu extends AbstractMenu {

    private ShowHideMenu SHOW_HIDE_MENU;

    ViewMenu(final String key) {
        super(I18n.tr("&View"));
        SHOW_HIDE_MENU = new ShowHideMenu();
        MENU.add(SHOW_HIDE_MENU.getMenu());
        addSeparator();

        MENU.add(new ThemeMenu().getMenu());

        MENU.addSeparator();
        ToggleSettingAction toggleAction = new ToggleIconSettingAction(UISettings.SMALL_ICONS, I18n.tr("Use &Small Icons"), I18n.tr("Use Small Icons"));
        addToggleMenuItem(toggleAction);

        toggleAction = new ToggleIconSettingAction(UISettings.TEXT_WITH_ICONS, I18n.tr("Show Icon &Text"), I18n.tr("Show Text Below Icons"));
        addToggleMenuItem(toggleAction);

        toggleAction = new ToggleSmileySettingAction(UISettings.SMILEYS_IN_CHAT, I18n.tr("Show Smi&leys"), I18n.tr("Show emoticons in chat"));
        addToggleMenuItem(toggleAction);

        addMenuItem(new ChangeFontSizeAction(2, I18n.tr("&Increase Font Size"), I18n.tr("Increases the Font Size")));

        addMenuItem(new ChangeFontSizeAction(-2, I18n.tr("&Decrease Font Size"), I18n.tr("Decreases the Font Size")));

        MENU.addSeparator();

        addMenuItem(new ShowLanguageWindowAction());
    }

    public void refreshShowHideMenu() {
        SHOW_HIDE_MENU.refreshMenu();
    }

    private static class ShowLanguageWindowAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = -6305934985012530356L;

        public ShowLanguageWindowAction() {
            super(I18n.tr("C&hange Language"));
            putValue(LONG_DESCRIPTION, I18n.tr("Select your Language Prefereces"));
        }

        public void actionPerformed(ActionEvent e) {
            LanguageWindow lw = new LanguageWindow();
            GUIUtils.centerOnScreen(lw);
            lw.setVisible(true);
        }
    }

    private static class ToggleIconSettingAction extends ToggleSettingAction {

        /**
         * 
         */
        private static final long serialVersionUID = -4953235635397552198L;

        public ToggleIconSettingAction(BooleanSetting setting, String name, String description) {
            super(setting, name, description);
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            GUIMediator.instance().buttonViewChanged();
        }
    }

    private static class ToggleSmileySettingAction extends ToggleSettingAction {
        /**
         * 
         */
        private static final long serialVersionUID = -1098362918446138044L;
        
        public static BooleanSetting newsetting;

        public ToggleSmileySettingAction(BooleanSetting setting, String name, String description) {
            super(setting, name, description);
            newsetting = setting;
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            System.out.println("new smiley setting from menu: " + newsetting.getValue());
            GUIMediator.instance().smileysChanged((boolean) newsetting.getValue());
        }
    }

    private static class ChangeFontSizeAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = -6517433597971721717L;

        private final int increment;

        public ChangeFontSizeAction(int inc, String name, String description) {
            super(name);
            putValue(LONG_DESCRIPTION, description);
            increment = inc;
        }

        public void actionPerformed(ActionEvent e) {
            int inc = ThemeSettings.FONT_SIZE_INCREMENT.getValue();
            inc += increment;
            ThemeSettings.FONT_SIZE_INCREMENT.setValue(inc);
            ThemeMediator.setFontSizeDelta(increment);
            ThemeMediator.updateComponentHierarchy();
        }
    }
}
