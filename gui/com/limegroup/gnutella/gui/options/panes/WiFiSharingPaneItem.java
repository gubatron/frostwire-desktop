/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
 *
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

package com.limegroup.gnutella.gui.options.panes;

import java.io.IOException;

import javax.swing.JCheckBox;

import com.frostwire.gui.upnp.UPnPManager;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.LibrarySettings;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
public class WiFiSharingPaneItem extends AbstractPaneItem {

    public final static String TITLE = I18n.tr("Wi-Fi Sharing");

    public final static String LABEL = I18n.tr("You can see and share your file with peers in your local Wi-Fi network.");

    private final JCheckBox checkBoxEnabledWiFiSharing;

    public WiFiSharingPaneItem() {
        super(TITLE, LABEL);

        this.checkBoxEnabledWiFiSharing = new JCheckBox(I18n.tr("Active search and discovery of local peer files."));

        add(checkBoxEnabledWiFiSharing);
    }

    @Override
    public boolean isDirty() {
        if (LibrarySettings.LIBRARY_WIFI_SHARING_ENABLED.getValue() != checkBoxEnabledWiFiSharing.isSelected()) {
            return true;
        }

        return false;
    }

    @Override
    public void initOptions() {
        checkBoxEnabledWiFiSharing.setSelected(LibrarySettings.LIBRARY_WIFI_SHARING_ENABLED.getValue());
    }

    @Override
    public boolean applyOptions() throws IOException {
        boolean resetUPnP = isDirty();

        LibrarySettings.LIBRARY_WIFI_SHARING_ENABLED.setValue(checkBoxEnabledWiFiSharing.isSelected());

        if (resetUPnP) {
            if (LibrarySettings.LIBRARY_WIFI_SHARING_ENABLED.getValue()) {
                UPnPManager.instance().resume();
            } else {
                UPnPManager.instance().pause();
            }
        }

        return false;
    }
}
