package com.limegroup.gnutella;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.limegroup.gnutella.settings.ConnectionSettings;
import com.limegroup.gnutella.settings.UploadSettings;
import com.limegroup.gnutella.uploader.UploadSlotManager;

@Singleton
public class UploadServicesImpl implements UploadServices {
    
    public static boolean IS_SEEDING_HOSTILES_TXT = false;
    
    private final Provider<UploadSlotManager> uploadSlotManager;
    
    @Inject
    public UploadServicesImpl(
            Provider<UploadSlotManager> uploadSlotManager) {
        this.uploadSlotManager = uploadSlotManager;
    }
    

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.UploadServices#hasActiveUploads()
     */
    public boolean hasActiveUploads() {
        uploadSlotManager.get().measureBandwidth();
        try {
            return uploadSlotManager.get().getMeasuredBandwidth() > 0;
        } catch (InsufficientDataException ide) {
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.UploadServices#getRequestedUploadSpeed()
     */
    public float getRequestedUploadSpeed() {
        return 0;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.UploadServices#getNumUploads()
     */
    public int getNumUploads() {
        return 0;//uploadManager.get().uploadsInProgress();
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.UploadServices#getNumQueuedUploads()
     */
    public int getNumQueuedUploads() {
        return 0;//uploadManager.get().getNumQueuedUploads();
    }
    
    public boolean isSeedingHostilesTxt() {
        return IS_SEEDING_HOSTILES_TXT;
    }


}
