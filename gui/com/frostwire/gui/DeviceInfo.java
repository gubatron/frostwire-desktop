/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
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

package com.frostwire.gui;

import com.frostwire.gui.library.ScreenMetrics;

/**
 * 
 * @author gubatron
 * @author aldenml
 * 
 */
public class DeviceInfo {

    public String getVersion() {
        return Build.VERSION.RELEASE;
    }

    public String getModel() {
        return Build.MODEL;
    }

    public String getProduct() {
        return Build.PRODUCT;
    }

    public String getName() {
        return Build.DEVICE;
    }

    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    public String getBrand() {
        return Build.BRAND;
    }

    public ScreenMetrics getScreenMetrics() {
        ScreenMetrics sm = new ScreenMetrics();

        sm.densityDpi = 1;
        sm.heightPixels = 1;
        sm.widthPixels = 1;
        sm.xdpi = 1;
        sm.ydpi = 1;

        return sm;
    }

    private static final class Build {

        public static final String BRAND = "b--";
        public static final String MANUFACTURER = "m--";
        public static final String DEVICE = "d--";
        public static final String PRODUCT = "p--";
        public static final String MODEL = "m--";

        public static final class VERSION {

            public static final String RELEASE = "r--";

        }
    }
}
