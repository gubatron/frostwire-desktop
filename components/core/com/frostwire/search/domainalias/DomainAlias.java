package com.frostwire.search.domainalias;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.frostwire.concurrent.DefaultThreadFactory;
import com.frostwire.util.HttpClient;
import com.frostwire.util.HttpClientFactory;

public class DomainAlias {

    public static final ExecutorService executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            30L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(),
            new DefaultThreadFactory("DomainAliasCheckers", true));

    public final String original;
    public final String alias; 
    private DomainAliasState aliasState;
    private long lastChecked;
    private int failedAttempts;

    private final static long DOMAIN_ALIAS_CHECK_INTERVAL_MILLISECONDS = 5000;//time to wait before we check again this domain alias after it's been marked offline.
    private final static int DOMAIN_ALIAS_CHECK_TIMEOUT_MILLISECONDS = 3500;

    public DomainAlias(String original, String alias) {
        this.original = original;
        this.alias = alias;
        lastChecked = -1;
        aliasState = DomainAliasState.UNCHECKED;
        failedAttempts = 0;
    }

    public long getLastChecked() {
        return lastChecked;
    }

    public DomainAliasState getState() {
        return aliasState;
    }

    public String getOriginal() {
        return original;
    }

    public String getAlias() {
        return alias;
    }

    public void checkStatus(final DomainAliasPongListener pongListener) {
        if (aliasState != DomainAliasState.CHECKING) {
            long timeSinceLastCheck = System.currentTimeMillis() - lastChecked;

            if (timeSinceLastCheck > DOMAIN_ALIAS_CHECK_INTERVAL_MILLISECONDS) {
                Thread r = new Thread("DomainAlias-Pinger (" + original + "=>" + alias + ")") {
                    @Override
                    public void run() {
                        pingAlias(pongListener);
                    }
                };
                executor.execute(r);
            } else {
                System.out.println("DomainAlias.checkStatus: Too early to ping again " + alias);
            }
        } else {
            System.out.println("DomainAlias.checkStatus: Not checking " + alias +" because it's still CHECKING");
        }
    }

    private void pingAlias(final DomainAliasPongListener pongListener) {
        aliasState = DomainAliasState.CHECKING;
        lastChecked = System.currentTimeMillis();
        if (ping(alias)) {
            System.out.println(alias + " Domain alias pong! ");
            aliasState = DomainAliasState.ONLINE;  
            failedAttempts = 0;
            pongListener.onDomainAliasPong(this);
        } else {
            pingFailed();
            pongListener.onDomainAliasPingFailed(this);
        }
    }
    
    private static boolean ping(String domainName) {
        boolean pong = false;
        try {
            HttpClient httpClient = HttpClientFactory.newDefaultInstance();
            String string = httpClient.get("http://"+domainName, DOMAIN_ALIAS_CHECK_TIMEOUT_MILLISECONDS);
            pong = string != null && string.length()> 0;
        } catch (Throwable t) {
            System.out.println("No pong from " + domainName + ".\n");
        }
        return pong;
    }
    
    private void pingFailed() {
        aliasState = DomainAliasState.OFFLINE;
        failedAttempts++;
    }
    
    public void markOffline() {
        aliasState = DomainAliasState.OFFLINE;
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
    
    @Override
    public String toString() {
        return "("+original+" => "+alias + " [" + aliasState + "])";
    }

    public void reset() {
        this.aliasState = DomainAliasState.UNCHECKED;
        this.failedAttempts = 0;
        this.lastChecked = -1;
    }
}