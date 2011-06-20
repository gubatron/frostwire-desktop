package com.limegroup.gnutella.licenses;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.http.URIUtils;


/**
 * A factory for constructing Licenses based on licenses.
 */
@Singleton
public final class LicenseFactoryImpl implements LicenseFactory {
    
    private static final Log LOG = LogFactory.getLog(LicenseFactoryImpl.class);
    
    private final Provider<LicenseCache> licenseCache;
    
    @Inject
    public LicenseFactoryImpl(Provider<LicenseCache> licenseCache) {
        this.licenseCache = licenseCache;
    }
    
    public boolean isVerifiedAndValid(URN urn, String licenseString) {
        URI uri = getLicenseURI(licenseString);
        return uri != null && licenseCache.get().isVerifiedAndValid(urn, uri);
    }
    
    public String getLicenseName(String licenseString) {
        if(isCCLicense(licenseString))
            return CC_NAME;
        else
            return null;
    }
    
    public License create(String licenseString) {
        if(licenseString == null)
            return null;
        
        if(LOG.isTraceEnabled())
            LOG.trace("Attempting to create license from: " + licenseString);
        
        License license = null;
        URI uri = getLicenseURI(licenseString);
        
        // Try to get a cached version, first.
        if(uri != null)
            license = licenseCache.get().getLicense(licenseString, uri);
        
        // If the cached version didn't exist, try to make one.
        if(license == null) {
            if(isCCLicense(licenseString)) {
                if(uri != null)
                    license = new CCLicense(licenseString, uri);
                else
                    license = new BadCCLicense(licenseString);
            }
        }
        
        // set additional properties
        if (license instanceof MutableLicense) {
            ((MutableLicense)license).setLicenseName(getLicenseName(licenseString));
        }
        
        return license;
    }
    
    /** Determines if the given string can be a CC license. */
    private static boolean isCCLicense(String s) {
        return s.toLowerCase(Locale.US).indexOf(CCConstants.URL_INDICATOR) != -1;
    }
    
    /**
     * Persists the cache.
     */
    public void persistCache() {
        licenseCache.get().persistCache();
    }
    
    /**
     * Determines the URI to verify this license at from the license string.
     */
    static URI getLicenseURI(String license) {
        if(license == null)
            return null;
            
        // Look for CC first.
        URI uri = getCCLicenseURI(license);
        
//        // Then Weed.
//        if(uri == null)
//            uri = getWeedLicenseURI(license);
//            
        // ADD MORE LICENSES IN THE FORM OF
        // if( uri == null)
        //      uri = getXXXLicenseURI(license)
        // AS WE UNDERSTAND MORE...
        
        return uri;
    }
        
    /** Gets a CC license URI from the given license string. */
    private static URI getCCLicenseURI(String license) {
        license = license.toLowerCase(Locale.US);
        
        // find where the URL should begin.
        int verifyAt = license.indexOf(CCConstants.URL_INDICATOR);
        if(verifyAt == -1)
            return null;
            
        int urlStart = verifyAt + CCConstants.URL_INDICATOR.length();
        if(urlStart >= license.length())
            return null;
            
        String url = license.substring(urlStart).trim();
        URI uri = null;
        try {
            uri = URIUtils.toURI(url);

            // Make sure the scheme is HTTP.
            String scheme = uri.getScheme();
            if(scheme == null || !scheme.equalsIgnoreCase("http"))
                throw new URISyntaxException(uri.toString(), "Invalid scheme: " + scheme);
            // Make sure the scheme has some authority.
            String authority = uri.getAuthority();
            if(authority == null || authority.equals("") || authority.indexOf(' ') != -1)
                throw new URISyntaxException(uri.toString(), "Invalid authority: " + authority);
            
        } catch(URISyntaxException e) {
            URIUtils.error(e);
            uri = null;
            LOG.error("Unable to create URI", e);
        }
        
        return uri;
    }
}
  