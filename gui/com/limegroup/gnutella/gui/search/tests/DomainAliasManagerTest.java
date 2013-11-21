package com.limegroup.gnutella.gui.search.tests;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.PluginManager;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.plugins.dht.DHTPlugin;
import com.aelitis.azureus.plugins.dht.DHTPluginContact;
import com.aelitis.azureus.plugins.dht.DHTPluginOperationListener;
import com.aelitis.azureus.plugins.dht.DHTPluginValue;
import com.frostwire.AzureusStarter;
import com.frostwire.search.SearchManager;
import com.frostwire.search.SearchManagerImpl;
import com.frostwire.search.SearchManagerListener;
import com.frostwire.search.SearchPerformer;
import com.frostwire.search.SearchResult;
import com.frostwire.search.WebSearchPerformer;
import com.frostwire.search.domainalias.DomainAliasManager;
import com.frostwire.search.domainalias.DomainAliasManagerBroker;
import com.frostwire.util.SecurityUtils;
import com.frostwire.util.SignedMessage;
import com.limegroup.gnutella.gui.search.SearchEngine;

public class DomainAliasManagerTest {
    
    private static class DHTUpdateMessagePublishListener implements DHTPluginOperationListener {

        @Override
        public void starts(byte[] key) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void diversified() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void valueRead(DHTPluginContact originator, DHTPluginValue value) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void valueWritten(DHTPluginContact target, DHTPluginValue value) {
            System.out.println("Update Message Written at " + target.getName() + " " + target.getAddress().getHostString());
        }

        @Override
        public void complete(byte[] key, boolean timeout_occurred) {
            System.out.println(new String(key) + " key complete!");
            
            if (timeout_occurred) {
                System.out.println("DHT publish completed due to time out.");
            }
        }
        
    }

    public static void dhtInitializationTest() throws InterruptedException, IOException {
        AzureusStarter.start();
        AzureusCore azureusCore = null;

        while (!AzureusStarter.isAzureusCoreStarted()) {
            System.out.println("Waiting for azureus core to start...");
            Thread.sleep(1000);
        }

        azureusCore = AzureusStarter.getAzureusCore();
        PluginManager pluginManager = azureusCore.getPluginManager();
        
        if (pluginManager == null) {
            System.out.println("Could not get plugin manage.");
            return;
        }

        PluginInterface defaultPluginInterface = pluginManager.getDefaultPluginInterface();
        
        if (defaultPluginInterface == null) {
            System.out.println("Could not get default plugin interface.");
            return;
        }
        
        DHTPlugin dhtPlugin = (DHTPlugin)
                defaultPluginInterface.getPluginManager().getPluginInterfaceByClass(
                            DHTPlugin.class);
        
        if (dhtPlugin != null) {
            String dhtKey = "http://update.frostwire.com/|2013-11-20|19:00";
            byte[] value = FileUtils.readFileToByteArray(new File("/Users/gubatron/Desktop/update.xml"));
            PrivateKey privateKey = SecurityUtils.getPrivateKey(FileUtils.readFileToString(new File("/Users/gubatron/Desktop/private.key")));
            SignedMessage signedUpdateMessage = SecurityUtils.sign(value, privateKey);
            dhtPlugin.put(dhtKey.getBytes(), "frostwire-desktop update.xml file", signedUpdateMessage.toBytes(), DHTPlugin.FLAG_SINGLE_VALUE, new DomainAliasManagerTest.DHTUpdateMessagePublishListener());
        } else {
            System.out.println("Could not get DHTPlugin.");
            return;
        }
        
    }
    
    public static void testDomainAliasManager() throws InterruptedException {
        DomainAliasManagerBroker DOMAIN_ALIAS_MANAGER_BROKER = new DomainAliasManagerBroker();
        @SuppressWarnings("unused")
        DomainAliasManager domainAliasManager = DOMAIN_ALIAS_MANAGER_BROKER.getDomainAliasManager("kickass.to");//www.kat.ph");

        SearchEngine kat = SearchEngine.KAT;
        SearchManager manager = new SearchManagerImpl();
        manager.registerListener(new SearchManagerListener() {

            @Override
            public void onResults(SearchPerformer performer, List<? extends SearchResult> results) {
                System.out.println(performer.getToken() + " got results -> " + results.size() + " (from " + ((WebSearchPerformer) performer).getDomainName() + ")");
                for (SearchResult r : results) {
                    System.out.println(r.getDisplayName());
                }
            }

            @Override
            public void onFinished(long token) {
                System.out.println("search done - token:" + token);
            }
        });
        long tokenId = 2312389382l;
        manager.perform(kat.getPerformer(2312389382l, "frostwire"));

        while (true) {
            System.out.println("Waiting 10 secs for next search...");
            Thread.sleep(10000);
            tokenId += 2312389382l;
            manager.perform(kat.getPerformer(tokenId, "frostwire"));

        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        
        dhtInitializationTest();
        
        while (true) {
            System.out.println("...");
            Thread.sleep(1000);
        }
    }
}