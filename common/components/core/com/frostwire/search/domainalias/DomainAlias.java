package com.frostwire.search.domainalias;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.limewire.concurrent.ExecutorsHelper;
import org.limewire.concurrent.ThreadPoolExecutor;

public class DomainAlias {

    public static final ExecutorService executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            30L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(),
            ExecutorsHelper.daemonThreadFactory("DomainAliasCheckers"));//ExecutorsHelper.newThreadPool("DomainAliasCheckers");

    public final String original;
    public final String alias; 
    private DomainAliasState aliasState;
    private long lastChecked;
    private int failedAttempts;

    private final static long DOMAIN_ALIAS_CHECK_INTERVAL_MILLISECONDS = 8000;//test with one minute //600000; //10 minutes in production.
    private final static int DOMAIN_ALIAS_CHECK_TIMEOUT_MILLISECONDS = 4000;

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

    public void checkStatus() {
        if (aliasState != DomainAliasState.CHECKING) {
            long timeSinceLastCheck = System.currentTimeMillis() - lastChecked;

            if (timeSinceLastCheck > DOMAIN_ALIAS_CHECK_INTERVAL_MILLISECONDS) {
                Thread r = new Thread("DomainAlias-Pinger (" + original + "=>" + alias + ")") {
                    @Override
                    public void run() {
                        pingAlias();
                    }
                };
                executor.execute(r);
            }
        }
    }

    private void pingAlias() {
        aliasState = DomainAliasState.CHECKING;
        lastChecked = System.currentTimeMillis();
        if (ping(alias)) {
            aliasState = DomainAliasState.ONLINE;  
            failedAttempts = 0;
        } else {
            pingFailed();
        }
    }
    
    private static boolean ping(String domainName) {
        boolean connected = false;
        try {
            //try reacheability test first (this tries a ICMP ping and a tcp connection to echo port.
            InetAddress address = InetAddress.getByName(domainName);
            boolean reachable = address.isReachable(DomainAlias.DOMAIN_ALIAS_CHECK_TIMEOUT_MILLISECONDS);
            
            if (reachable) {
                connected = true;
            } else {
                //but the server may not attend to pings or have echo port on.
                //so we then try a TCP connection with port 80.
                InetSocketAddress inetSocketAddress = new InetSocketAddress(domainName, 80);
                SocketChannel sc = SocketChannel.open();
                sc.configureBlocking(true);
                connected = sc.connect(inetSocketAddress);
                sc.close();
            }
        } catch (IOException e) {
            connected = false;
        }
        return connected;
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