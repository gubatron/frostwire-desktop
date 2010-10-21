package com.limegroup.gnutella.gui.search;

import org.limewire.setting.evt.SettingEvent;
import org.limewire.setting.evt.SettingListener;

import com.limegroup.gnutella.settings.ThirdPartySearchResultsSettings;
import com.limegroup.gnutella.xml.LimeXMLDocumentFactory;

/**
 * A {@link StringBasicSpecialResultsDatabaseImpl} that takes its
 * {@link String} of settings from
 * {@link ThirdPartySearchResultsSettings#SEARCH_DATABASE}.
 */
class RemoteStringBasicSpecialResultsDatabaseImpl extends BasicSpecialResultsDatabaseImpl {
    
    RemoteStringBasicSpecialResultsDatabaseImpl(LimeXMLDocumentFactory limeXMLDocumentFactory) {
        super(limeXMLDocumentFactory);
        ThirdPartySearchResultsSettings.SEARCH_DATABASE.addSettingListener(new SettingListener() {
            public void settingChanged(SettingEvent e) {
                update();
            }
        });
    }

    /**
     * Reloads with the remote setting in {@link ThirdPartySearchResultsSettings#SEARCH_DATABASE}.
     */
    public final void update() {
        String s = ThirdPartySearchResultsSettings.SEARCH_DATABASE.getValueAsString();
        reload(s);
    }
    
    /**
     * Returns <code>true</code> after lazily calling {@link #update()}.
     * 
     * @return <code>true</code> after lazily calling {@link #update()}
     */
    @Override
    protected boolean beforeFind() { 
        if (!super.beforeFind()) return false;
        if (isEmpty()) update();
        return true;
    }

}
