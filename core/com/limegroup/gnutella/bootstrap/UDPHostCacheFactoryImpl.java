package com.limegroup.gnutella.bootstrap;

import org.limewire.io.NetworkInstanceUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.limegroup.gnutella.MessageRouter;
import com.limegroup.gnutella.UDPPinger;
import com.limegroup.gnutella.messages.PingRequestFactory;

@Singleton
public class UDPHostCacheFactoryImpl implements UDPHostCacheFactory {

    private final Provider<MessageRouter> messageRouter;
    private final PingRequestFactory pingRequestFactory;
    private final NetworkInstanceUtils networkInstanceUtils;

    @Inject
    public UDPHostCacheFactoryImpl(Provider<MessageRouter> messageRouter,
            PingRequestFactory pingRequestFactory,
            NetworkInstanceUtils networkInstanceUtils) {
        this.messageRouter = messageRouter;
        this.pingRequestFactory = pingRequestFactory;
        this.networkInstanceUtils = networkInstanceUtils;
    }
    
    public UDPHostCache createUDPHostCache(UDPPinger pinger) {
        return null;//new UDPHostCache(pinger, messageRouter, pingRequestFactory, connectionServices,
                //networkInstanceUtils);
    }

    public UDPHostCache createUDPHostCache(long expiryTime, UDPPinger pinger) {
        return null;
    }

}
