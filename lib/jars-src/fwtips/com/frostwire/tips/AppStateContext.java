package com.frostwire.tips;

import java.util.List;

public final class AppStateContext {

    private long parentHwnd;
    private boolean parentActive;
    private long mouseIdleTimeMillis;
    private List<Download> downloads;
    private boolean noAnalyze;

    public AppStateContext() {
        this.parentHwnd = 0;
    }

    public long getParentHwnd() {
        return parentHwnd;
    }

    public void setParentHwnd(long parentHwnd) {
        this.parentHwnd = parentHwnd;
    }

    public boolean isParentActive() {
        return parentActive;
    }

    public void setParentActive(boolean parentActive) {
        this.parentActive = parentActive;
    }

    public long getMouseIdleTimeMillis() {
        return mouseIdleTimeMillis;
    }

    public void setMouseIdleTimeMillis(long mouseIdleTimeMillis) {
        this.mouseIdleTimeMillis = mouseIdleTimeMillis;
    }

    public List<Download> getDownloads() {
        return downloads;
    }

    public void setDownloads(List<Download> downloads) {
        this.downloads = downloads;
    }

    public boolean isNoAnalyze() {
        return noAnalyze;
    }

    public void setNoAnalyze(boolean enable) {
        this.noAnalyze = enable;
    }

    public static final class Download {
        public long startTime;
        public int progress;
        public long speed;
    }
}
