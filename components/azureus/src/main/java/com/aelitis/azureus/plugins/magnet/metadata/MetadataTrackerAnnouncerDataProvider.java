/*
 * Created by  Alden Torres (aldenml)
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

package com.aelitis.azureus.plugins.magnet.metadata;

import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncerDataProvider;

public class MetadataTrackerAnnouncerDataProvider implements TRTrackerAnnouncerDataProvider {

    @Override
    public void setPeerSources(String[] allowed_sources) {
    }

    @Override
    public boolean isPeerSourceEnabled(String peer_source) {
        return true;
    }

    @Override
    public int getUploadSpeedKBSec(boolean estimate) {
        return 0;
    }

    @Override
    public long getTotalSent() {
        return 0;
    }

    @Override
    public long getTotalReceived() {
        return 0;
    }

    @Override
    public long getRemaining() {
        return 0;
    }

    @Override
    public int getPendingConnectionCount() {
        return 0;
    }

    @Override
    public String getName() {
        return "MetadataTrackerAnnouncerDataProvider";
    }

    @Override
    public int getMaxNewConnectionsAllowed() {
        return 20;
    }

    @Override
    public long getFailedHashCheck() {
        return 0;
    }

    @Override
    public String getExtensions() {
        return null;
    }

    @Override
    public int getCryptoLevel() {
        return 0;
    }

    @Override
    public int getConnectedConnectionCount() {
        return 0;
    }
}