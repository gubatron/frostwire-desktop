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

    private List<DomainAlias> aliases;

    public DomainAliasManager(String defaultDomain) {
        this(defaultDomain, Collections.<DomainAlias> emptyList());
    }

    public DomainAliasManager(String defaultDomain, List<DomainAlias> aliases) {
        this.defaultDomain = defaultDomain;
        this.aliases = aliases;
    }

    public String getDefaultDomain() {
        return defaultDomain;
    }

    public List<DomainAlias> getAliases() {
        return aliases;
    }

    public void updateAliases(List<String> aliasNames) {
        if (aliasNames != null && aliasNames.size() > 0) {
            for (String alias : aliasNames) {
                //add new aliases if new are to be found.
                DomainAlias domainAlias = new DomainAlias(defaultDomain, alias);
                if (!aliases.contains(domainAlias)) {
                    aliases.add(domainAlias);
                }
            }
        }
        Collections.shuffle(aliases);
    }

    public void markAliasOffline(String offlineAlias) {
        for (DomainAlias domainAlias : aliases) {
            if (domainAlias.alias.equals(offlineAlias)) {
                domainAlias.markOffline();
            }
        }
    }

    /**
     * Returns the next domain considered as online on the manager's list.
     * null if the current list is empty, null or nobody is online.
     * 
     * This method will not check, checks must have been done in advance
     * 
     * @return
     */
    public String getOnlineAlias() {
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
            for (DomainAlias alias : aliases) {
                if (alias.getFailedAttempts() <= 3) {
                    alias.checkStatus();
                } else {
                    toRemove.add(alias);
                }
            }
            
            if (!toRemove.isEmpty()) {
                aliases.removeAll(toRemove);
            }
        }
    }
}