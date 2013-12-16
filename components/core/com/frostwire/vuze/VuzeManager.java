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

package com.frostwire.vuze;

import org.gudy.azureus2.core3.util.AERunStateHandler;

import com.aelitis.azureus.core.AzureusCore;

/**
 * Class to initialize the azureus core.
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class VuzeManager {

    private final AzureusCore core;

    public VuzeManager(AzureusCore core) {
        this.core = core;
    }

    public AzureusCore getCore() {
        return core;
    }

    public boolean isDHTSleeping() {
        return AERunStateHandler.isDHTSleeping();
    }

    public void setDHTSleeping(boolean sleeping) {
        if (sleeping != isDHTSleeping()) {
            long rm = AERunStateHandler.getResourceMode();

            if (sleeping) {
                AERunStateHandler.setResourceMode(rm | AERunStateHandler.RS_DHT_SLEEPING);
            } else {
                AERunStateHandler.setResourceMode(rm & ~AERunStateHandler.RS_DHT_SLEEPING);
            }
        }
    }
}
