package com.limegroup.gnutella;


import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledExecutorService;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.limegroup.gnutella.auth.ContentManager;
import com.limegroup.gnutella.messagehandlers.InspectionRequestHandler;
import com.limegroup.gnutella.messagehandlers.UDPCrawlerPingHandler;
import com.limegroup.gnutella.messages.PingReplyFactory;
import com.limegroup.gnutella.messages.PingRequest;
import com.limegroup.gnutella.messages.PingRequestFactory;
import com.limegroup.gnutella.messages.QueryReplyFactory;
import com.limegroup.gnutella.messages.QueryRequest;
import com.limegroup.gnutella.messages.QueryRequestFactory;
import com.limegroup.gnutella.messages.StaticMessages;
import com.limegroup.gnutella.messages.vendor.HeadPongFactory;
import com.limegroup.gnutella.search.QueryDispatcher;
import com.limegroup.gnutella.search.QueryHandlerFactory;

/**
 * This class is the message routing implementation for TCP messages.
 */
@Singleton
public class StandardMessageRouter extends MessageRouterImpl {
    
    @Inject
    public StandardMessageRouter(NetworkManager networkManager,
            QueryRequestFactory queryRequestFactory,
            QueryHandlerFactory queryHandlerFactory,
            HeadPongFactory headPongFactory, PingReplyFactory pingReplyFactory,
            QueryUnicaster queryUnicaster, ContentManager contentManager,
            DownloadManager downloadManager, UDPService udpService,
            QueryReplyFactory queryReplyFactory, StaticMessages staticMessages,
            Provider<MessageDispatcher> messageDispatcher,
            MulticastService multicastService, QueryDispatcher queryDispatcher,
            Provider<ActivityCallback> activityCallback,
            ApplicationServices applicationServices,
            @Named("backgroundExecutor")
            ScheduledExecutorService backgroundExecutor,
            Provider<PongCacher> pongCacher,
            GuidMapManager guidMapManager, 
            UDPReplyHandlerCache udpReplyHandlerCache,
            Provider<InspectionRequestHandler> inspectionRequestHandlerFactory,
            Provider<UDPCrawlerPingHandler> udpCrawlerPingHandlerFactory,
            PingRequestFactory pingRequestFactory, MessageHandlerBinder messageHandlerBinder) {
        super(networkManager, queryRequestFactory, queryHandlerFactory,
                headPongFactory, pingReplyFactory,
                queryUnicaster,
                contentManager,
                downloadManager, udpService,
                queryReplyFactory, staticMessages,
                messageDispatcher, multicastService, queryDispatcher,
                activityCallback, applicationServices,
                backgroundExecutor, pongCacher,
                guidMapManager, udpReplyHandlerCache, inspectionRequestHandlerFactory, 
                udpCrawlerPingHandlerFactory, 
                messageHandlerBinder);
    }
    
    /**
     * Responds to a Gnutella ping with cached pongs. This does special handling
     * for both "heartbeat" pings that were sent to ensure that the connection
     * is still live as well as for pings from a crawler.
     * 
     * @param ping the <tt>PingRequest</tt> to respond to
     * @param handler the <tt>ReplyHandler</tt> to send any pongs to
     */
    @Override
    protected void respondToPingRequest(PingRequest ping,
                                        ReplyHandler handler) {
        
    }

	/**
	 * Responds to a ping request received over a UDP port.  This is
	 * handled differently from all other ping requests.  Instead of
	 * responding with cached pongs, we respond with a pong from our node.
	 *
	 * @param request the <tt>PingRequest</tt> to service
     * @param addr the <tt>InetSocketAddress</tt> containing the IP
     *  and port of the client node
     * @param handler the <tt>ReplyHandler</tt> that should handle any
     *  replies
	 */
    @Override
	protected void respondToUDPPingRequest(PingRequest request, 
										   InetSocketAddress addr,
                                           ReplyHandler handler) {
        
        
	}
    
    @Override
    protected boolean respondToQueryRequest(QueryRequest queryRequest,
                                            byte[] clientGUID,
                                            ReplyHandler handler) {
//        //Only respond if we understand the actual feature, if it had a feature.
//        if(!FeatureSearchData.supportsFeature(queryRequest.getFeatureSelector()))
//            return false;
//                                                
//        // Only send results if we're not busy.  Note that this ignores
//        // queue slots -- we're considered busy if all of our "normal"
//        // slots are full.  This allows some spillover into our queue that
//        // is necessary because we're always returning more total hits than
//        // we have slots available.
//        if(!uploadManager.mayBeServiceable() )  {
//            return false;
//        }
//                                                
//                                                
//        // Ensure that we have a valid IP & Port before we send the response.
//        // Otherwise the QueryReply will fail on creation.
//        if( !NetworkUtils.isValidPort(networkManager.getPort()) ||
//            !NetworkUtils.isValidAddress(networkManager.getAddress()))
//            return false;
//                                                     
//        // Run the local query
//        Response[] responses = fileManager.query(queryRequest);
//        return sendResponses(responses, queryRequest, handler);
        return false;
        
    }
}
