package com.limegroup.gnutella.gui.search.tests;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.FileUtils;
import org.gudy.azureus2.plugins.PluginEvent;
import org.gudy.azureus2.plugins.PluginEventListener;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.PluginManager;

import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.dht.DHT;
import com.aelitis.azureus.core.dht.DHTOperationListener;
import com.aelitis.azureus.core.dht.transport.DHTTransportContact;
import com.aelitis.azureus.core.dht.transport.DHTTransportValue;
import com.aelitis.azureus.plugins.dht.DHTPlugin;
import com.frostwire.AzureusStarter;
import com.frostwire.util.Base32;
import com.frostwire.util.SecurityUtils;
import com.frostwire.util.SignedMessage;

public class DHTTest {
    private static final String TEST_KEY = "http://update.frostwire.com/|2013-11-21|19:01";
    public static int seconds = 1;
    
    private static class DHTUpdateMessagePublishListener implements DHTOperationListener {

        private final DHT dhtPlugin;
        private boolean writeComplete = false;
        
        public DHTUpdateMessagePublishListener(DHT dht) {
            this.dhtPlugin = dht;
        }

        @Override
        public void searching(DHTTransportContact contact, int level, int active_searches) {
        }

        @Override
        public void diversified(String desc) {
        }

        @Override
        public void found(DHTTransportContact contact, boolean is_closest) {
        }

        @Override
        public void read(DHTTransportContact contact, DHTTransportValue value) {
            onValueRead(contact, value);
        }

        @Override
        public void wrote(DHTTransportContact contact, DHTTransportValue value) {
            System.out.println("Update Message Written at " + contact.getName() + " " + contact.getAddress().getHostString());
            
            if (!writeComplete) {
                writeComplete=true;
                try {
                    readTest(dhtPlugin);
                } catch (Throwable e) {

                    e.printStackTrace();
                }
            }
        }

        @Override
        public void complete(boolean timeout) {
            System.out.println("Key complete!");

            if (timeout) {
                System.out.println("DHT publish completed due to time out.");
            }
        }
    }
    
    public static class DHTPluginLoadingListener implements PluginEventListener {
        private DHT dht = null;
        private final CountDownLatch latch;
        
        public DHTPluginLoadingListener(CountDownLatch latch) {
            this.latch = latch;
        }
        
        @Override
        public void handleEvent(PluginEvent ev) {
            if (ev.getType() == DHTPlugin.EVENT_DHT_AVAILABLE) {
                System.out.println(ev.getValue());
                dht = (DHT) ev.getValue();
                latch.countDown();
            }
        }
        
        public DHT getDHT() {
            return dht;
        }
    };

    
    public static AzureusCore initAzureusCore() {
        System.out.println("Starting Azureus core...");
        AzureusStarter.start();
        AzureusCore azureusCore = AzureusStarter.getAzureusCore();
        return azureusCore;
    }
    
    public static DHT getDHT(AzureusCore azureusCore) throws InterruptedException {
        PluginManager pluginManager = azureusCore.getPluginManager();

        if (pluginManager == null) {
            System.out.println("Could not get plugin manager.");
            return null;
        }
        
        PluginInterface pi = pluginManager.getPluginInterfaceByClass(DHTPlugin.class);
        final CountDownLatch latch = new CountDownLatch(1);
        final DHTPluginLoadingListener pel = new DHTPluginLoadingListener(latch);
        pi.addEventListener(pel);
        System.out.println("Wait for DHT...");
        latch.await();
        
        return pel.getDHT();//dhtPlugin.getDHT(DHT.NW_MAIN);
    }

    public static void writeTest(final DHT dht) throws InterruptedException, IOException {
        if (dht == null) {
            System.out.println("No dht!");
            return;
        }
        
        String dhtKey = TEST_KEY;
        byte[] value = new String("Hello World, this should be easy to read and write.").getBytes();//FileUtils.readFileToByteArray(new File("/Users/gubatron/Desktop/update.xml"));
        PrivateKey privateKey = SecurityUtils.getPrivateKey(FileUtils.readFileToString(new File("/Users/gubatron/Desktop/private.key")).trim());
        SignedMessage signedUpdateMessage = SecurityUtils.sign(value, privateKey);
        byte[] signedMessageData = signedUpdateMessage.toBytes();
        System.out.println("putting "+ signedMessageData.length +" bytes...");
        System.out.println(new String(signedMessageData));
        System.out.println("...");
        
        dht.put(dhtKey.getBytes(), "", signedMessageData, (byte) (DHT.FLAG_MULTI_VALUE | DHT.FLAG_PRECIOUS),new DHTUpdateMessagePublishListener(dht));
        System.out.println("invoked put...");
    }
    
    public static void readTest(final DHT dht) throws InterruptedException, IOException {
        String dhtKey = TEST_KEY;
        System.out.println("readTest: about to send get");
        dht.get(dhtKey.getBytes(), "", DHTPlugin.FLAG_MULTI_VALUE, 30, 60000*3, true, true, new DHTUpdateMessagePublishListener(dht));
        System.out.println("readTest: get invocation finished.");
        DHTTest.seconds = 1;
    }
    
    private static void onValueRead(DHTTransportContact originator, DHTTransportValue value) {
        System.out.println("Read value from " + originator.getAddress().getHostString());
        byte[] data = value.getValue();
        SignedMessage signedMessage = SignedMessage.fromBytes(data);
        boolean verify = SecurityUtils.verify(signedMessage, SecurityUtils.getPublicKey(SecurityUtils.DHT_PUBLIC_KEY));
        System.out.println("Is it our signed message? " + verify);

        if (signedMessage.base32DataString != null) {
            String updateMessageXML = new String(Base32.decode(signedMessage.base32DataString));
            System.out.println("-----");
            System.out.println(updateMessageXML);
            System.out.println("-----");
        }
    }
    
    public static String getString(DHTTransportValue value) {
        return( new String( value.getValue()) + 
            "; flags=" + Integer.toHexString( value.getFlags()) + 
            "; lifetime_hours=" + value.getLifeTimeHours() + 
            "; replication_factor=" + value.getReplicationFactor() + 
            "; originator=" + value.getOriginator().getAddress().getHostString());
    }
    
    public static void main(String[] args ) throws InterruptedException, IOException {
        final AzureusCore azureusCore = initAzureusCore();
        final DHT dht = getDHT(azureusCore);
        writeTest(dht);
        readTest(dht);
        
        while (true) {
            System.out.println("... " + seconds);
            seconds++;
            Thread.sleep(1000);
        }
    }
}