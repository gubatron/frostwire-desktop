package com.limegroup.gnutella.connection;

import java.net.Socket;

import org.limewire.io.NetworkInstanceUtils;
import org.limewire.security.SecureMessageVerifier;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.limegroup.gnutella.ApplicationServices;
import com.limegroup.gnutella.GuidMapManager;
import com.limegroup.gnutella.MessageDispatcher;
import com.limegroup.gnutella.NetworkManager;
import com.limegroup.gnutella.NetworkUpdateSanityChecker;
import com.limegroup.gnutella.filters.SpamFilterFactory;
import com.limegroup.gnutella.messages.MessageFactory;
import com.limegroup.gnutella.messages.QueryReplyFactory;
import com.limegroup.gnutella.messages.QueryRequestFactory;
import com.limegroup.gnutella.messages.vendor.CapabilitiesVMFactory;
import com.limegroup.gnutella.messages.vendor.MessagesSupportedVendorMessage;
import com.limegroup.gnutella.search.SearchResultHandler;
import com.limegroup.gnutella.statistics.OutOfBandStatistics;
import com.sun.corba.se.pept.transport.Acceptor;

/**
 * An implementation of {@link RoutedConnectionFactory} that constructs {@link GnutellaConnection GnutellaConnections}.
 */
@Singleton
public class RoutedConnectionFactoryImpl implements RoutedConnectionFactory {

    private final NetworkManager networkManager;

    private final QueryRequestFactory queryRequestFactory;

    
    private final QueryReplyFactory queryReplyFactory;

    private final Provider<MessageDispatcher> messageDispatcher;

    private final Provider<NetworkUpdateSanityChecker> networkUpdateSanityChecker;

    private final Provider<SearchResultHandler> searchResultHandler;

    private final CapabilitiesVMFactory capabilitiesVMFactory;

    private final MessagesSupportedVendorMessage supportedVendorMessage;

    private final GuidMapManager guidMapManager;

    private final SpamFilterFactory spamFilterFactory;

    private final MessageFactory messageFactory;

    private final MessageReaderFactory messageReaderFactory;

    private final ApplicationServices applicationServices;
    
    private final Provider<SecureMessageVerifier> secureMessageVerifier;
    
    private final OutOfBandStatistics outOfBandStatistics;
    
    private final NetworkInstanceUtils networkInstanceUtils;

    @Inject
    public RoutedConnectionFactoryImpl(
            NetworkManager networkManager, QueryRequestFactory queryRequestFactory,
           QueryReplyFactory queryReplyFactory, Provider<MessageDispatcher> messageDispatcher,
            Provider<NetworkUpdateSanityChecker> networkUpdateSanityChecker,
            Provider<SearchResultHandler> searchResultHandler,
            CapabilitiesVMFactory capabilitiesVMFactory, 
            MessagesSupportedVendorMessage supportedVendorMessage, 
            GuidMapManager guidMapManager,
            SpamFilterFactory spamFilterFactory, MessageFactory messageFactory,
            MessageReaderFactory messageReaderFactory, ApplicationServices applicationServices,
            Provider<SecureMessageVerifier> secureMessageVerifier, OutOfBandStatistics outOfBandStatistics,
            NetworkInstanceUtils networkInstanceUtils) {
        this.networkManager = networkManager;
        this.queryRequestFactory = queryRequestFactory;
        this.queryReplyFactory = queryReplyFactory;
        this.messageDispatcher = messageDispatcher;
        this.networkUpdateSanityChecker = networkUpdateSanityChecker;
        this.applicationServices = applicationServices;
        this.searchResultHandler = searchResultHandler;
        this.capabilitiesVMFactory = capabilitiesVMFactory;
        this.supportedVendorMessage = supportedVendorMessage;
        this.guidMapManager = guidMapManager;
        this.spamFilterFactory = spamFilterFactory;
        this.messageFactory = messageFactory;
        this.messageReaderFactory = messageReaderFactory;
        this.secureMessageVerifier = secureMessageVerifier;
        this.outOfBandStatistics = outOfBandStatistics;
        this.networkInstanceUtils = networkInstanceUtils;
    }

    public RoutedConnection createRoutedConnection(String host, int port) {
        return null;
    }

    public RoutedConnection createRoutedConnection(Socket socket) {
        return null;
    }

}
