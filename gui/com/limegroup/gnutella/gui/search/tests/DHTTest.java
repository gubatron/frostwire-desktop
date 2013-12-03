package com.limegroup.gnutella.gui.search.tests;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
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

public class DHTTest {
    private static final String TEST_KEY = "http://update.frostwire.com/|2013-11-21|21:55";
    private static final int MAX_VALUE_SIZE = 512;
    public static int seconds = 1;
    
    private static class DHTTestOperationListener implements DHTOperationListener {

        private final DHT dht;
        private boolean writeComplete = false;
        private ByteBuffer readBuffer;
        
        public DHTTestOperationListener(DHT dht) {
            this.dht = dht;
            readBuffer = ByteBuffer.allocate(1024*256);
        }

        @Override
        public void searching(DHTTransportContact contact, int level, int active_searches) {
        }

        @Override
        public void diversified(String desc) {
            System.out.println("diversified desc: " + desc);
        }

        @Override
        public void found(DHTTransportContact contact, boolean is_closest) {
        }

        @Override
        public void read(DHTTransportContact contact, DHTTransportValue value) {
            try {
                onValueRead(contact, value, this);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        
        public ByteBuffer getReadingByteBuffer() {
            return readBuffer;
        }

        @Override
        public void wrote(DHTTransportContact contact, DHTTransportValue value) {
            System.out.println("Update Message Written at " + contact.getName() + " " + contact.getAddress().getHostString());
            
            if (!writeComplete && value.getFlags() == 0) {
                writeComplete=true;
                try {
                    System.out.println("Looks like last piece has been written, read test here we go.");
                    readTest(dht,this);
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

        public DHT getDHT() {
            return dht;
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
            if (ev.getType() == DHTPlugin.EVENT_DHT_AVAILABLE && ev.getValue() instanceof DHT) {
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
        
        return pel.getDHT();//dht.getDHT(DHT.NW_MAIN);
    }

    public static void writeTest(final DHT dht) throws InterruptedException, IOException {
        if (dht == null) {
            System.out.println("No dht!");
            return;
        }
        
        String dhtKey = TEST_KEY;
        byte[] value = new String("00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001").getBytes();//FileUtils.readFileToByteArray(new File("/Users/gubatron/Desktop/update.xml"));
        PrivateKey privateKey = SecurityUtils.getPrivateKey(FileUtils.readFileToString(new File("/Users/gubatron/Desktop/private.key")).trim());
        SignedMessage signedUpdateMessage = SecurityUtils.sign(value, privateKey);
        byte[] signedMessageData = signedUpdateMessage.toBytes();
        System.out.println("putting "+ signedMessageData.length +" bytes...");
        System.out.println(new String(signedMessageData));
        System.out.println("...");
        
        
        DHTTestOperationListener dhtUpdateMessagePublishListener = new DHTTestOperationListener(dht);
        if (signedMessageData.length > MAX_VALUE_SIZE) {
            int offset = 0;
            boolean lastChunk = false;
            while (!lastChunk) {
                byte[] tempData = new byte[Math.min(MAX_VALUE_SIZE, signedMessageData.length - offset)];
                System.arraycopy(signedMessageData, offset, tempData, 0, tempData.length);
                offset += tempData.length;

                if ((signedMessageData.length - offset) > MAX_VALUE_SIZE) {
                    dht.put(dhtKey.getBytes(), "", tempData, (byte) DHT.FLAG_MULTI_VALUE ,dhtUpdateMessagePublishListener);                   
                    System.out.println("offset="+offset+" dht.put part of " + tempData.length);
                } else {
                    dht.put(dhtKey.getBytes(), "", tempData, (byte) 0 , dhtUpdateMessagePublishListener); 
                    System.out.println("offset="+offset+" dht.put LAST part of " + tempData.length);
                    lastChunk = true;
                }
            }
        } else {
            dht.put(dhtKey.getBytes(), "", signedMessageData, (byte) 0 ,new DHTTestOperationListener(dht));
        }
        System.out.println("invoked put...");
    }
    
    public static void readTest(final DHT dht, DHTTestOperationListener dumpl) throws InterruptedException, IOException {
        String dhtKey = TEST_KEY;
        System.out.println("readTest: about to send get");
        dht.get(dhtKey.getBytes(), "", (byte) DHT.FLAG_SINGLE_VALUE, 30, 60000*3, false, false, dumpl);
        System.out.println("readTest: get invocation finished.");
        DHTTest.seconds = 1;
    }
    
    private static void onValueRead(DHTTransportContact originator, DHTTransportValue value,final DHTTestOperationListener dumpl) throws InterruptedException, IOException {
        System.out.println("Read value from " + originator.getAddress().getHostString());
        System.out.println("[" + getString(value) + "]");
        ByteBuffer readBuffer = dumpl.getReadingByteBuffer();
        byte[] data = value.getValue();
        readBuffer.put(data);
        
        if (value.getFlags() == 0) {
            data = new byte[readBuffer.position()];
            readBuffer.get(data);
            
            SignedMessage signedMessage = SignedMessage.fromBytes(data);
            boolean verify = SecurityUtils.verify(signedMessage, SecurityUtils.getPublicKey(SecurityUtils.DHT_PUBLIC_KEY));
            System.out.println("Is it our signed message? " + verify);
    
            if (signedMessage.base32DataString != null) {
                String updateMessageXML = new String(Base32.decode(signedMessage.base32DataString));
                System.out.println("-----");
                System.out.println(getString(value));
                System.out.println(updateMessageXML);
                System.out.println("-----");
            }
        } else {
            readTest(dumpl.getDHT(), dumpl);
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
        DHTTestOperationListener dhtUpdateMessagePublishListener = new DHTTestOperationListener(dht);
        //writeTest(dht);
        readTest(dht,dhtUpdateMessagePublishListener);
        //removeTest(dht);
        
        while (true) {
            System.out.println("... " + seconds);
            seconds++;
            Thread.sleep(1000);
            //readTest(dht);
        }
    }
}