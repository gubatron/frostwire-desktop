package com.limegroup.gnutella.gui.search.tests;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;

import org.apache.commons.io.FileUtils;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.PluginManager;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.plugins.dht.DHTPlugin;
import com.aelitis.azureus.plugins.dht.DHTPluginContact;
import com.aelitis.azureus.plugins.dht.DHTPluginOperationListener;
import com.aelitis.azureus.plugins.dht.DHTPluginValue;
import com.frostwire.AzureusStarter;
import com.frostwire.util.Base32;
import com.frostwire.util.SecurityUtils;
import com.frostwire.util.SignedMessage;

public class DHTTest {
    private static class DHTUpdateMessagePublishListener implements DHTPluginOperationListener {

        private final DHTPlugin dhtPlugin;
        private boolean writeComplete = false;
        
        public DHTUpdateMessagePublishListener(DHTPlugin dhtPlugin) {
            this.dhtPlugin = dhtPlugin;
        }

        @Override
        public void starts(byte[] key) {
            System.out.println("Started DHT action with key: ["+ new String(key)+ "]!");
        }

        @Override
        public void diversified() {
            // TODO Auto-generated method stub
        }

        @Override
        public void valueRead(DHTPluginContact originator, DHTPluginValue value) {
            System.out.println("Read value from " + originator.getAddress().getHostString());
            byte[] data = value.getValue();
            SignedMessage signedMessage = SignedMessage.fromBytes(data);
            boolean verify = SecurityUtils.verify(signedMessage, SecurityUtils.getPublicKey(SecurityUtils.DHT_PUBLIC_KEY));
            System.out.println("Is it our signed message? " + verify);

            if (signedMessage.base32DataString != null) {
                String updateMessageXML = Base32.decode(signedMessage.base32DataString).toString();
                System.out.println("-----");
                System.out.println(updateMessageXML);
                System.out.println("-----");
            }

        }

        @Override
        public void valueWritten(DHTPluginContact target, DHTPluginValue value) {
            System.out.println("Update Message Written at " + target.getName() + " " + target.getAddress().getHostString());
            
            if (!writeComplete) {
                writeComplete=true;
                try {
                    readTest(dhtPlugin, this);
                } catch (Throwable e) {

                    e.printStackTrace();
                }
            }
        }

        @Override
        public void complete(byte[] key, boolean timeout_occurred) {
            System.out.println(new String(key) + " key complete!");

            if (timeout_occurred) {
                System.out.println("DHT publish completed due to time out.");
            }
        }
    }
    
    public static AzureusCore initAzureusCore() {
        System.out.println("Starting Azureus core...");
        AzureusStarter.start();
        AzureusCore azureusCore = AzureusStarter.getAzureusCore();
        return azureusCore;
    }
    
    public static DHTPlugin getDHTPlugin(AzureusCore azureusCore) {
        PluginManager pluginManager = azureusCore.getPluginManager();

        if (pluginManager == null) {
            System.out.println("Could not get plugin manager.");
            return null;
        }
        
        PluginInterface pi = pluginManager.getPluginInterfaceByClass(DHTPlugin.class);
        DHTPlugin dhtPlugin = (DHTPlugin) pi.getPlugin();
        return dhtPlugin;
    }

    public static void writeTest(final DHTPlugin dhtPlugin) throws InterruptedException, IOException {

        if (dhtPlugin != null) {
            String dhtKey = "http://update.frostwire.com/|2013-11-20|19:00";
            byte[] value = FileUtils.readFileToByteArray(new File("/Users/gubatron/Desktop/update.xml"));
            PrivateKey privateKey = SecurityUtils.getPrivateKey(FileUtils.readFileToString(new File("/Users/gubatron/Desktop/private.key")).trim());
            SignedMessage signedUpdateMessage = SecurityUtils.sign(value, privateKey);
            byte[] signedMessageData = signedUpdateMessage.toBytes();
            System.out.println("putting "+ signedMessageData.length +" bytes...");
            System.out.println(new String(signedMessageData));
            System.out.println("...");
            dhtPlugin.put(dhtKey.getBytes(), "frostwire-desktop update.xml file", signedMessageData, DHTPlugin.FLAG_SINGLE_VALUE, new DHTTest.DHTUpdateMessagePublishListener(dhtPlugin));
            System.out.println("invoked put...");
        } else {
            System.out.println("Could not get DHTPlugin.");
            return;
        }
    }
    
    public static void readTest(final DHTPlugin dhtPlugin, final DHTTest.DHTUpdateMessagePublishListener dhtOplistener) throws InterruptedException, IOException {
        String dhtKey = "http://update.frostwire.com/|2013-11-20|19:00";
        System.out.println("readTest: about to send get");
        dhtPlugin.get(dhtKey.getBytes(), "frostwire-desktop update.xml file", DHTPlugin.FLAG_SINGLE_VALUE, 20, 180000, true, true, dhtOplistener);
    }
    
    
    public static void main(String[] args ) throws InterruptedException, IOException {
        AzureusCore azureusCore = initAzureusCore();
        DHTPlugin dhtPlugin = getDHTPlugin(azureusCore);
        writeTest(dhtPlugin);
        int seconds = 1;
        while (true) {
            System.out.println("... " + seconds);
            seconds++;
            Thread.sleep(1000);
        }
    }
}
