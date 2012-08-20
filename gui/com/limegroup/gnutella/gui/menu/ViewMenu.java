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

package com.limegroup.gnutella.gui.menu;

import java.awt.event.ActionEvent;

import org.limewire.setting.BooleanSetting;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LanguageWindow;
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

    ViewMenu(final String key) {
        super(I18n.tr("&View"));

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

        addMenuItem(new ResetFontSizeAction());

        MENU.addSeparator();

        addMenuItem(new ShowLanguageWindowAction());
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

    private static class ResetFontSizeAction extends AbstractAction {

        private static final long serialVersionUID = -4678340681263959986L;

        public ResetFontSizeAction() {
            super(I18n.tr("Reset Font Size"));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            ThemeMediator.resetFontSizes();
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

            //            if (inc <= -4 || inc >= 4) {
            //                return;
            //            }

            inc += increment;
            ThemeSettings.FONT_SIZE_INCREMENT.setValue(inc);
            ThemeMediator.setFontSizeDelta(increment);
            ThemeMediator.updateComponentHierarchy();
        }
    }
}
