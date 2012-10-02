package com.limegroup.gnutella.gui;

import com.frostwire.gui.mplayer.MPlayerComponent;
import com.frostwire.gui.mplayer.MPlayerComponentFactory;

public class MediaComponentMediator {

    private static MediaComponentMediator _instance;
    private final MPlayerComponent mplayerComponent;

    private MediaComponentMediator() {
        mplayerComponent = MPlayerComponentFactory.instance().createPlayerComponent();
    }
    
    public static synchronized MediaComponentMediator instance() {
        if (_instance == null)
            _instance = new MediaComponentMediator();
        return _instance;
    }
    
    public MPlayerComponent GetMPlayerComponent() {
        return mplayerComponent;
    }
}
