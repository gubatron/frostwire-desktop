package com.limegroup.gnutella.messages.vendor;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.limewire.io.IPPortCombo;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class UDPCrawlerPongFactoryImpl implements UDPCrawlerPongFactory {

    @Inject
    public UDPCrawlerPongFactoryImpl() {
    }
    
    public UDPCrawlerPong createUDPCrawlerPong(UDPCrawlerPing request) {
        return new UDPCrawlerPong(request, derivePayload(request));
    }

    private byte [] derivePayload(UDPCrawlerPing request) {
        
        return null;
    }
    
    
    /**
     * copy/pasted from PushProxyRequest.  This should go to NetworkUtils imho
     * @param addr address of the other person
     * @param port the port
     * @return 6-byte value representing the address and port.
     */
    private static byte[] packIPAddress(InetAddress addr, int port) {
        try {
            // i do it during construction....
            IPPortCombo combo = 
                new IPPortCombo(addr.getHostAddress(), port);
            return combo.toBytes();
        } catch (UnknownHostException uhe) {
            throw new IllegalArgumentException(uhe.getMessage());
        }
    }

}
