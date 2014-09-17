/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
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

package com.frostwire.bittorrent;

import com.frostwire.bittorrent.libtorrent.LTEngine;
import org.limewire.util.CommonUtils;

import java.io.File;

/**
 * @author gubatron
 * @author aldenml
 */
public final class BTEngineFactory {

    private static BTEngine instance;

    private BTEngineFactory() {
    }

    public static BTEngine getInstance() {
        if (instance == null) {
            instance = LTEngine.getInstance();
            instance.setHome(getHome());
        }
        return instance;
    }

    private static File getHome() {
        File path = new File(CommonUtils.getUserSettingsDir() + File.separator + "libtorrent" + File.separator);
        if (!path.exists()) {
            path.mkdirs();
        }
        return path;
    }
}
