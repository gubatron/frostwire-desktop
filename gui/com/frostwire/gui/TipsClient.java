package com.frostwire.gui;

import java.util.LinkedList;
import java.util.List;

import org.limewire.util.OSUtils;

import sun.awt.windows.WComponentPeer;

import com.frostwire.gui.bittorrent.BTDownload;
import com.frostwire.gui.bittorrent.BTDownloadMediator;
import com.frostwire.tips.AppStateContext;
import com.frostwire.tips.AppStateContext.Download;
import com.frostwire.tips.TipsEngine;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.LanguageUtils;
import com.limegroup.gnutella.util.FrostWireUtils;

public final class TipsClient {

    private final TipsEngine engine;

    private static final TipsClient instance = new TipsClient();

    public static TipsClient instance() {
        return instance;
    }

    private TipsClient() {
        this.engine = new TipsEngine(OSUtils.getFullOS(),FrostWireUtils.getFrostWireVersion(),LanguageUtils.getLocaleString());
    }

    public void call() {
        MouseTracker.instance().track();

        if (engine.processAllowed()) {
            engine.process(buildContext());
        }
    }

    private AppStateContext buildContext() {
        AppStateContext context = new AppStateContext();

        context.setParentHwnd(getParentHwnd());
        context.setParentActive(GUIMediator.getAppFrame().isActive());
        context.setIdleTimeMillis(MouseTracker.instance().idleTimeMillis());
        context.setDownloads(getDownloads());
        context.setNoAnalyze(false);

        return context;
    }

    @SuppressWarnings("deprecation")
    private long getParentHwnd() {
        return ((WComponentPeer) GUIMediator.getAppFrame().getPeer()).getHWnd();
    }

    private List<Download> getDownloads() {
        List<Download> downloads = new LinkedList<Download>();
        List<BTDownload> btDownloads = BTDownloadMediator.instance().getDownloads();

        for (BTDownload btDownload : btDownloads) {
            Download d = new Download();
            d.startTime = btDownload.getDateCreated().getTime();
            d.progress = btDownload.getProgress();
            d.speed = (long) btDownload.getDownloadSpeed();
        }

        return downloads;
    }
}
