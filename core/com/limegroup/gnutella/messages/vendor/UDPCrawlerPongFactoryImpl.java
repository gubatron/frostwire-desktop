package com.limegroup.gnutella.messages.vendor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.gudy.azureus2.plugins.network.ConnectionManager;
import org.limewire.io.IPPortCombo;
import org.limewire.service.ErrorService;
import org.limewire.util.ByteOrder;
import org.limewire.util.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.limegroup.gnutella.Constants;
import com.limegroup.gnutella.connection.Connection;
import com.limegroup.gnutella.connection.RoutedConnection;
import com.limegroup.gnutella.settings.ApplicationSettings;
import com.limegroup.gnutella.util.FrostWireUtils;

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
