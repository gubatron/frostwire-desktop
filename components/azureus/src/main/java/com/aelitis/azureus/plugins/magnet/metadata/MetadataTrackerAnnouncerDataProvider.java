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