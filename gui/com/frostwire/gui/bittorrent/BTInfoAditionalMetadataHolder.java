package com.frostwire.gui.bittorrent;

import java.util.Map;

import org.gudy.azureus2.core3.download.DownloadManager;

import com.frostwire.torrent.CopyrightLicenseBroker;
import com.frostwire.torrent.PaymentOptions;

/**
 * Here to factor out the initialization of additional metadata objects found
 * inside the info map of a torrent download manager.
 * @author gubatron
 *
 */
public class BTInfoAditionalMetadataHolder {
    private final CopyrightLicenseBroker license;
    private final PaymentOptions paymentOptions;

    public BTInfoAditionalMetadataHolder(DownloadManager downloadManager, String paymentOptionsDisplayName) {
        final TorrentInfoManipulator infoManipulator = new TorrentInfoManipulator(downloadManager);
        @SuppressWarnings("unchecked")
        Map<String, Object> additionalInfoProperties = infoManipulator.getAdditionalInfoProperties();
        
        @SuppressWarnings("unchecked")
        Map<String,Map<String,Object>> licenseMap = (additionalInfoProperties != null) ? (Map<String,Map<String,Object>>) additionalInfoProperties.get("license") : null;

        @SuppressWarnings("unchecked")
        Map<String,Map<String,Object>> paymentOptionsMap = (additionalInfoProperties != null) ? (Map<String,Map<String,Object>>) additionalInfoProperties.get("paymentOptions") : null;
        
        boolean hasLicense = licenseMap != null && !licenseMap.isEmpty();
        boolean hasPaymentOptions = paymentOptionsMap != null && !paymentOptionsMap.isEmpty();
        
        if (hasLicense) {
            license = new CopyrightLicenseBroker(licenseMap);
        } else {
            license = null;
        }
        
        if (hasPaymentOptions) {
            paymentOptions = new PaymentOptions(paymentOptionsMap);
        } else {
            paymentOptions = new PaymentOptions(null,null,null,null);
        }
        paymentOptions.setItemName(paymentOptionsDisplayName);
    }
    
    public CopyrightLicenseBroker getLicenseBroker() {
        return license;
    }
    
    public PaymentOptions getPaymentOptions() {
        return paymentOptions;
    }
    
}
