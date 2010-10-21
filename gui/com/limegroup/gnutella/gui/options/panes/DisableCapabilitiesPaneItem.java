package com.limegroup.gnutella.gui.options.panes;

import java.io.IOException;

import javax.swing.JCheckBox;

import org.limewire.i18n.I18nMarker;

import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.LabeledComponent;
import com.limegroup.gnutella.settings.DHTSettings;
import com.limegroup.gnutella.settings.SSLSettings;
import com.limegroup.gnutella.settings.UltrapeerSettings;

/**
 * This class gives the user the option of whether or not to automatically
 * connect to the network when the program first starts.
 */
//2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
public final class DisableCapabilitiesPaneItem extends AbstractPaneItem {
    
    public final static String TITLE = I18n.tr("Capabilities");
    
    public final static String LABEL = I18n.tr("If your computer has a fast internet connection, FrostWire may act as an \"Ultrapeer\" and/or connect to the Mojito DHT.  FrostWire can also use a secure communications mode called TLS, which may use more CPU resources.  You can disable these if needed.");

    /** The checkbox for ultrapeer capabilities. */
    private final JCheckBox UP_CHECK_BOX = new JCheckBox();

    /** The checkbox for DHT capabilities. */
    private final JCheckBox DHT_CHECK_BOX = new JCheckBox();
    
    /** The checkbox for TLS capabilities. */
    private final JCheckBox TLS_CHECK_BOX = new JCheckBox();

    public DisableCapabilitiesPaneItem() {
        super(TITLE, LABEL);
        
        add(new LabeledComponent(I18nMarker.marktr("Disable Ultrapeer Capabilities:"),
                UP_CHECK_BOX, LabeledComponent.LEFT_GLUE).getComponent());
        add(new LabeledComponent(I18nMarker.marktr("Disable Mojito DHT Capabilities:"),
                DHT_CHECK_BOX, LabeledComponent.LEFT_GLUE).getComponent());
        
        if(!SSLSettings.isTLSDisabled()) {
            add(new LabeledComponent(I18nMarker.marktr("Disable TLS Capabilities:"),
                    TLS_CHECK_BOX, LabeledComponent.LEFT_GLUE).getComponent());
        }
    }

    public void initOptions() {
        UP_CHECK_BOX.setSelected(UltrapeerSettings.DISABLE_ULTRAPEER_MODE.getValue());
        DHT_CHECK_BOX.setSelected(DHTSettings.DISABLE_DHT_USER.getValue());
        TLS_CHECK_BOX.setSelected(!SSLSettings.TLS_INCOMING.getValue() || !SSLSettings.TLS_OUTGOING.getValue());
    }

    public boolean applyOptions() throws IOException {
        boolean upChanged = UltrapeerSettings.DISABLE_ULTRAPEER_MODE.getValue() != UP_CHECK_BOX.isSelected();
        boolean tlsServerChanged = TLS_CHECK_BOX.isSelected() != !SSLSettings.TLS_INCOMING.getValue();
        boolean isSupernode = GuiCoreMediator.getConnectionServices().isSupernode();
		UltrapeerSettings.DISABLE_ULTRAPEER_MODE.setValue(UP_CHECK_BOX.isSelected());
        DHTSettings.DISABLE_DHT_USER.setValue(DHT_CHECK_BOX.isSelected());
        SSLSettings.TLS_INCOMING.setValue(!TLS_CHECK_BOX.isSelected());
        SSLSettings.TLS_OUTGOING.setValue(!TLS_CHECK_BOX.isSelected());
        
        if((tlsServerChanged || (upChanged && UP_CHECK_BOX.isSelected()) && isSupernode)) {
            GuiCoreMediator.getConnectionServices().disconnect();
            GuiCoreMediator.getConnectionServices().connect();
        }
        return false;
    }
	
    public boolean isDirty() {
        return UltrapeerSettings.DISABLE_ULTRAPEER_MODE.getValue() != UP_CHECK_BOX.isSelected() 
            || DHTSettings.DISABLE_DHT_USER.getValue() != DHT_CHECK_BOX.isSelected()
            || (!SSLSettings.TLS_INCOMING.getValue() && !SSLSettings.TLS_OUTGOING.getValue())
                   != TLS_CHECK_BOX.isSelected();
    }	
}
