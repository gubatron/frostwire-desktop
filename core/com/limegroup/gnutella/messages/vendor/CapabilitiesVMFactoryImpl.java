package com.limegroup.gnutella.messages.vendor;

import java.util.HashSet;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.limegroup.gnutella.messages.FeatureSearchData;
import com.limegroup.gnutella.settings.SSLSettings;
import com.limegroup.gnutella.version.UpdateHandler;

@Singleton
public class CapabilitiesVMFactoryImpl implements CapabilitiesVMFactory {

    private final Provider<UpdateHandler> updateHandler;
    private volatile CapabilitiesVM currentCapabilities;

    @Inject
    public CapabilitiesVMFactoryImpl(
            Provider<UpdateHandler> updateHandler) {
        this.updateHandler = updateHandler;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.vendor.CapabilitiesVMFactory#getCapabilitiesVM()
     */
    public CapabilitiesVM getCapabilitiesVM() {
        if (currentCapabilities == null)
            currentCapabilities = new CapabilitiesVM(getSupportedMessages());
        return currentCapabilities;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.vendor.CapabilitiesVMFactory#updateCapabilities()
     */
    public void updateCapabilities() {
        currentCapabilities = new CapabilitiesVM(getSupportedMessages());
    }

    // ADD NEW CAPABILITIES HERE AS YOU BUILD THEM....
    /**
     * Adds all supported capabilities to the given set.
     */
    // protected for testing
    protected Set<CapabilitiesVM.SupportedMessageBlock> getSupportedMessages() {
        Set<CapabilitiesVM.SupportedMessageBlock> supported = new HashSet<CapabilitiesVM.SupportedMessageBlock>();

        CapabilitiesVM.SupportedMessageBlock smb = null;
        smb = new CapabilitiesVM.SupportedMessageBlock(
                CapabilitiesVM.FEATURE_SEARCH_BYTES,
                FeatureSearchData.FEATURE_SEARCH_MAX_SELECTOR);
        supported.add(smb);

        smb = new CapabilitiesVM.SupportedMessageBlock(
                CapabilitiesVM.LIME_UPDATE_BYTES, updateHandler.get()
                        .getLatestId());
        supported.add(smb);

        if (SSLSettings.isIncomingTLSEnabled()) {
            smb = new CapabilitiesVM.SupportedMessageBlock(
                    CapabilitiesVM.TLS_SUPPORT_BYTES, 1);
            supported.add(smb);
        }

        return supported;
    }

}
