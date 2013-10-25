/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.frostwire.gui;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class TipsClient {

    private static final Log LOG = LogFactory.getLog(TipsClient.class);

    private final TipsEngine engine;

    private static final TipsClient instance = new TipsClient();

    public static TipsClient instance() {
        return instance;
    }

    private TipsClient() {
        this.engine = buildEngine();
    }

    public void call() {
        try {
            if (engine != null) {
                InputIdleTracker.instance().trackMouse();

                if (engine.processAllowed()) {
                    engine.process(buildContext());
                }
            }
        } catch (Throwable e) {
            LOG.warn("Error processing call in tips engine", e);
        }
    }

    private TipsEngine buildEngine() {
        try {
            return new TipsEngine(OSUtils.getFullOS(), FrostWireUtils.getFrostWireVersion(), LanguageUtils.getLocaleString());
        } catch (Throwable e) {
            LOG.warn("Error creating tips engine", e);
            return null;
        }
    }

    private AppStateContext buildContext() {
        AppStateContext context = new AppStateContext();

        context.setParentHwnd(getParentHwnd());
        context.setParentActive(GUIMediator.getAppFrame().isActive());
        context.setIdleTimeMillis(InputIdleTracker.instance().idleTimeMillis());
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
            downloads.add(d);
        }

        return downloads;
    }
}
