package com.frostwire.search.domainalias;

import java.util.concurrent.ExecutorService;

import org.limewire.concurrent.ExecutorsHelper;

import com.frostwire.util.HttpClient;
import com.frostwire.util.HttpClientFactory;

public class DomainAlias {

    public static final ExecutorService executor = ExecutorsHelper.newFixedSizeThreadPool(5, "DomainAliasCheckers");

    public final String original;
    public final String alias;
    private DomainAliasState state;
    private long lastChecked;
    private int failedAttempts;

    private final long DOMAIN_ALIAS_CHECK_INTERVAL_MILLISECONDS = 60000;//test with one minute //600000; //10 minutes in production.
    private final int DOMAIN_ALIAS_CHECK_TIMEOUT_SECONDS = 3;

    public DomainAlias(String original, String alias) {
        this.original = original;
        this.alias = alias;
        lastChecked = -1;
        state = DomainAliasState.UNCHECKED;
        failedAttempts = 0;
    }

    public long getLastChecked() {
        return lastChecked;
    }

    public DomainAliasState getState() {
        return state;
    }

    public String getOriginal() {
        return original;
    }

    public String getAlias() {
        return alias;
    }

    public void checkStatus() {
        if (state != DomainAliasState.CHECKING) {
            long timeSinceLastCheck = System.currentTimeMillis() - lastChecked;

            if (timeSinceLastCheck > DOMAIN_ALIAS_CHECK_INTERVAL_MILLISECONDS) {
                state = DomainAliasState.CHECKING;
                Thread r = new Thread("DomainAlias-Pinger (" + original + "=>" + alias + ")") {
                    @Override
                    public void run() {
                        ping();
                    }
                };
                executor.execute(r);
            }
        }
    }

    private void ping() {
        lastChecked = System.currentTimeMillis();
        final HttpClient client = HttpClientFactory.newDefaultInstance();
        final String result = client.get("http://" + alias, DOMAIN_ALIAS_CHECK_TIMEOUT_SECONDS);
        state = DomainAliasState.ONLINE;
        if (result == null) {
            state = DomainAliasState.OFFLINE;
            failedAttempts++;
        }
    }

    public void markOffline() {
        state = DomainAliasState.OFFLINE;
        lastChecked = System.currentTimeMillis();
    }
    
    public int getFailedAttempts() {
        return failedAttempts;
    }

    @Override
    public boolean equals(Object obj) {
        DomainAlias other = (DomainAlias) obj;
        return this.original.equals(other.original) && this.alias.equals(other.alias);
    }

    @Override
    public int hashCode() {
        return (this.original.hashCode() * 29) + (this.alias.hashCode() * 13);
    }
}