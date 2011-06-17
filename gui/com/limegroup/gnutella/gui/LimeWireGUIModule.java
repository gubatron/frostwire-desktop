package com.limegroup.gnutella.gui;

import com.google.inject.AbstractModule;
import com.limegroup.gnutella.ActivityCallback;
import com.limegroup.gnutella.gui.bugs.BugManager;
import com.limegroup.gnutella.gui.bugs.DeadlockBugManager;
import com.limegroup.gnutella.gui.bugs.FatalBugManager;
import com.limegroup.gnutella.gui.bugs.SessionInfo;
import com.limegroup.gnutella.gui.options.panes.BugsPaneItem;

public class LimeWireGUIModule extends AbstractModule {

    @Override
    protected void configure() {
        //DPINJ: Temporary measures...
        requestStaticInjection(GuiCoreMediator.class);        
        requestStaticInjection(BugsPaneItem.class);
        requestStaticInjection(BugManager.class);
        requestStaticInjection(DeadlockBugManager.class);
        requestStaticInjection(FatalBugManager.class);
        
        bind(ActivityCallback.class).to(VisualConnectionCallback.class);
        
        bind(LocalClientInfoFactory.class).to(LocalClientInfoFactoryImpl.class);
    }
}