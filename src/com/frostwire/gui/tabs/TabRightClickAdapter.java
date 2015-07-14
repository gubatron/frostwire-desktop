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

package com.frostwire.gui.tabs;

import com.frostwire.gui.theme.SkinPopupMenu;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * This class is put as a mouse adapter on the tabs so that the users
 * can quickly show and hide all the FrostWire Tabs with the exception
 * of the Search Tab.
 * <p/>
 * To save processing and memory, this adapter and it's popup menu is built
 * only once. On click, all the visibilities are checked and the popup menu
 * is refreshed accordingly.
 *
 * @author FTA, gubatron
 */
public class TabRightClickAdapter extends MouseAdapter {
    private JPopupMenu _menu;
    private static TabRightClickAdapter INSTANCE = new TabRightClickAdapter();

    private TabRightClickAdapter() {
        initializeMenu();
    }

    private final void initializeMenu() {
        _menu = new SkinPopupMenu();
    }


    /*
     * Iterate through all the elements of the menu and refresh if they should
     * be checked or not.
     *
     * NOTE: Uses _menu_items hashmap to get the references of the
     * JPopupMenu JCheckBoxMenuItems. Couldn't find a way to do
     * popupMenu.get(key) -> JMenuItem. Only a numeric index, and I already had
     * mapped the actual tabs to names, so we used the same keys for both the tabs
     * and the menu items.
     */
    private final void refreshMenu() {
        _menu.removeAll();
        initializeMenu();
    }

    public final static TabRightClickAdapter getInstance() {
        return INSTANCE;
    }

    public void mouseClicked(MouseEvent me) {
        if (me.getButton() != MouseEvent.BUTTON1) {
            refreshMenu();
            _menu.pack();
            _menu.show(me.getComponent(), me.getX(), me.getY());
        }
    }
}
