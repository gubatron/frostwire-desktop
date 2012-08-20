package com.frostwire.gui.updates;

public final class UpdateMediator {

    private UpdateMessage latestMsg;

    private static UpdateMediator instance;

    public static UpdateMediator instance() {
        if (instance == null) {
            instance = new UpdateMediator();
        }
        return instance;
    }

    private UpdateMediator() {
    }

    public boolean isUpdated() {
        return true;
    }

    public String getLatestVersion() {
        return latestMsg != null ? latestMsg.getVersion() : "";
    }

    public void setUpdateMessage(UpdateMessage msg) {
        this.latestMsg = msg;
    }
}
