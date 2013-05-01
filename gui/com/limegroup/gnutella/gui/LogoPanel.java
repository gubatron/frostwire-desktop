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

package com.limegroup.gnutella.gui;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * This class contains the logo and the searching icon for the application.
 */
final class LogoPanel extends BoxPanel {

    /**
     * Icon for the when we're searching.
     */
    private final ImageIcon searchingIcon;

    /**
     * Icon for not searching.
     */
    private final ImageIcon notSearchingIcon;

    /**
     * Constant for the <tt>JLabel</tt> used for displaying the lime/spinning
     * lime search status indicator.
     */
    private JLabel labelIcon;

    private JLabel labelLogo;

    /**
     * Constructs a new panel containing the logo and the search icon.
     */
    LogoPanel() {
        super(BoxPanel.X_AXIS);

        searchingIcon = GUIMediator.getThemeImage("searching");
        notSearchingIcon = GUIMediator.getThemeImage("notsearching");

        setupUI();
    }

    private void setupUI() {

        labelIcon = new JLabel();
        labelIcon.setIcon(notSearchingIcon);

        labelLogo = new JLabel();
        ImageIcon logoIcon = GUIMediator.getThemeImage("logo");
        labelLogo.setIcon(logoIcon);

        labelLogo.setSize(logoIcon.getIconWidth(), logoIcon.getIconHeight());
        labelIcon.setSize(searchingIcon.getIconWidth(), searchingIcon.getIconHeight());

        GUIUtils.setOpaque(false, this);

        add(Box.createHorizontalGlue());
        add(labelIcon);
        add(labelLogo);
        add(Box.createHorizontalGlue());

        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                GUIMediator.openURL("http://www.frostwire.com");
            }

            public void mouseEntered(MouseEvent me) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        });
    }

    /**
     * Sets the searching or not searching status of the application.
     *
     * @param searching the searching status of the application
     */
    void setSearching(boolean searching) {
        if (searching) {
            labelIcon.setIcon(searchingIcon);
        } else {
            labelIcon.setIcon(notSearchingIcon);
        }
    }
}
