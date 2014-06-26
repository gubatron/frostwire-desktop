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

package com.frostwire.torrent;

import java.util.HashMap;
import java.util.Map;

import com.frostwire.util.StringUtils;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public class PaymentOptions extends AbstractMappable<String, Map<String, String>> {
    /** BitCoin URI, see BIP-0021 - https://github.com/bitcoin/bips/blob/master/bip-0021.mediawiki 
     * bitcoinurn     = "bitcoin:" bitcoinaddress [ "?" bitcoinparams ]
     * bitcoinaddress = base58 *base58
     * Example: bitcoin:175tWpb8K1S7NmH4Zx6rewF9WQrcZv245W
     * 
     * To be serialized as dictionary in the .torrent as follows
     *     paymentOptions: {
     *        bitcoin: "bitcoin:14F6JPXK2fR5b4gZp3134qLRGgYtvabMWL",
     *        litecoin: "litecoin:LiYp3Dg11N5BgV8qKW42ubSZXFmjDByjoV",
     *        dogecoin: "dogecoin:DNnZb9Xn5cnShg2or1GdgdvfqHmS54AjEm",
     *        paypalUrl: "http://frostwire.com/donate"
     *     }
     * 
     */
    public final String bitcoin;
    public final String litecoin;
    public final String dogecoin;

    public enum PaymentMethod {
        BITCOIN, LITECOIN, DOGECOIN, PAYPAL
    }

    /** Simply a valid email address for creating a paypal payment form */
    public final String paypalUrl;
    
    public String itemName;

    public PaymentOptions() {
        bitcoin = null;
        litecoin = null;
        dogecoin = null;
        paypalUrl = null;
    }

    public PaymentOptions(String bitcoin, String litecoin, String dogecoin, String paypal) {
        this.bitcoin = bitcoin;
        this.litecoin = litecoin;
        this.dogecoin = dogecoin;
        this.paypalUrl = paypal;
    }

    public PaymentOptions(Map<String, Map<String, Object>> paymentOptionsMap) {
        Map<String, Object> paymentOptions = paymentOptionsMap.get("paymentOptions");
        this.bitcoin = getStringFromEncodedMap("bitcoin", paymentOptions);
        this.litecoin = getStringFromEncodedMap("litecoin", paymentOptions);
        this.dogecoin = getStringFromEncodedMap("dogecoin", paymentOptions);
        this.paypalUrl = getStringFromEncodedMap("paypalUrl", paymentOptions);
    }

    public Map<String, Map<String, String>> asMap() {
        Map<String, String> innerMap = new HashMap<String, String>();
        if (!StringUtils.isNullOrEmpty(bitcoin)) {
            innerMap.put("bitcoin", bitcoin);
        }
        if (!StringUtils.isNullOrEmpty(litecoin)) {
            innerMap.put("litecoin", litecoin);
        }
        if (!StringUtils.isNullOrEmpty(dogecoin)) {
            innerMap.put("dogecoin", dogecoin);
        }
        if (!StringUtils.isNullOrEmpty(paypalUrl)) {
            innerMap.put("paypalUrl", paypalUrl);
        }

        Map<String, Map<String, String>> paymentOptions = new HashMap<String, Map<String, String>>();

        if (!innerMap.isEmpty()) {
            paymentOptions.put("paymentOptions", innerMap);
        }
        return paymentOptions;
    }
    
    public void setItemName(String name) {
        itemName = name;
    }
    
    public String getItemName() {
        return itemName;
    }
}