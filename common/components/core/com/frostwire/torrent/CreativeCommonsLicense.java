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

package com.frostwire.torrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.limegroup.gnutella.gui.I18n;


/** TODO: Refactor this to have Licenses modeled with OO hierarchy after we have proof of concept working. 
 * This is a quick-n-dirty implementation. */
public class CreativeCommonsLicense implements Mappable<String,Map<String,String>> {
    public final String licenseUrl;
    
    public final String attributionTitle;
    public final String attributionAuthor;
    public final String attributionUrl;
    
    public static final String CC_VERSION = "4.0";
    
    /** attribution */
    public static final String CC_BY = "http://creativecommons.org/licenses/by/"+ CC_VERSION + "/";
    
    /** attribution - share alike */
    public static final String CC_BY_SA = "http://creativecommons.org/licenses/by-sa/"+ CC_VERSION + "/";

    /** attribution - no derivatives */
    public static final String CC_BY_ND = "http://creativecommons.org/licenses/by-nd/"+ CC_VERSION + "/";
    
    /** attribution - non commercial */
    public static final String CC_BY_NC = "http://creativecommons.org/licenses/by-nc/"+ CC_VERSION + "/";
    
    /** attribution - non commercial - share alike */
    public static final String CC_NC_SA = "http://creativecommons.org/licenses/by-nc-sa/"+ CC_VERSION + "/";
    
    /** attribution - non commercial - no derivatives */
    public static final String CC_NC_ND = "http://creativecommons.org/licenses/by-nc-nd/"+ CC_VERSION + "/";

    public static final List<String> validLicenses;
    
    public final String BY_WORD = I18n.tr("Attribution");
    public final String SA_WORD = I18n.tr("Share-Alike");
    public final String ND_WORD = I18n.tr("NoDerivatives");
    public final String NC_WORD = I18n.tr("NonCommercial");
    public final String INTERNATIONAL_LICENSE = I18n.tr("International License");
    
    static {
        validLicenses = new ArrayList<>();
        validLicenses.add(CC_BY);
        validLicenses.add(CC_BY_SA);
        validLicenses.add(CC_BY_ND);
        validLicenses.add(CC_BY_NC);
        validLicenses.add(CC_NC_SA);
        validLicenses.add(CC_NC_ND);
    }
    
    public CreativeCommonsLicense(boolean shareAlike, boolean nonCommercial, boolean noDerivatives, String attributionTitle, String attributionAuthor, String attributionURL) {
        final String licenseUrl = getValidLicenseString(shareAlike, nonCommercial, noDerivatives);
        if (!isInvalidLicense(licenseUrl)) {
            this.licenseUrl = licenseUrl;
            this.attributionTitle = attributionTitle;
            this.attributionAuthor = attributionAuthor;
            this.attributionUrl = attributionURL;
        } else {
            throw new IllegalArgumentException("The given license string is invalid.");
        }
    }
    
    /** Deserialization constructor */
    public CreativeCommonsLicense(Map<String,Map<String,String>> map) {
         Map<String,String> innerMap = map.get("creative-commons");
         this.licenseUrl = innerMap.get("licenseUrl");
         this.attributionTitle = innerMap.get("attributionTitle");
         this.attributionAuthor = innerMap.get("attributionAuthor");
         this.attributionUrl = innerMap.get("attributionUrl");
    }

    public Map<String, Map<String,String>> asMap() {
        Map<String,Map<String,String>> container = new HashMap<>();
        Map<String,String> innerMap = new HashMap<>();
        innerMap.put("licenseUrl", this.licenseUrl);
        innerMap.put("attributionTitle", this.attributionTitle);
        innerMap.put("attributionAuthor", this.attributionAuthor);
        innerMap.put("attributionUrl", this.attributionUrl);
        
        container.put("creative-commons", innerMap);
        return container;
    }
    
    public String getLicenseShortCode() {
       int offsetStart = "http://creativecommons.org/licenses/".length();
       int offsetEnd = licenseUrl.indexOf("/", offsetStart+1);
       return licenseUrl.substring(offsetStart, offsetEnd);
    }
    
    public String getLicenseName() {
        String shortCode = getLicenseShortCode();
        String licenseName = shortCode.replaceAll("by",BY_WORD).
                replaceAll("nc",NC_WORD).
                replaceAll("nd", ND_WORD).
                replaceAll("sa",SA_WORD);
        
        return "Creative Commons Attribution" + licenseName + " " + CC_VERSION + INTERNATIONAL_LICENSE;
    }
    
    private static boolean isInvalidLicense(String licenseStr) {
        return licenseStr == null || licenseStr.isEmpty() || !validLicenses.contains(licenseStr);
    }

    /** This method makes sure you input a valid license, even if you make a mistake combining these parameters */
    public static String getValidLicenseString(boolean shareAlike, boolean nonCommercial, boolean noDerivatives) {
        if (nonCommercial && shareAlike) {
            noDerivatives = false;
        } else if (nonCommercial && noDerivatives) {
            shareAlike = false;
        } else if (shareAlike) {
            noDerivatives = false;
        }
        
        String license = "by-" + (nonCommercial ? "nc-" : "") + (shareAlike ? "sa" : "") + (noDerivatives ? "-nd" : "");
        license = license.replace("--", "-");
        if (license.endsWith("-")) {
            license = license.substring(0, license.length()-1);
        }
        return "http://creativecommons.org/licenses/" + license + "/" + CC_VERSION + "/";
    }

    private static void testValidLicenseStringGeneration() {
        String license = getValidLicenseString(false, false, false);
        System.out.println(license + " is valid license? " + !isInvalidLicense(license));
        
        license = getValidLicenseString(false, false, true);
        System.out.println(license + " is valid license? " + !isInvalidLicense(license));
        
        license = getValidLicenseString(false, true, false);
        System.out.println(license + " is valid license? " + !isInvalidLicense(license));
        
        license = getValidLicenseString(false, true, true);
        System.out.println(license + " is valid license? " + !isInvalidLicense(license));
        
        license = getValidLicenseString(true, false, false);
        System.out.println(license + " is valid license? " + !isInvalidLicense(license));
        
        license = getValidLicenseString(true, false, true);
        System.out.println(license + " is valid license? " + !isInvalidLicense(license));

        license = getValidLicenseString(true, true, false);
        System.out.println(license + " is valid license? " + !isInvalidLicense(license));

        license = getValidLicenseString(true, true, true);
        System.out.println(license + " is valid license? " + !isInvalidLicense(license));
    }
    
    public static void main(String[] arg) {
        testValidLicenseStringGeneration();
    }
}