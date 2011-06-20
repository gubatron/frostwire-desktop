package com.limegroup.gnutella.licenses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public enum LicenseType {    
    NO_LICENSE(""),
    CC_LICENSE (CCConstants.CC_URI_PREFIX),
    DRM_LICENSE (""), 
    GPL ("http://www.gnu.org/copyleft/gpl.html"),
    LGPL ("http://www.gnu.org/copyleft/lgpl.html"),
    APACHE_BSD ("http://opensource.org/licenses/apache2.0.php"),
    MIT_X ("http://opensource.org/licenses/mit-license.php"),
    FDL ("http://www.gnu.org/copyleft/fdl.html"), 
    ARTISTIC ("http://www.opensource.org/licenses/artistic-license.php"), 
    PUBLIC_DOMAIN ("http://www.public-domain.org"), 
    SHAREWARE ("http://en.wikipedia.org/wiki/Shareware");
    
    private final String keyword;
    private final List<String> indivList;
    
    private LicenseType(String keyword) {
        this.keyword = keyword;
        List<String> indiv = new ArrayList<String>(1);
        indiv.add(keyword);
        this.indivList = Collections.unmodifiableList(indiv);
    }
    
    public boolean isDRMLicense() {
        return this == DRM_LICENSE;
    }
    
    public List<String> getIndivisibleKeywords() {
        return indivList;
    }

    /**
     * Determines the license type based on the a license type and the actual license
     */
    public static LicenseType determineLicenseType(String license, String type) {
        if (hasCCLicense(license, type))
            return CC_LICENSE;
        
        // the other licenses do not have any special requirements 
        // for the license or type field (yet)
        for(LicenseType licenseType : values()) {
            if(licenseType.keyword.equals(type))
                return licenseType;
        }
        
        return NO_LICENSE;
    }
    
    private static boolean hasCCLicense(String license, String type) {
        if(license != null)
            license = license.toLowerCase(Locale.US);
        return (type != null && type.equals(CCConstants.CC_URI_PREFIX)) ||
               (license != null && license.indexOf(CCConstants.CC_URI_PREFIX) != -1
                                && license.indexOf(CCConstants.URL_INDICATOR) != -1)
               ;
    }
}
