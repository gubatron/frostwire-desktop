package com.frostwire.search.domainalias;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simply responsible for maintaining the list of domain aliases and their states for
 * a single domain.
 * 
 * @author gubatron
 *
 */
public class DomainAliasManager {

    private final String defaultDomain;

    private boolean defaultDomainOnline;
    
    private List<DomainAlias> aliases;

    public DomainAliasManager(String defaultDomain) {
        this(defaultDomain, Collections.<DomainAlias> emptyList());
    }

    public DomainAliasManager(String defaultDomain, List<DomainAlias> aliases) {
        this.defaultDomain = defaultDomain;
        this.aliases = Collections.synchronizedList(aliases);
        this.defaultDomainOnline = true;
    }

    public String getDefaultDomain() {
        return defaultDomain;
    }

    public List<DomainAlias> getAliases() {
        return aliases;
    }

    public void updateAliases(final List<String> aliasNames) {
        List<DomainAlias> newAliasList = new ArrayList<DomainAlias>();
        
        if (aliasNames != null && aliasNames.size() > 0) {
            for (String alias : aliasNames) {
                DomainAlias domainAlias = new DomainAlias(defaultDomain, alias);
                if (!aliases.contains(domainAlias)) {
                    newAliasList.add(domainAlias);
                } else {
                    newAliasList.add(aliases.get(aliases.indexOf(domainAlias)));
                }
            }
            Collections.shuffle(newAliasList);

            if (newAliasList.size() > 0) {
                synchronized (aliases) {
                    aliases = Collections.synchronizedList(newAliasList);
                }
            }
        }
    }

    public void markDomainOffline(String offlineDomain) {
        if (offlineDomain.equals(defaultDomain)) {
            defaultDomainOnline = false;
        } else {
            for (DomainAlias domainAlias : aliases) {
                if (domainAlias.alias.equals(offlineDomain)) {
                    domainAlias.markOffline();
                }
            }
        }
    }
    
    /**
     * Until it doesn't know the default domain name is not accesible
     * it will keep re
     * @return
     */
    public String getDomainNameToUse() {
        String result = defaultDomain;
        if (!defaultDomainOnline) {
            String onlineAlias = getNextOnlineAlias();
            if (onlineAlias != null) {
                result = onlineAlias;
            }
        }
        return result;
    }

    /**
     * Returns the next domain considered as online on the manager's list.
     * null if the current list is empty, null or nobody is online.
     * 
     * This method will not check, checks must have been done in advance
     * 
     * @return
     */
    private String getNextOnlineAlias() {
        String result = null;
        if (aliases != null && !aliases.isEmpty()) {
            for (DomainAlias alias : aliases) {
                if (alias.getState() == DomainAliasState.ONLINE) {
                    result = alias.getAlias();
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Will try to ping all DomainAliases that have not been pinged recently to update
     * their statuses.
     */
    public void checkStatuses() {
        if (aliases != null && !aliases.isEmpty()) {
            List<DomainAlias> toRemove = new ArrayList<DomainAlias>();
            
            synchronized(aliases) {
                for (DomainAlias alias : aliases) {
                    if (alias.getFailedAttempts() <= 3) {
                        alias.checkStatus();
                    } else {
                        toRemove.add(alias);
                    }
                }
            }
            
            if (!toRemove.isEmpty()) {
                aliases.removeAll(toRemove);
            }
        } else {
            //be borne again.
            resetAliases();
        }
    }

    private void resetAliases() {
        defaultDomainOnline = true;
        if (aliases != null && aliases.size() > 0) {
            for (DomainAlias alias : aliases) {
                alias.reset();
            }
        }
    }
}