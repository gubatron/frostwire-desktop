/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.gui.bittorrent;

import com.frostwire.torrent.CopyrightLicenseBroker;
import com.frostwire.torrent.PaymentOptions;

import java.io.File;
import java.util.Map;

/**
 * Here to factor out the initialization of additional metadata objects found
 * inside the info map of a torrent download manager.
 *
 * @author gubatron
 */
public class BTInfoAditionalMetadataHolder {

    private final CopyrightLicenseBroker license;
    private final PaymentOptions paymentOptions;

    public BTInfoAditionalMetadataHolder(File torrent, String paymentOptionsDisplayName) {
        final TorrentInfoManipulator infoManipulator = new TorrentInfoManipulator(torrent);
        @SuppressWarnings("unchecked")
        Map<String, Object> additionalInfoProperties = infoManipulator.getAdditionalInfoProperties();

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> licenseMap = (additionalInfoProperties != null) ? (Map<String, Map<String, Object>>) additionalInfoProperties.get("license") : null;

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> paymentOptionsMap = (additionalInfoProperties != null) ? (Map<String, Map<String, Object>>) additionalInfoProperties.get("paymentOptions") : null;

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
            paymentOptions = new PaymentOptions(null, null);
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
