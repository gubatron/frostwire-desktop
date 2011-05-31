package com.limegroup.gnutella;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.collection.Buffer;
import org.limewire.collection.FixedsizeHashMap;
import org.limewire.collection.NoMoreStorageException;
import org.limewire.concurrent.ExecutorsHelper;
import org.limewire.concurrent.ManagedThread;
import org.limewire.inspection.Inspectable;
import org.limewire.inspection.InspectionPoint;
import org.limewire.io.IpPort;
import org.limewire.io.NetworkUtils;
import org.limewire.service.ErrorService;
import org.limewire.setting.evt.SettingEvent;
import org.limewire.setting.evt.SettingListener;
import org.limewire.util.Base32;
import org.limewire.util.ByteOrder;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.limegroup.gnutella.auth.ContentManager;
import com.limegroup.gnutella.connection.Connection;
import com.limegroup.gnutella.connection.ConnectionLifecycleEvent;
import com.limegroup.gnutella.connection.ConnectionLifecycleListener;
import com.limegroup.gnutella.connection.RoutedConnection;
import com.limegroup.gnutella.guess.GUESSEndpoint;
import com.limegroup.gnutella.messagehandlers.DualMessageHandler;
import com.limegroup.gnutella.messagehandlers.InspectionRequestHandler;
import com.limegroup.gnutella.messagehandlers.MessageHandler;
import com.limegroup.gnutella.messagehandlers.UDPCrawlerPingHandler;
import com.limegroup.gnutella.messages.BadPacketException;
import com.limegroup.gnutella.messages.Message;
import com.limegroup.gnutella.messages.PingReply;
import com.limegroup.gnutella.messages.PingReplyFactory;
import com.limegroup.gnutella.messages.PingRequest;
import com.limegroup.gnutella.messages.PingRequestFactory;
import com.limegroup.gnutella.messages.PushRequest;
import com.limegroup.gnutella.messages.QueryReply;
import com.limegroup.gnutella.messages.QueryReplyFactory;
import com.limegroup.gnutella.messages.QueryRequest;
import com.limegroup.gnutella.messages.QueryRequestFactory;
import com.limegroup.gnutella.messages.StaticMessages;
import com.limegroup.gnutella.messages.vendor.ContentResponse;
import com.limegroup.gnutella.messages.vendor.HeadPing;
import com.limegroup.gnutella.messages.vendor.HeadPong;
import com.limegroup.gnutella.messages.vendor.HeadPongFactory;
import com.limegroup.gnutella.messages.vendor.InspectionRequest;
import com.limegroup.gnutella.messages.vendor.LimeACKVendorMessage;
import com.limegroup.gnutella.messages.vendor.PushProxyAcknowledgement;
import com.limegroup.gnutella.messages.vendor.PushProxyRequest;
import com.limegroup.gnutella.messages.vendor.QueryStatusResponse;
import com.limegroup.gnutella.messages.vendor.ReplyNumberVendorMessage;
import com.limegroup.gnutella.messages.vendor.TCPConnectBackRedirect;
import com.limegroup.gnutella.messages.vendor.TCPConnectBackVendorMessage;
import com.limegroup.gnutella.messages.vendor.UDPConnectBackRedirect;
import com.limegroup.gnutella.messages.vendor.UDPConnectBackVendorMessage;
import com.limegroup.gnutella.messages.vendor.UDPCrawlerPing;
import com.limegroup.gnutella.messages.vendor.UpdateRequest;
import com.limegroup.gnutella.messages.vendor.UpdateResponse;
import com.limegroup.gnutella.messages.vendor.VendorMessage;
import com.limegroup.gnutella.routing.PatchTableMessage;
import com.limegroup.gnutella.routing.QueryRouteTable;
import com.limegroup.gnutella.routing.ResetTableMessage;
import com.limegroup.gnutella.search.QueryDispatcher;
import com.limegroup.gnutella.search.QueryHandler;
import com.limegroup.gnutella.search.QueryHandlerFactory;
import com.limegroup.gnutella.search.ResultCounter;
import com.limegroup.gnutella.search.SearchResultHandler;
import com.limegroup.gnutella.settings.ConnectionSettings;
import com.limegroup.gnutella.settings.FilterSettings;
import com.limegroup.gnutella.settings.MessageSettings;
import com.limegroup.gnutella.settings.SearchSettings;
import com.limegroup.gnutella.util.FrostWireUtils;


/**
 * One of the three classes that make up the core of the backend.  This
 * class' job is to direct the routing of messages and to count those message
 * as they pass through.  To do so, it aggregates a ConnectionManager that
 * maintains a list of connections.
 */
//@Singleton // not here because it's not constructed, subclass is!
public abstract class MessageRouterImpl implements MessageRouter {
    
    private static final Log LOG = LogFactory.getLog(MessageRouterImpl.class);

    /**
     * Constant for the number of old connections to use when forwarding
     * traffic from old connections.
     */
    private static final int OLD_CONNECTIONS_TO_USE = 15;

    /**
     * The GUID we attach to QueryReplies to allow PushRequests in
     * responses.
     */
    protected byte[] _clientGUID;

    /**
     * The maximum size for <tt>RouteTable</tt>s.
     */
    private int MAX_ROUTE_TABLE_SIZE = 50000;  //actually 100,000 entries

    /**
     * Maps PingRequest GUIDs to PingReplyHandlers.  Stores 2-4 minutes,
     * typically around 2500 entries, but never more than 100,000 entries.
     */
    @InspectionPoint("ping routing table dump")
    private RouteTable _pingRouteTable = 
        new RouteTable(2*60, MAX_ROUTE_TABLE_SIZE);
    /**
     * Maps QueryRequest GUIDs to QueryReplyHandlers.  Stores 5-10 minutes,
     * typically around 13000 entries, but never more than 100,000 entries.
     */
    @InspectionPoint("query routing table dump")
    private RouteTable _queryRouteTable = 
        new RouteTable(5*60, MAX_ROUTE_TABLE_SIZE);
    /**
     * Maps QueryReply client GUIDs to PushRequestHandlers.  Stores 7-14
     * minutes, typically around 3500 entries, but never more than 100,000
     * entries.  
     */
    @InspectionPoint("push routing table dump")
    private RouteTable _pushRouteTable = 
        new RouteTable(7*60, MAX_ROUTE_TABLE_SIZE);
    
    /**
     * Maps HeadPong guids to the originating pingers.  Short-lived since
     * we expect replies from our leaves quickly.
     */
    @InspectionPoint("headpong routing table dump")
    private RouteTable _headPongRouteTable = 
    	new RouteTable(10, MAX_ROUTE_TABLE_SIZE);

    /** How long to buffer up out-of-band replies.
     */
    private static final long CLEAR_TIME = 30 * 1000; // 30 seconds
    
    /**
     * The amount of time after which to expire an OOBSession.
     */
    private static final long OOB_SESSION_EXPIRE_TIME = 2 * 60 * 1000;

    /** Time between sending HopsFlow messages.
     */
    private static final long HOPS_FLOW_INTERVAL = 15 * 1000; // 15 seconds

    /** The maximum number of UDP replies to buffer up.  Non-final for 
     *  testing.
     */
    public static final int MAX_BUFFERED_REPLIES = 250;
    
    private int maxBufferedReplies = MAX_BUFFERED_REPLIES;

    /**
     * Keeps track of QueryReplies to be sent after recieving LimeAcks (sent
     * if the sink wants them).  Cleared every CLEAR_TIME seconds.
     * TimedGUID->QueryResponseBundle.
     */
    private final Map<GUID.TimedGUID, QueryResponseBundle> _outOfBandReplies =
        new Hashtable<GUID.TimedGUID, QueryResponseBundle>();

    
    private final BypassedResultsCache _bypassedResultsCache;
    
    /**
     * Keeps track of what hosts we have recently tried to connect back to via
     * UDP.  The size is limited and once the size is reached, no more connect
     * back attempts will be honored.
     */
    private static final FixedsizeHashMap<String, String> _udpConnectBacks = 
        new FixedsizeHashMap<String, String>(200);
        
    /**
     * The maximum numbers of ultrapeers to forward a UDPConnectBackRedirect
     * message to, per forward.
     */
    private static final int MAX_UDP_CONNECTBACK_FORWARDS = 5;

    /**
     * Keeps track of what hosts we have recently tried to connect back to via
     * TCP.  The size is limited and once the size is reached, no more connect
     * back attempts will be honored.
     */
    private static final FixedsizeHashMap<String, String> _tcpConnectBacks = 
        new FixedsizeHashMap<String, String>(200);
        
    /**
     * The maximum numbers of ultrapeers to forward a TCPConnectBackRedirect
     * message to, per forward.
     */
    private static final int MAX_TCP_CONNECTBACK_FORWARDS = 5;        
    
    /**
     * The processingqueue to add tcpconnectback socket connections to.
     */
    private static final ExecutorService TCP_CONNECT_BACKER =
        ExecutorsHelper.newProcessingQueue("TCPConnectBack");
    
	/**
	 * A handle to the thread that deals with QRP Propagation
	 */
	private final QRPPropagator QRP_PROPAGATOR = new QRPPropagator();


    /**
     * Variable for the most recent <tt>QueryRouteTable</tt> created
     * for this node.  If this node is an Ultrapeer, the routing
     * table will include the tables from its leaves.
     */
    private QueryRouteTable _lastQueryRouteTable;

    /**
     * The maximum number of response to send to a query that has
     * a "high" number of hops.
     */
    private static final int HIGH_HOPS_RESPONSE_LIMIT = 10;

    /**
     * The lifetime of OOBs guids.
     */
    private static final long TIMED_GUID_LIFETIME = 25 * 1000; 

    /** Keeps track of Listeners of GUIDs. */
    private volatile Map<byte[], List<MessageListener>> _messageListeners =
        Collections.emptyMap();
    
    /**
     * Lock that registering & unregistering listeners can hold
     * while replacing the listeners map / lists.
     */
    private final Object MESSAGE_LISTENER_LOCK = new Object();

    /**
     * The time we last received a request for a query key.
     */
    private long _lastQueryKeyTime;
    
    /** Handlers for TCP messages. */
    private ConcurrentMap<Class<? extends Message>, MessageHandler> messageHandlers =
        new ConcurrentHashMap<Class<? extends Message>, MessageHandler>(30, 0.75f, 3);
    
    /** Handler for UDP messages. */
    private ConcurrentMap<Class<? extends Message>, MessageHandler> udpMessageHandlers =
        new ConcurrentHashMap<Class<? extends Message>, MessageHandler>(15, 0.75f, 3);
    
    /** Handler for TCP messages. */
    private ConcurrentMap<Class<? extends Message>, MessageHandler> multicastMessageHandlers =
        new ConcurrentHashMap<Class<? extends Message>, MessageHandler>(5, 0.75f, 3);
        
    /** The length of time a multicast guid should stay alive. */
    private static final long MULTICAST_GUID_EXPIRE_TIME = 60 * 1000;
    
    /** How long to remember cached udp reply handlers. */
    private static final int UDP_REPLY_CACHE_TIME = 60 * 1000;
    
    @InspectionPoint("guid tracker")
    @SuppressWarnings("unused")
    private final GUIDTracker guidTracker = new GUIDTracker();
    
    @InspectionPoint("dropped replies")
    private final DroppedReplyCounter droppedReplyCounter = new DroppedReplyCounter(); 
    
    @InspectionPoint("duplicate queries")
    private final DuplicateQueryCounter duplicateQueryCounter = new DuplicateQueryCounter();
    
    protected final NetworkManager networkManager;
    protected final QueryRequestFactory queryRequestFactory;
    protected final QueryHandlerFactory queryHandlerFactory;
    protected final HeadPongFactory headPongFactory;
    protected final PingReplyFactory pingReplyFactory;
    protected final QueryUnicaster queryUnicaster;
    protected final FileManager fileManager;
    protected final ContentManager contentManager;
    protected final DownloadManager downloadManager;
    protected final UDPService udpService;
    protected final SearchResultHandler searchResultHandler;
    protected final HostCatcher hostCatcher;
    protected final QueryReplyFactory queryReplyFactory;
    protected final StaticMessages staticMessages;
    protected final Provider<MessageDispatcher> messageDispatcher;
    protected final MulticastService multicastService;
    protected final QueryDispatcher queryDispatcher;
    protected final Provider<ActivityCallback> activityCallback;
    protected final ScheduledExecutorService backgroundExecutor;
    protected final Provider<PongCacher> pongCacher;
    protected final UDPReplyHandlerCache udpReplyHandlerCache;
    protected final GuidMap multicastGuidMap;
    private final Provider<InspectionRequestHandler> inspectionRequestHandlerFactory;
    private final Provider<UDPCrawlerPingHandler> udpCrawlerPingHandlerFactory;
    
    private final PingRequestFactory pingRequestFactory;

    private final MessageHandlerBinder messageHandlerBinder;

    private final ConnectionListener connectionListener = new ConnectionListener();
    
    /**
     * Creates a MessageRouter. Must call initialize before using.
     */
    @Inject
    // TODO: Create a MessageRouterController,
    //       or split MessageRouter into a number of different classes,
    //       instead of passing all of these...
    protected MessageRouterImpl(NetworkManager networkManager,
            QueryRequestFactory queryRequestFactory,
            QueryHandlerFactory queryHandlerFactory,
            HeadPongFactory headPongFactory,
            PingReplyFactory pingReplyFactory,
            QueryUnicaster queryUnicaster,
            FileManager fileManager,
            ContentManager contentManager,
            DownloadManager downloadManager,
            UDPService udpService,
            SearchResultHandler searchResultHandler,
            HostCatcher hostCatcher,
            QueryReplyFactory queryReplyFactory,
            StaticMessages staticMessages,
            Provider<MessageDispatcher> messageDispatcher,
            MulticastService multicastService,
            QueryDispatcher queryDispatcher,
            Provider<ActivityCallback> activityCallback,
            ApplicationServices applicationServices,
            @Named("backgroundExecutor") ScheduledExecutorService backgroundExecutor,
            Provider<PongCacher> pongCacher,
            GuidMapManager guidMapManager,	
            UDPReplyHandlerCache udpReplyHandlerCache,
            Provider<InspectionRequestHandler> inspectionRequestHandlerFactory,
            Provider<UDPCrawlerPingHandler> udpCrawlerPingHandlerFactory,
            PingRequestFactory pingRequestFactory, MessageHandlerBinder messageHandlerBinder) {
        this.networkManager = networkManager;
        this.queryRequestFactory = queryRequestFactory;
        this.queryHandlerFactory = queryHandlerFactory;
        this.headPongFactory = headPongFactory;
        this.pingReplyFactory = pingReplyFactory;
        this.queryUnicaster = queryUnicaster;
        this.fileManager = fileManager;
        this.contentManager = contentManager;
        this.downloadManager = downloadManager;
        this.udpService = udpService;
        this.searchResultHandler = searchResultHandler;
        this.hostCatcher = hostCatcher;
        this.queryReplyFactory = queryReplyFactory;
        this.staticMessages = staticMessages;
        this.messageDispatcher = messageDispatcher;
        this.multicastService = multicastService;
        this.queryDispatcher = queryDispatcher;
        this.activityCallback = activityCallback;
        this.backgroundExecutor = backgroundExecutor;
        this.pongCacher = pongCacher;
        this.udpCrawlerPingHandlerFactory = udpCrawlerPingHandlerFactory;
        this.pingRequestFactory = pingRequestFactory;
        this.messageHandlerBinder = messageHandlerBinder;
        this.multicastGuidMap = guidMapManager.getMap();
        this.udpReplyHandlerCache = udpReplyHandlerCache;
        this.inspectionRequestHandlerFactory = inspectionRequestHandlerFactory;

        _clientGUID = applicationServices.getMyGUID();
        _bypassedResultsCache = new BypassedResultsCache(activityCallback, downloadManager);
    }
    
    /** Sets a new handler to the given handlerMap, for the given class. */
    private boolean setHandler(ConcurrentMap<Class<? extends Message>, MessageHandler> handlerMap,
                            Class<? extends Message> clazz, MessageHandler handler) {
        
        if (handler != null) {
            MessageHandler old = handlerMap.put(clazz, handler);
            if(old != null) {
                LOG.warn("Ejecting old handler: " + old + " for clazz: " + clazz);
            }
            return true;
        } else {
            return handlerMap.remove(clazz) != null;
        }
    }
    
    /** 
     * Adds the given handler to the handlerMap for the given class.
     * If a handler already existed, this will construct a DualMessageHandler
     * so that both are handlers are notified.
     * 
     * @param handlerMap
     * @param clazz
     * @param handler
     */
    private void addHandler(ConcurrentMap<Class<? extends Message>, MessageHandler> handlerMap,
                            Class<? extends Message> clazz, MessageHandler handler) {
        MessageHandler existing = handlerMap.get(clazz);
        if(existing != null) {
            // non-blocking addition -- continue trying until we succesfully
            // replace the prior handler w/ a dual version of that handler
            while(true) {
                MessageHandler dual = new DualMessageHandler(handler, existing);
                if(handlerMap.replace(clazz, existing, dual))
                    break;
                existing = handlerMap.get(clazz);
                dual = new DualMessageHandler(handler, existing);
            }
        } else {
            setHandler(handlerMap, clazz, handler);
        }
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#setMessageHandler(java.lang.Class, com.limegroup.gnutella.messagehandlers.MessageHandler)
     */
    public void setMessageHandler(Class<? extends Message> clazz, MessageHandler handler) {
        setHandler(messageHandlers, clazz, handler);
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#addMessageHandler(java.lang.Class, com.limegroup.gnutella.messagehandlers.MessageHandler)
     */
    public void addMessageHandler(Class<? extends Message> clazz, MessageHandler handler) {
        addHandler(messageHandlers, clazz, handler);
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#getMessageHandler(java.lang.Class)
     */
    public MessageHandler getMessageHandler(Class<? extends Message> clazz) {
        return messageHandlers.get(clazz);
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#setUDPMessageHandler(java.lang.Class, com.limegroup.gnutella.messagehandlers.MessageHandler)
     */
    public void setUDPMessageHandler(Class<? extends Message> clazz, MessageHandler handler) {
        setHandler(udpMessageHandlers, clazz, handler);
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#addUDPMessageHandler(java.lang.Class, com.limegroup.gnutella.messagehandlers.MessageHandler)
     */
    public void addUDPMessageHandler(Class<? extends Message> clazz, MessageHandler handler) {
        addHandler(udpMessageHandlers, clazz, handler);
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#getUDPMessageHandler(java.lang.Class)
     */
    public MessageHandler getUDPMessageHandler(Class<? extends Message> clazz) {
        return udpMessageHandlers.get(clazz);
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#setMulticastMessageHandler(java.lang.Class, com.limegroup.gnutella.messagehandlers.MessageHandler)
     */
    public void setMulticastMessageHandler(Class<? extends Message> clazz, MessageHandler handler) {
        setHandler(multicastMessageHandlers, clazz, handler);
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#addMulticastMessageHandler(java.lang.Class, com.limegroup.gnutella.messagehandlers.MessageHandler)
     */
    public void addMulticastMessageHandler(Class<? extends Message> clazz, MessageHandler handler) {
        addHandler(multicastMessageHandlers, clazz, handler);
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#getMulticastMessageHandler(java.lang.Class)
     */
    public MessageHandler getMulticastMessageHandler(Class<? extends Message> clazz) {
        return multicastMessageHandlers.get(clazz);
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#initialize()
     */
    public void start() {
        
		// TODO listener leaking, we should have a shutdown event
		//connectionManager.addEventListener(connectionListener);
		
	    QRP_PROPAGATOR.start();

        // schedule a runner to clear unused out-of-band replies
        backgroundExecutor.scheduleWithFixedDelay(new Expirer(), CLEAR_TIME, CLEAR_TIME, TimeUnit.MILLISECONDS);
        // schedule a runner to clear guys we've connected back to
        backgroundExecutor.scheduleWithFixedDelay(new ConnectBackExpirer(), 10 * CLEAR_TIME, 
                               10 * CLEAR_TIME, TimeUnit.MILLISECONDS);
//        // schedule a runner to send hops-flow messages
//        backgroundExecutor.scheduleWithFixedDelay(new HopsFlowManager(uploadManager, connectionManager), HOPS_FLOW_INTERVAL*10, 
//                               HOPS_FLOW_INTERVAL, TimeUnit.MILLISECONDS);
        backgroundExecutor.scheduleWithFixedDelay(new UDPReplyCleaner(), UDP_REPLY_CACHE_TIME, UDP_REPLY_CACHE_TIME, TimeUnit.MILLISECONDS);
        
        // runner to clean up OOB sessions
//        OOBHandler oobHandler = oobHandlerFactory.get();
//        backgroundExecutor.scheduleWithFixedDelay(oobHandler, CLEAR_TIME, CLEAR_TIME, TimeUnit.MILLISECONDS);
//        
        // handler for inspection requests
        InspectionRequestHandler inspectionHandler = inspectionRequestHandlerFactory.get();
        
        messageHandlerBinder.bind(this);
        
        setMessageHandler(PingRequest.class, new PingRequestHandler());
        setMessageHandler(PingReply.class, new PingReplyHandler());
        setMessageHandler(QueryRequest.class, new QueryRequestHandler());
        setMessageHandler(QueryReply.class, new QueryReplyHandler());
        setMessageHandler(ResetTableMessage.class, new ResetTableHandler());
        setMessageHandler(PatchTableMessage.class, new PatchTableHandler());
        setMessageHandler(TCPConnectBackVendorMessage.class, new TCPConnectBackHandler());
        setMessageHandler(UDPConnectBackVendorMessage.class, new UDPConnectBackHandler());
        setMessageHandler(TCPConnectBackRedirect.class, new TCPConnectBackRedirectHandler());
        setMessageHandler(UDPConnectBackRedirect.class, new UDPConnectBackRedirectHandler());
        setMessageHandler(PushProxyRequest.class, new PushProxyRequestHandler());
        setMessageHandler(QueryStatusResponse.class, new QueryStatusResponseHandler());
        setMessageHandler(HeadPing.class, new HeadPingHandler());
        //setMessageHandler(SimppRequestVM.class, new SimppRequestVMHandler());
        //setMessageHandler(SimppVM.class, new SimppVMHandler());
        setMessageHandler(UpdateRequest.class, new UpdateRequestHandler());
        setMessageHandler(UpdateResponse.class, new UpdateResponseHandler());
        setMessageHandler(HeadPong.class, new HeadPongHandler());
        setMessageHandler(VendorMessage.class, new VendorMessageHandler());
        setMessageHandler(InspectionRequest.class, inspectionHandler);
        
        setUDPMessageHandler(QueryRequest.class, new UDPQueryRequestHandler());
        //setUDPMessageHandler(QueryReply.class, new UDPQueryReplyHandler(oobHandler));
        setUDPMessageHandler(PingRequest.class, new UDPPingRequestHandler());
        setUDPMessageHandler(PingReply.class, new UDPPingReplyHandler());
        setUDPMessageHandler(LimeACKVendorMessage.class, new UDPLimeACKVendorMessageHandler());
        //setUDPMessageHandler(ReplyNumberVendorMessage.class, oobHandler);
        setUDPMessageHandler(UDPCrawlerPing.class, udpCrawlerPingHandlerFactory.get());
        setUDPMessageHandler(HeadPing.class, new UDPHeadPingHandler());
        setUDPMessageHandler(UpdateRequest.class, new UDPUpdateRequestHandler());
        setUDPMessageHandler(ContentResponse.class, new UDPContentResponseHandler());
        setUDPMessageHandler(InspectionRequest.class, inspectionHandler);
        
        setMulticastMessageHandler(QueryRequest.class, new MulticastQueryRequestHandler());
        //setMulticastMessageHandler(QueryReply.class, new MulticastQueryReplyHandler());
        setMulticastMessageHandler(PingRequest.class, new MulticastPingRequestHandler());
        //setMulticastMessageHandler(PingReply.class, new MulticastPingReplyHandler());
    }
    
    public void stop() {
        //connectionManager.removeEventListener(connectionListener);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#originateQueryGUID(byte[])
     */
    public void originateQueryGUID(byte[] guid) {
        //_queryRouteTable.routeReply(guid, forMeReplyHandler);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#queryKilled(com.limegroup.gnutella.GUID)
     */
    public void queryKilled(GUID guid) throws IllegalArgumentException {
        if (guid == null)
            throw new IllegalArgumentException("Input GUID is null!");
        _bypassedResultsCache.queryKilled(guid);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#downloadFinished(com.limegroup.gnutella.GUID)
     */
    public void downloadFinished(GUID guid) throws IllegalArgumentException {
        if (guid == null)
            throw new IllegalArgumentException("Input GUID is null!");
        _bypassedResultsCache.downloadFinished(guid);
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#getQueryLocs(com.limegroup.gnutella.GUID)
     */
    public Set<GUESSEndpoint> getQueryLocs(GUID guid) {
        return _bypassedResultsCache.getQueryLocs(guid);
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#getPingRouteTableDump()
     */
    public String getPingRouteTableDump() {
        return _pingRouteTable.toString();
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#getQueryRouteTableDump()
     */
    public String getQueryRouteTableDump() {
        return _queryRouteTable.toString();
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#getPushRouteTableDump()
     */
    public String getPushRouteTableDump() {
        return _pushRouteTable.toString();
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#removeConnection(com.limegroup.gnutella.ReplyHandler)
     */
    private void removeConnection(ReplyHandler rh) {
        queryDispatcher.removeReplyHandler(rh);
        _pingRouteTable.removeReplyHandler(rh);
        _queryRouteTable.removeReplyHandler(rh);
        _pushRouteTable.removeReplyHandler(rh);
        _headPongRouteTable.removeReplyHandler(rh);
    }

	/* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#handleMessage(com.limegroup.gnutella.messages.Message, com.limegroup.gnutella.RoutedConnection)
     */
    public void handleMessage(Message msg, 
                              ReplyHandler receivingConnection) {
        // Increment hops and decrease TTL.
        msg.hop();
        MessageHandler msgHandler = getMessageHandler(msg.getHandlerClass());
        if (msgHandler != null) {
            msgHandler.handleMessage(msg, null, receivingConnection);
        } else if (msg instanceof VendorMessage) {
            msgHandler = getMessageHandler(VendorMessage.class);
            if (msgHandler != null) {
                msgHandler.handleMessage(msg, null, receivingConnection);
            }
        }
        
        //This may trigger propogation of query route tables.  We do this AFTER
        //any handshake pings.  Otherwise we'll think all clients are old
        //clients.
		//forwardQueryRouteTables();
        notifyMessageListener(msg, receivingConnection);
    }

    /**
     * Notifies any message listeners of this message's guid about the message.
     * This holds no locks.
     */
    private final void notifyMessageListener(Message msg, ReplyHandler handler) {
        List<MessageListener> all = _messageListeners.get(msg.getGUID());
        if(all != null) {
            for(MessageListener next : all) {
                next.processMessage(msg, handler);
            }
        }
    }

	/* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#handleUDPMessage(com.limegroup.gnutella.messages.Message, java.net.InetSocketAddress)
     */	
	public void handleUDPMessage(Message msg, InetSocketAddress addr) {
	    // Increment hops and decrement TTL.
	    msg.hop();

        if(msg instanceof QueryReply) {
            // check to see if it was from the multicast map.
            byte[] origGUID = multicastGuidMap.getOriginalGUID(msg.getGUID());
            if(origGUID != null) {
                msg = queryReplyFactory.createQueryReply(origGUID, (QueryReply)msg);
                ((QueryReply)msg).setMulticastAllowed(true);
            }
        }
        
        ReplyHandler replyHandler = udpReplyHandlerCache.getUDPReplyHandler(addr);
        MessageHandler msgHandler = getUDPMessageHandler(msg.getHandlerClass());
        if (msgHandler != null) {
            msgHandler.handleMessage(msg, addr, replyHandler);
        }  else if (msg instanceof VendorMessage) {
            msgHandler = getUDPMessageHandler(VendorMessage.class);
            if (msgHandler != null) {
                msgHandler.handleMessage(msg, addr, replyHandler);
            }
        }
        
        notifyMessageListener(msg, replyHandler);
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#handleMulticastMessage(com.limegroup.gnutella.messages.Message, java.net.InetSocketAddress)
     */
	public void handleMulticastMessage(Message msg, InetSocketAddress addr) {
    
        // Use this assert for testing only -- it is a dangerous assert
        // to have in the field, as not all messages currently set the
        // network int appropriately.
        // If someone sends us messages we're not prepared to handle,
        // this could cause widespreaad AssertFailures
        // Assert.that(msg.isMulticast(),
        // "non multicast message in handleMulticastMessage: " + msg);

        // no multicast messages should ever have been
        // set with a TTL greater than 1.
        if (msg.getTTL() > 1) {
            return;
        }

        // Increment hops and decrement TTL.
        msg.hop();
        
        if (NetworkUtils.isLocalAddress(addr.getAddress())
                && !ConnectionSettings.ALLOW_MULTICAST_LOOPBACK.getValue()) {
            return;
        }

        ReplyHandler replyHandler = udpReplyHandlerCache.getUDPReplyHandler(addr);
        
        MessageHandler msgHandler = getMulticastMessageHandler(msg.getHandlerClass());
        if (msgHandler != null) {
            msgHandler.handleMessage(msg, addr, replyHandler);
        } else if (msg instanceof VendorMessage) {
            msgHandler = getMulticastMessageHandler(VendorMessage.class);
            if (msgHandler != null) {
                msgHandler.handleMessage(msg, addr, replyHandler);
            }
        }

        notifyMessageListener(msg, replyHandler);
    }


    /**
     * Returns true if the Query has a valid AddressSecurityToken. false if it isn't present
     * or valid.
     */
    protected boolean hasValidQueryKey(InetAddress ip, int port, 
                                       QueryRequest qr) {
//        AddressSecurityToken qk = qr.getQueryKey();
//        if (qk == null)
//            return false;
//        
//        return qk.isFor(ip, port);
        return false;
    }

	/**
	 * Sends an ack back to the GUESS client node.  
	 */
	protected void sendAcknowledgement(InetSocketAddress addr, byte[] guid) {
		
	}

	/**
	 * Creates a new <tt>PingReply</tt> from the set of cached
	 * GUESS endpoints, or a <tt>PingReply</tt> for localhost
	 * if no GUESS endpoints are available.
	 */
	private PingReply createPingReply(byte[] guid) {
		GUESSEndpoint endpoint = queryUnicaster.getUnicastEndpoint();
		if(endpoint == null) {
		    if(networkManager.isIpPortValid())
                return pingReplyFactory.create(guid, (byte)1);
            else
                return null;
		} else {
            return pingReplyFactory.createGUESSReply(guid, (byte)1, 
                                              endpoint.getPort(),
                                              endpoint.getInetAddress().getAddress());
		}
	}



	
    /**
     * The handler for PingRequests received in
     * RoutedConnection.loopForMessages().  Checks the routing table to see
     * if the request has already been seen.  If not, calls handlePingRequest.
     */
    final void handlePingRequestPossibleDuplicate(
        PingRequest request, ReplyHandler handler) {
		if(_pingRouteTable.tryToRouteReply(request.getGUID(), handler) != null)
            handlePingRequest(request, handler);
    }

    /**
     * The handler for PingRequests received in
     * RoutedConnection.loopForMessages().  Checks the routing table to see
     * if the request has already been seen.  If not, calls handlePingRequest.
     */
    final void handleUDPPingRequestPossibleDuplicate(													 
        PingRequest request, ReplyHandler handler, InetSocketAddress  addr) {
		if(_pingRouteTable.tryToRouteReply(request.getGUID(), handler) != null)
            handleUDPPingRequest(request, handler, addr);
    }

    /**
     * The handler for QueryRequests received in
     * RoutedConnection.loopForMessages().  Checks the routing table to see
     * if the request has already been seen.  If not, calls handleQueryRequest.
     */
    final void handleQueryRequestPossibleDuplicate(
        QueryRequest request, ReplyHandler receivingConnection) {
        
        // With the new handling of probe queries (TTL 1, Hops 0), we have a few
        // new options:
        // 1) If we have a probe query....
        //  a) If you have never seen it before, put it in the route table and
        //  set the ttl appropriately
        //  b) If you have seen it before, then just count it as a duplicate
        // 2) If it isn't a probe query....
        //  a) Is it an extension of a probe?  If so re-adjust the TTL.
        //  b) Is it a 'normal' query (no probe extension or already extended)?
        //  Then check if it is a duplicate:
        //    1) If it a duplicate, just count it as one
        //    2) If it isn't, put it in the route table but no need to setTTL

        // we msg.hop() before we get here....
        // hops may be 1 or 2 because we may be probing with a leaf query....
        final boolean isProbeQuery = 
            ((request.getTTL() == 0) && 
             ((request.getHops() == 1) || (request.getHops() == 2)));

		ResultCounter counter = 
			_queryRouteTable.tryToRouteReply(request.getGUID(), 
											 receivingConnection);

		if(counter != null) {  // query is new (probe or normal)
            // 1a: set the TTL of the query so it can be potentially extended  
            if (isProbeQuery) 
                _queryRouteTable.setTTL(counter, (byte)1);

            // 1a and 2b2
            // if a new probe or a new request, do everything (so input true
            // below)
            handleQueryRequest(request, receivingConnection, counter, true);
		} else if (!isProbeQuery) {// probe extension?
            if (wasProbeQuery(request)) {
                // rebroadcast out but don't locally evaluate....
                handleQueryRequest(request, receivingConnection, counter, 
                                   false);
            } else { // 2b1: not a correct extension, so call it a duplicate....
                tallyDupQuery(request);
            }
        } else if (isProbeQuery) { // 1b: duplicate probe
            tallyDupQuery(request);
        }
    }
	
    private boolean wasProbeQuery(QueryRequest request) {
        // if the current TTL is large enough and the old TTL was 1, then this
        // was a probe query....
        // NOTE: that i'm setting the ttl to be the actual ttl of the query.  i
        // could set it to some max value, but since we only allow TTL 1 queries
        // to be extended, it isn't a big deal what i set it to.  in fact, i'm
        // setting the ttl to the correct value if we had full expanding rings
        // of queries.
        return ((request.getTTL() > 0) && 
                _queryRouteTable.getAndSetTTL(request.getGUID(), (byte)1, 
                                              (byte)(request.getTTL()+1)));
    }

    private void tallyDupQuery(QueryRequest request) {
        duplicateQueryCounter.duplicateQuery(request);
    }

	/**
	 * Special handler for UDP queries.  Checks the routing table to see if
	 * the request has already been seen, handling it if not.
	 *
	 * @param query the UDP <tt>QueryRequest</tt> 
	 * @param handler the <tt>ReplyHandler</tt> that will handle the reply
	 * @return false if it was a duplicate, true if it was not.
	 */
	final boolean handleUDPQueryRequestPossibleDuplicate(QueryRequest request,
													  ReplyHandler handler)  {
		ResultCounter counter = 
			_queryRouteTable.tryToRouteReply(request.getGUID(), 
											 handler);
		if(counter != null) {
            handleQueryRequest(request, handler, counter, true);
            return true;
		}
		return false;
	}

    /**
     * Handles pings from the network.  With the addition of pong caching, this
     * method will either respond with cached pongs, or it will ignore the ping
     * entirely if another ping has been received from this connection very
     * recently.  If the ping is TTL=1, we will always process it, as it may
     * be a hearbeat ping to make sure the connection is alive and well.
     *
     * @param ping the ping to handle
     * @param handler the <tt>ReplyHandler</tt> instance that sent the ping
     */
    final private void handlePingRequest(PingRequest ping,
                                         ReplyHandler handler) {
        // Send it along if it's a heartbeat ping or if we should allow new 
        // pings on this connection.
        if(ping.isHeartbeat() || handler.allowNewPings()) {
            respondToPingRequest(ping, handler);
        } 
    }


    /**
     * The default handler for PingRequests received in
     * RoutedConnection.loopForMessages().  This implementation updates stats,
     * does the broadcast, and generates a response.
     *
     * You can customize behavior in three ways:
     *   1. Override. You can assume that duplicate messages
     *      (messages with the same GUID that arrived via different paths) have
     *      already been filtered.  If you want stats updated, you'll
     *      have to call super.handlePingRequest.
     *   2. Override broadcastPingRequest.  This allows you to use the default
     *      handling framework and just customize request routing.
     *   3. Implement respondToPingRequest.  This allows you to use the default
     *      handling framework and just customize responses.
     */
    protected void handleUDPPingRequest(PingRequest pingRequest,
										ReplyHandler handler, 
										InetSocketAddress addr) {
        if (pingRequest.isQueryKeyRequest())
            sendQueryKeyPong(pingRequest, addr);
        else
            respondToUDPPingRequest(pingRequest, addr, handler);
    }
    

    /**
     * Generates a AddressSecurityToken for the source (described by addr) and sends the
     * AddressSecurityToken to it via a AddressSecurityToken pong....
     */
    protected void sendQueryKeyPong(PingRequest pr, InetSocketAddress addr) {

        // check if we're getting bombarded
        long now = System.currentTimeMillis();
        if (now - _lastQueryKeyTime < SearchSettings.QUERY_KEY_DELAY.getValue())
            return;
        
        _lastQueryKeyTime = now;
        
        // after find more sources and OOB queries, everyone can dole out query
        // keys....

        // generate a AddressSecurityToken (quite quick - current impl. (DES) is super
        // fast!
        InetAddress address = addr.getAddress();
        int port = addr.getPort();
//        AddressSecurityToken key = new AddressSecurityToken(address, port, MACCalculatorRepositoryManager.get());
//        
//        // respond with Pong with QK, as GUESS requires....
//        PingReply reply = 
//            pingReplyFactory.createQueryKeyReply(pr.getGUID(), (byte)1, key);
//        udpService.send(reply, addr.getAddress(), addr.getPort());
    }


    protected void handleUDPPingReply(PingReply reply, ReplyHandler handler,
                                      InetAddress address, int port) {
//        if (reply.getQueryKey() != null) {
//            // this is a PingReply in reply to my AddressSecurityToken Request - 
//            //consume the Pong and return, don't process as usual....
//            onDemandUnicaster.handleQueryKeyPong(reply);
//            return;
//        }
//
//        // do not process the pong if different from the host
//        // described in the reply 
//        if((reply.getPort() != port) || 
//           (!reply.getInetAddress().equals(address))) {
//            return;
//		}
//        
//        // normal pong processing...
//        handlePingReply(reply, handler);
    }

    
    /**
     * The default handler for QueryRequests received in
     * RoutedConnection.loopForMessages().  This implementation updates stats,
     * does the broadcast, and generates a response.
     *
     * You can customize behavior in three ways:
     *   1. Override. You can assume that duplicate messages
     *      (messages with the same GUID that arrived via different paths) have
     *      already been filtered.  If you want stats updated, you'll
     *      have to call super.handleQueryRequest.
     *   2. Override broadcastQueryRequest.  This allows you to use the default
     *      handling framework and just customize request routing.
     *   3. Implement respondToQueryRequest.  This allows you to use the default
     *      handling framework and just customize responses.
     *
     * @param locallyEvaluate false if you don't want to send the query to
     * leaves and yourself, true otherwise....
     */
    protected void handleQueryRequest(QueryRequest request,
									  ReplyHandler handler, 
									  ResultCounter counter,
                                      boolean locallyEvaluate) {
        // Apply the personal filter to decide whether the callback
        // should be informed of the query
        if (!handler.isPersonalSpam(request)) {
            activityCallback.get().handleQueryString(request.getQuery());
        }
        
		// if it's a request from a leaf and we GUESS, send it out via GUESS --
		// otherwise, broadcast it if it still has TTL
		//if(handler.isSupernodeClientConnection() && 
		// RouterService.isGUESSCapable()) 
		//unicastQueryRequest(request, handler);
        //else if(request.getTTL() > 0) {
        updateMessage(request, handler);

		if(handler.isSupernodeClientConnection() && counter != null) {
            if (request.desiresOutOfBandReplies()) {
                // this query came from a leaf - so check if it desires OOB
                // responses and make sure that the IP it advertises is legit -
                // if it isn't drop away....
                // no need to check the port - if you are attacking yourself you
                // got problems
                String remoteAddr = handler.getInetAddress().getHostAddress();
                String myAddress = 
                    NetworkUtils.ip2string(networkManager.getAddress());
                if (request.getReplyAddress().equals(remoteAddr))
                    ; // continue below, everything looks good
                else if (request.getReplyAddress().equals(myAddress) && 
                         networkManager.isOOBCapable())
                    // i am proxying - maybe i should check my success rate but
                    // whatever...
                    ; 
                else return;
            }

            // don't send it to leaves here -- the dynamic querier will 
            // handle that
            locallyEvaluate = false;
            
            // do respond with files that we may have, though
            respondToQueryRequest(request, _clientGUID, handler);
            
            multicastQueryRequest(request);
            
			if(handler.isGoodLeaf()) {
				sendDynamicQuery(queryHandlerFactory.createHandlerForNewLeaf(request, 
																	  handler,
                                                                      counter), 
								 handler);
			} else {
				sendDynamicQuery(queryHandlerFactory.createHandlerForOldLeaf(request,
																	  handler,
                                                                      counter), 
								 handler);
			}
		} 
			
        if (locallyEvaluate) {
            // always forward any queries to leaves -- this only does
            // anything when this node's an Ultrapeer
            forwardQueryRequestToLeaves(request, handler);
            
            // if (I'm firewalled AND the source is firewalled) AND 
            // NOT(he can do a FW transfer and so can i) then don't reply...
            if ((request.isFirewalledSource() &&
                 !networkManager.acceptedIncomingConnection()) &&
                !(request.canDoFirewalledTransfer() &&
                  networkManager.canDoFWT())
                )
                return;
            respondToQueryRequest(request, _clientGUID, handler);
        }
    }

    /** Handles a ACK message - looks up the QueryReply and sends it out of
     *  band.
     */
    protected void handleLimeACKMessage(LimeACKVendorMessage ack,
                                        InetSocketAddress addr) {

//        GUID.TimedGUID refGUID = new GUID.TimedGUID(new GUID(ack.getGUID()),
//                                                    TIMED_GUID_LIFETIME);
//        QueryResponseBundle bundle = _outOfBandReplies.remove(refGUID);
//        
//        // token is null for old oob messages, it will just be ignored then
//        SecurityToken securityToken = ack.getSecurityToken();
//       
//        if ((bundle != null) && (ack.getNumResults() > 0)) {
//            InetAddress iaddr = addr.getAddress();
//            int port = addr.getPort();
//
//            //convert responses to QueryReplies, but only send as many as the
//            //node wants
//            Iterable<QueryReply> iterable;
//            if (ack.getNumResults() < bundle._responses.length) {
//                // TODO move selection to responseToQueryReplies methods for randomization
//                Response[] desired = new Response[ack.getNumResults()];
//                System.arraycopy(bundle._responses, 0, desired, 0, desired.length);
//                iterable = responsesToQueryReplies(desired, bundle._query, 1, securityToken);
//            } else { 
//                iterable = responsesToQueryReplies(bundle._responses, 
//                                                   bundle._query, 1, securityToken);
//            }
//            
//            //send the query replies
//            for(QueryReply queryReply : iterable)
//                udpService.send(queryReply, iaddr, port);
//        }
//        // else some sort of routing error or attack?
//        // TODO: tally some stat stuff here
    }
    
    /**
     * Adds the sender of the reply message to an internal cache under the guid of the message
     * if the sender can receive unsolicited UDP.
     * 
     * @see {@link #getQueryLocs(GUID)} for retrieval
     */
    public boolean addBypassedSource(ReplyNumberVendorMessage reply, ReplyHandler handler) {
        
        //if the reply cannot receive unsolicited udp, there is no point storing it
        if (!reply.canReceiveUnsolicited()) {
            return false;
        }

        GUESSEndpoint ep = new GUESSEndpoint(handler.getInetAddress(), handler.getPort());
        return _bypassedResultsCache.addBypassedSource(new GUID(reply.getGUID()), ep);
    }

    /**
     * Adds the sender of the reply message to an internal cache under the guid of the message
     * if the sender can receive unsolicited UDP.
     * 
     * @see {@link #getQueryLocs(GUID)} for retrieval
     */
    public boolean addBypassedSource(QueryReply reply, ReplyHandler handler) {
        try {
            if (reply.getHostData().isFirewalled())
                return false;
        } catch (BadPacketException bpe){
            return false;
        }
        GUESSEndpoint ep = new GUESSEndpoint(handler.getInetAddress(), handler.getPort());
        return _bypassedResultsCache.addBypassedSource(new GUID(reply.getGUID()), ep);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#getNumOOBToRequest(com.limegroup.gnutella.messages.vendor.ReplyNumberVendorMessage)
     */
    public int getNumOOBToRequest(ReplyNumberVendorMessage reply) {
    	GUID qGUID = new GUID(reply.getGUID());
    	
        int numResults = searchResultHandler.getNumResultsForQuery(qGUID);
    	
        if (numResults < 0) // this may be a proxy query
    		numResults = queryDispatcher.getLeafResultsForQuery(qGUID);

        if (numResults < 0 || numResults > QueryHandler.ULTRAPEER_RESULTS) {
            return -1;
        }
        
    	return reply.getNumResults();
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#isQueryAlive(com.limegroup.gnutella.GUID)
     */
    public boolean isQueryAlive(GUID guid) {
        return _queryRouteTable.getReplyHandler(guid.bytes()) != null;
    }

    /** Stores (for a limited time) the resps for later out-of-band delivery -
     *  interacts with handleLimeACKMessage
     *  @return true if the operation failed, false if not (i.e. too busy)
     */
    protected boolean bufferResponsesForLaterDelivery(QueryRequest query,
                                                      Response[] resps) {
        // store responses by guid for later retrieval
        synchronized (_outOfBandReplies) {
            if (_outOfBandReplies.size() < maxBufferedReplies) {
                GUID.TimedGUID tGUID = 
                    new GUID.TimedGUID(new GUID(query.getGUID()),
                                       TIMED_GUID_LIFETIME);
                _outOfBandReplies.put(tGUID, new QueryResponseBundle(query, 
                                                                     resps));
                return true;
            }
            return false;
        }
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#isHostUnicastQueried(com.limegroup.gnutella.GUID, org.limewire.io.IpPort)
     */
    public boolean isHostUnicastQueried(GUID guid, IpPort host) {
        return false;//onDemandUnicaster.isHostQueriedForGUID(guid, host);
    }
    
    /**
     * Forwards the UDPConnectBack to neighboring peers
     * as a UDPConnectBackRedirect request.
     */
    protected void handleUDPConnectBackRequest(UDPConnectBackVendorMessage udp,
                                               Connection source) {

        
    }


    /**
     * Sends a ping to the person requesting the connectback request.
     */
    protected void handleUDPConnectBackRedirect(UDPConnectBackRedirect udp,
                                               Connection source) {
        
    }
    
    /**
     * @param map the map that keeps track of recent redirects
     * @param key the key which we would (have) store(d) in the map
     * @return whether we should service the redirect request
     * @modifies the map
     */
    private boolean shouldServiceRedirect(FixedsizeHashMap<String, String> map, String key) {
        synchronized(map) {
            String placeHolder = map.get(key);
            if (placeHolder == null) {
                try {
                    map.put(key, key);
                    return true;
                } catch (NoMoreStorageException nomo) {
                    return false;  // we've done too many connect backs, stop....
                }
            } else 
                return false;  // we've connected back to this guy recently....
        }
    }



    /**
     * Forwards the request to neighboring Ultrapeers as a
     * TCPConnectBackRedirect message.
     */
    protected void handleTCPConnectBackRequest(TCPConnectBackVendorMessage tcp,
                                               Connection source) {
                
    }

    /**
     * Basically, just get the correct parameters, create a Socket, and
     * send a "/n/n".
     */
    protected void handleTCPConnectBackRedirect(TCPConnectBackRedirect tcp,
                                                Connection source) {
        // 
    }


    /**
     * 1) confirm that the connection is Ultrapeer to Leaf, then send your
     * listening port in a PushProxyAcknowledgement.
     * 2) Also cache the client's client GUID.
     */
    protected void handlePushProxyRequest(PushProxyRequest ppReq,
                                          RoutedConnection source) {
        if (source.isSupernodeClientConnection() 
                && networkManager.isIpPortValid()) {
            String stringAddr = 
                NetworkUtils.ip2string(networkManager.getAddress());
            InetAddress addr = null;
            try {
                addr = InetAddress.getByName(stringAddr);
            } catch(UnknownHostException uhe) {
                ErrorService.error(uhe); // impossible
            }

            // 1)
            PushProxyAcknowledgement ack = 
                new PushProxyAcknowledgement(addr,networkManager.getPort(),
                                             ppReq.getClientGUID());
            source.send(ack);
            
            // 2)
            _pushRouteTable.routeReply(ppReq.getClientGUID().bytes(), source);
            source.setPushProxyFor(true);
        }
    }

    /** This method should be invoked when this node receives a
     *  QueryStatusResponse message from the wire.  If this node is an
     *  Ultrapeer, we should update the Dynamic Querier about the status of
     *  the leaf's query.
     */    
    protected void handleQueryStatus(QueryStatusResponse resp,
                                     RoutedConnection leaf) {
        // message only makes sense if i'm a UP and the sender is a leaf
        if (!leaf.isSupernodeClientConnection())
            return;

        GUID queryGUID = resp.getQueryGUID();
        int numResults = resp.getNumResults();
        
        // get the QueryHandler and update the stats....
        queryDispatcher.updateLeafResultsForQuery(queryGUID, numResults);
    }


    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#sendPingRequest(com.limegroup.gnutella.messages.PingRequest, com.limegroup.gnutella.RoutedConnection)
     */
    public void sendPingRequest(PingRequest request,
                                RoutedConnection connection) {
//        if(request == null) {
//            throw new NullPointerException("null ping");
//        }
//        if(connection == null) {
//            throw new NullPointerException("null connection");
//        }
//        _pingRouteTable.routeReply(request.getGUID(), forMeReplyHandler);
//        connection.send(request);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#sendQueryRequest(com.limegroup.gnutella.messages.QueryRequest, com.limegroup.gnutella.RoutedConnection)
     */
    public void sendQueryRequest(QueryRequest request,
                                 RoutedConnection connection) {        
//        if(request == null) {
//            throw new NullPointerException("null query");
//        }
//        if(connection == null) {
//            throw new NullPointerException("null connection");
//        }
//        _queryRouteTable.routeReply(request.getGUID(), forMeReplyHandler);
//        connection.send(request);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#broadcastPingRequest(com.limegroup.gnutella.messages.PingRequest)
     */
    public void broadcastPingRequest(PingRequest ping) {
		
    }

	/* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#sendDynamicQuery(com.limegroup.gnutella.messages.QueryRequest)
     */
	public void sendDynamicQuery(QueryRequest query) {
		
    }
    
    /**
     * Originates a multicast query from this host.
     * This will alter the GUID of the query and store it in a mapping
     * of new -> old GUID.  When replies come in, if they have the new GUID,
     * they are reset to be the old one and the multicast flag is allowed.
     * 
     * @param query
     * @return the newGUID that the multicast query is using.
     */
    protected void originateMulticastQuery(QueryRequest query) {
        byte[] newGUID = GUID.makeGuid();
        QueryRequest mquery = queryRequestFactory.createMulticastQuery(newGUID, query);
        multicastGuidMap.addMapping(query.getGUID(), newGUID, MULTICAST_GUID_EXPIRE_TIME);
		multicastQueryRequest(mquery);
	}

	/**
	 * Initiates a dynamic query.  Only Ultrapeer should call this method,
	 * as this technique relies on fairly high numbers of connections to 
	 * dynamically adjust the TTL based on the number of results received, 
	 * the number of remaining connections, etc.
	 *
	 * @param qh the <tt>QueryHandler</tt> instance that generates
	 *  queries for this dynamic query
     * @param handler the <tt>ReplyHandler</tt> for routing replies for
     *  this query
	 * @throws <tt>NullPointerException</tt> if the <tt>ResultCounter</tt>
	 *  for the guid cannot be found -- this should never happen, or if any
	 *  of the arguments is <tt>null</tt>
	 */
	private void sendDynamicQuery(QueryHandler qh, ReplyHandler handler) {
		if(qh == null) {
			throw new NullPointerException("null QueryHandler");
		} else if(handler == null) {
			throw new NullPointerException("null ReplyHandler");
		} 
        queryDispatcher.addQuery(qh);
	}

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#forwardQueryRequestToLeaves(com.limegroup.gnutella.messages.QueryRequest, com.limegroup.gnutella.ReplyHandler)
     */
	public final void forwardQueryRequestToLeaves(QueryRequest query,
                                                  ReplyHandler handler) {
		
	}

	/**
	 * Factored-out method that sends a query to a connection that supports
	 * query routing.  The query is only forwarded if there's a hit in the
	 * query routing entries.
	 *
	 * @param query the <tt>QueryRequest</tt> to potentially forward
	 * @param mc the <tt>RoutedConnection</tt> to forward the query to
	 * @param handler the <tt>ReplyHandler</tt> that will be entered into
	 *  the routing tables to handle any replies
	 * @return <tt>true</tt> if the query was sent, otherwise <tt>false</tt>
	 */
	private boolean sendRoutedQueryToHost(QueryRequest query, RoutedConnection mc,
										  ReplyHandler handler) {
		if (mc.shouldForwardQuery(query)) {
			//A new client with routing entry, or one that hasn't started
			//sending the patch.
			mc.send(query);
			return true;
		}
		return false;
	}

    /**
     * Adds the QueryRequest to the unicaster module.  Not much work done here,
     * see QueryUnicaster for more details.
     */
    protected void unicastQueryRequest(QueryRequest query,
                                       ReplyHandler conn) {
        
		// set the TTL on outgoing udp queries to 1
		query.setTTL((byte)1);
				
		queryUnicaster.addQuery(query, conn);
	}
	
    /**
     * Send the query to the multicast group.
     */
    protected void multicastQueryRequest(QueryRequest query) {
		// set the TTL on outgoing udp queries to 1
		query.setTTL((byte)1);
				
		multicastService.send(query);
	}	


    /**
     * Broadcasts the query request to all initialized connections that
     * are not the receivingConnection, setting up the routing
     * to the designated QueryReplyHandler.  This is called from teh default
     * handleQueryRequest and the default broadcastQueryRequest(QueryRequest)
     *
     * If different (smarter) broadcasting functionality is desired, override
     * as desired.  If you do, note that receivingConnection may be null (for
     * requests originating here).
     */
    // default access for testing
    void forwardQueryToUltrapeers(QueryRequest query,
                                          ReplyHandler handler) {
		
    }

    /**
     * Performs a limited broadcast of the specified query.  This is
     * useful, for example, when receiving queries from old-style 
     * connections that we don't want to forward to all connected
     * Ultrapeers because we don't want to overly magnify the query.
     *
     * @param query the <tt>QueryRequest</tt> instance to forward
     * @param handler the <tt>ReplyHandler</tt> from which we received
     *  the query
     */
    // default access for testing
    void forwardLimitedQueryToUltrapeers(QueryRequest query,
                                                 ReplyHandler handler) {
		
    }

    /**
     * Forwards the specified query to the specified Ultrapeer.  This
     * encapsulates all necessary logic for forwarding queries to
     * Ultrapeers, for example handling last hop Ultrapeers specially
     * when the receiving Ultrapeer supports Ultrapeer query routing,
     * meaning that we check it's routing tables for a match before sending 
     * the query.
     *
     * @param query the <tt>QueryRequest</tt> to forward
     * @param handler the <tt>ReplyHandler</tt> that sent the query
     * @param ultrapeer the Ultrapeer to send the query to
     */
    // default access for testing
    void forwardQueryToUltrapeer(QueryRequest query, 
                                         ReplyHandler handler,
                                         RoutedConnection ultrapeer) {    
        
    }


    /**
     * Originate a new query from this leaf node.
     *
     * @param qr the <tt>QueryRequest</tt> to send
     */
    // default access for testing
    void originateLeafQuery(QueryRequest qr) {
		
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#originateQuery(com.limegroup.gnutella.messages.QueryRequest, com.limegroup.gnutella.RoutedConnection)
     */
    public boolean originateQuery(QueryRequest query, RoutedConnection mc) {
        
        return true;
    }
    
    /**
     * Respond to the ping request.  Implementations typically will either
     * do nothing (if they don't think a response is appropriate) or call
     * sendPingReply(PingReply).
     * This method is called from the default handlePingRequest.
     */
    protected abstract void respondToPingRequest(PingRequest request,
                                                 ReplyHandler handler);

	/**
	 * Responds to a ping received over UDP -- implementations
	 * handle this differently from pings received over TCP, as it is 
	 * assumed that the requester only wants pongs from other nodes
	 * that also support UDP messaging.
	 *
	 * @param request the <tt>PingRequest</tt> to service
     * @param addr the <tt>InetSocketAddress</tt> containing the ping
     * @param handler the <tt>ReplyHandler</tt> instance from which the
     *  ping was received and to which pongs should be sent
	 */
    protected abstract void respondToUDPPingRequest(PingRequest request, 
													InetSocketAddress addr,
                                                    ReplyHandler handler);


    /**
     * Respond to the query request.  Implementations typically will either
     * do nothing (if they don't think a response is appropriate) or call
     * sendQueryReply(QueryReply).
     * This method is called from the default handleQueryRequest.
     */
    protected abstract boolean respondToQueryRequest(QueryRequest queryRequest,
                                                     byte[] clientGUID,
                                                     ReplyHandler handler);

    /**
     * The default handler for PingRequests received in
     * RoutedConnection.loopForMessages().  This implementation
     * uses the ping route table to route a ping reply.  If an appropriate route
     * doesn't exist, records the error statistics.  On sucessful routing,
     * the PingReply count is incremented.<p>
     *
     * In all cases, the ping reply is recorded into the host catcher.<p>
     *
     * Override as desired, but you probably want to call super.handlePingReply
     * if you do.
     */
    protected void handlePingReply(PingReply reply,
                                   ReplyHandler handler) {
        
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#handleQueryReply(com.limegroup.gnutella.messages.QueryReply, com.limegroup.gnutella.ReplyHandler)
     */
    public void handleQueryReply(QueryReply queryReply,
                                 ReplyHandler handler) {
//        if(queryReply == null) {
//            throw new NullPointerException("null query reply");
//        }
//        if(handler == null) {
//            throw new NullPointerException("null ReplyHandler");
//        }
//        
//        // check the altloc count
//        if (!altCountOk(queryReply))
//            return;
//        
//        //For flow control reasons, we keep track of the bytes routed for this
//        //GUID.  Replies with less volume have higher priorities (i.e., lower
//        //numbers).
//        RouteTable.ReplyRoutePair rrp =
//            _queryRouteTable.getReplyHandler(queryReply.getGUID(),
//                                             queryReply.getTotalLength(),
//											 queryReply.getUniqueResultCount(),
//											 queryReply.getPartialResultCount(),
//                                             ByteOrder.beb2int(queryReply.getIPBytes(), 0));
//
//        if(rrp != null) {
//            queryReply.setPriority(rrp.getBytesRouted());
//            // Prepare a routing for a PushRequest, which works
//            // here like a QueryReplyReply
//            // Note the use of getClientGUID() here, not getGUID()
//            _pushRouteTable.routeReply(queryReply.getClientGUID(),
//                                       handler);
//            //Simple flow control: don't route this message along other
//            //connections if we've already routed too many replies for this
//            //GUID.  Note that replies destined for me all always delivered to
//            //the GUI.
//
//            ReplyHandler rh = rrp.getReplyHandler();
//            
//            // remember more stats
//            _queryRouteTable.countHopsTTLNet(queryReply);
//            // if this reply is for us, remember even more stats
//            if (rh == forMeReplyHandler)
//                _queryRouteTable.timeStampResults(queryReply);
//            
//            if(!shouldDropReply(rrp, rh, queryReply)) {                
//                rh.handleQueryReply(queryReply, handler);
//                // also add to the QueryUnicaster for accounting - basically,
//                // most results will not be relevant, but since it is a simple
//                // HashSet lookup, it isn't a prohibitive expense...
//                queryUnicaster.handleQueryReply(queryReply);
//
//            } else {
//                handler.countDroppedMessage();
//            }
//        } else {
//            handler.countDroppedMessage();
//        }
    }

    private boolean altCountOk(QueryReply qr) {
        try {
            for (Response r : qr.getResultsAsList()) {
                if (r.getLocations().size() > FilterSettings.MAX_ALTS_PER_RESPONSE.getValue())
                    return false;
            }
        } catch (BadPacketException bpe) {
            // may get routed to someone who can parse it
        }

        return true;
    }
    
    /**
     * Checks if the <tt>QueryReply</tt> should be dropped for various reasons.
     *
     * Reason 1) The reply has already routed enough traffic.  Based on per-TTL
     * hard limits for the number of bytes routed for the given reply guid.
     * This algorithm favors replies that don't have as far to go on the 
     * network -- i.e., low TTL hits have more liberal limits than high TTL
     * hits.  This ensures that hits that are closer to the query originator
     * -- hits for which we've already done most of the work, are not 
     * dropped unless we've routed a really large number of bytes for that
     * guid.  This method also checks that hard number of results that have
     * been sent for this GUID.  If this number is greater than a specified
     * limit, we simply drop the reply.
     *
     * Reason 2) The reply was meant for me -- DO NOT DROP.
     *
     * Reason 3) The TTL is 0, drop.
     *
     * @param rrp the <tt>ReplyRoutePair</tt> containing data about what's 
     *  been routed for this GUID
     * @param ttl the time to live of the query hit
     * @return <tt>true if the reply should be dropped, otherwise <tt>false</tt>
     */
    private boolean shouldDropReply(RouteTable.ReplyRoutePair rrp,
                                    ReplyHandler rh,
                                    QueryReply qr) {
//        byte ttl = qr.getTTL();
//                                           
//        // Reason 2 --  The reply is meant for me, do not drop it.
//        if( rh == forMeReplyHandler ) return false;
//        
//        // Reason 3 -- drop if TTL is 0.
//        if( ttl == 0 ) {
//            droppedReplyCounter.dropTTL0();
//            return true;                
//        }
//
//        // Reason 1 ...
//        
//        int resultsRouted = rrp.getResultsRouted();
//
//        // drop the reply if we've already sent more than the specified number
//        // of results for this GUID
//        if(resultsRouted > 100) {
//            droppedReplyCounter.tooManyResults((short)resultsRouted);
//            return true;
//        }
//
//        int bytesRouted = rrp.getBytesRouted();
//        // send replies with ttl above 2 if we've routed under 50K 
//        if(ttl > 2 && bytesRouted < 50    * 1024) return false;
//        // send replies with ttl 1 if we've routed under 1000K 
//        if(ttl == 1 && bytesRouted < 200 * 1024) return false;
//        // send replies with ttl 2 if we've routed under 333K 
//        if(ttl == 2 && bytesRouted < 100  * 1024) return false;
//
//        droppedReplyCounter.ttlByteDrop(ttl, bytesRouted);
//        // if none of the above conditions holds true, drop the reply
//        return true;
        return false;
    }


    /**
     *  Handles an update request by sending a response.
     */
    private void handleUpdateRequest(UpdateRequest req, ReplyHandler handler ) {

        
    }
    
    /** Handles a ContentResponse msg -- passing it to the ContentManager. */
    private void handleContentResponse(ContentResponse msg, ReplyHandler handler) {
        contentManager.handleContentResponse(msg);
    }

    /**
     * Passes the request onto the update manager.
     */
    private void handleUpdateResponse(UpdateResponse resp, ReplyHandler handler) {
        
    }

    /**
     * Returns the appropriate handler from the _pushRouteTable.
     * This enforces that requests for my clientGUID will return
     * FOR_ME_REPLY_HANDLER, even if it's not in the table.
     */
    public ReplyHandler getPushHandler(byte[] guid) {
//        ReplyHandler replyHandler = _pushRouteTable.getReplyHandler(guid);
//        if(replyHandler != null)
//            return replyHandler;
//        else if(Arrays.equals(_clientGUID, guid))
//            return forMeReplyHandler;
//        else
            return null;
    }

    /**
     * Uses the ping route table to send a PingReply to the appropriate
     * connection.  Since this is used for PingReplies orginating here, no
     * stats are updated.
     */
    protected void sendPingReply(PingReply pong, ReplyHandler handler) {
        if(pong == null) {
            throw new NullPointerException("null pong");
        }

        if(handler == null) {
            throw new NullPointerException("null reply handler");
        }
 
        handler.handlePingReply(pong, null);
    }

    /**
     * Uses the query route table to send a QueryReply to the appropriate
     * connection.  Since this is used for QueryReplies orginating here, no
     * stats are updated.
     * @throws IOException if no appropriate route exists.
     */
    protected void sendQueryReply(QueryReply queryReply)
        throws IOException {
        
        if(queryReply == null) {
            throw new NullPointerException("null reply");
        }
        //For flow control reasons, we keep track of the bytes routed for this
        //GUID.  Replies with less volume have higher priorities (i.e., lower
        //numbers).
        RouteTable.ReplyRoutePair rrp =
            _queryRouteTable.getReplyHandler(queryReply.getGUID(),
                                             queryReply.getTotalLength(),
											 queryReply.getResultCount(),
											 queryReply.getPartialResultCount(),
                                             0);

        if(rrp != null) {
            queryReply.setPriority(rrp.getBytesRouted());
            rrp.getReplyHandler().handleQueryReply(queryReply, null);
        }
        else
            throw new IOException("no route for reply");
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#sendPushRequest(com.limegroup.gnutella.messages.PushRequest)
     */
    public void sendPushRequest(PushRequest push)
        throws IOException {
//        if(push == null) {
//            throw new NullPointerException("null push");
//        }
//        
//
//        // Note the use of getClientGUID() here, not getGUID()
//        ReplyHandler replyHandler = getPushHandler(push.getClientGUID());
//
//        if(replyHandler != null)
//            replyHandler.handlePushRequest(push, forMeReplyHandler);
//        else
//            throw new IOException("no route for push");
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#sendMulticastPushRequest(com.limegroup.gnutella.messages.PushRequest)
     */
    public void sendMulticastPushRequest(PushRequest push) {
        if(push == null) {
            throw new NullPointerException("null push");
        }
        
        // must have a TTL of 1
        assert push.getTTL() == 1 : "multicast push ttl not 1";
        
        multicastService.send(push);
    }


    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#responsesToQueryReplies(com.limegroup.gnutella.Response[], com.limegroup.gnutella.messages.QueryRequest)
     */
    public Iterable<QueryReply> responsesToQueryReplies(Response[] responses,
                                            QueryRequest queryRequest) {
        return null;// responsesToQueryReplies(responses, queryRequest, 10, null);
    }


    /**
     * Converts the passed responses to QueryReplies. Each QueryReply can
     * accomodate atmost 255 responses. Not all the responses may get included
     * in QueryReplies in case the query request came from a far away host.
     * <p>
     * NOTE: This method doesnt have any side effect, 
     * and does not modify the state of this object
     * @param responses The responses to be converted
     * @param queryRequest The query request corresponding to which we are
     * generating query replies.
     * @param REPLY_LIMIT the maximum number of responses to have in each reply.
     * @param security token might be null
     * @return Iterable of QueryReply
     */
    // default access for testing
    Iterable<QueryReply> responsesToQueryReplies(Response[] responses,
                                             QueryRequest queryRequest,
                                             final int REPLY_LIMIT) {

        //List to store Query Replies
        List<QueryReply> queryReplies = new LinkedList<QueryReply>();
        
        // get the appropriate queryReply information
        byte[] guid = queryRequest.getGUID();
        byte ttl = (byte)(queryRequest.getHops() + 1);

//        //Return measured speed if possible, or user's speed otherwise.
//        long speed = uploadManager.measuredUploadSpeed();
//        boolean measuredSpeed=true;
//        if (speed==-1) {
//            speed=ConnectionSettings.CONNECTION_SPEED.getValue();
//            measuredSpeed=false;
//        }

        int numResponses = responses.length;
        int index = 0;

        int numHops = queryRequest.getHops();

        // limit the responses if we're not delivering this 
        // out-of-band and we have a lot of responses
        if(REPLY_LIMIT > 1 && 
           numHops > 2 && 
           numResponses > HIGH_HOPS_RESPONSE_LIMIT) {
            int j = 
                (int)(Math.random() * numResponses) % 
                (numResponses - HIGH_HOPS_RESPONSE_LIMIT);

            Response[] newResponses = 
                new Response[HIGH_HOPS_RESPONSE_LIMIT];
            for(int i=0; i<10; i++, j++) {
                newResponses[i] = responses[j];
            }
            responses = newResponses;
            numResponses = responses.length;
        }
        while (numResponses > 0) {
            int arraySize;
            // if there are more than 255 responses,
            // create an array of 255 to send in the queryReply
            // otherwise, create an array of whatever size is left.
            if (numResponses < REPLY_LIMIT) {
                // break;
                arraySize = numResponses;
            }
            else
                arraySize = REPLY_LIMIT;

            Response[] res;
            // a special case.  in the common case where there
            // are less than 256 responses being sent, there
            // is no need to copy one array into another.
            if ( (index == 0) && (arraySize < REPLY_LIMIT) ) {
                res = responses;
            }
            else {
                res = new Response[arraySize];
                // copy the reponses into bite-size chunks
                for(int i =0; i < arraySize; i++) {
                    res[i] = responses[index];
                    index++;
                }
            }

            // decrement the number of responses we have left
            numResponses-= arraySize;

			// see if there are any open slots
            // Note: if we are busy, non-metafile results would be filtered.
            // by this point.
			boolean busy = false;//!uploadManager.mayBeServiceable();
            boolean uploaded = false;//uploadManager.hadSuccesfulUpload();
			
            // We only want to return a "reply to multicast query" QueryReply
            // if the request travelled a single hop.
			boolean mcast = queryRequest.isMulticast() && 
                (queryRequest.getTTL() + queryRequest.getHops()) == 1;
			
            // We should mark our hits if the remote end can do a firewalled
            // transfer AND so can we AND we don't accept tcp incoming AND our
            // external address is valid (needed for input into the reply)
            final boolean fwTransfer = 
                queryRequest.canDoFirewalledTransfer() && 
                networkManager.canDoFWT() &&
                !networkManager.acceptedIncomingConnection();
            
			if ( mcast ) {
                ttl = 1; // not strictly necessary, but nice.
            }
            
//            List<QueryReply> replies =
//                createQueryReply(guid, ttl, speed, res, 
//                                 _clientGUID, busy, uploaded, 
//                                 measuredSpeed, mcast,
//                                 fwTransfer, securityToken);
//
//            //add to the list
//            queryReplies.addAll(replies);

        }//end of while
        
        return queryReplies;
    }


    /**
     * Handles a message to reset the query route table for the given
     * connection.
     *
     * @param rtm the <tt>ResetTableMessage</tt> for resetting the query
     *  route table
     * @param mc the <tt>RoutedConnection</tt> for which the query route
     *  table should be reset
     */
    private void handleResetTableMessage(ResetTableMessage rtm,
                                         RoutedConnection mc) {
        // if it's not from a leaf or an Ultrapeer advertising 
        // QRP support, ignore it
        if(!isQRPConnection(mc)) return;

        // reset the query route table for this connection
        synchronized (mc.getQRPLock()) {
            mc.resetQueryRouteTable(rtm);
        }

        // if this is coming from a leaf, make sure we update
        // our tables so that the dynamic querier has correct
        // data
        if(mc.isLeafConnection()) {
            _lastQueryRouteTable = createRouteTable();
        }
    }

    /**
     * Handles a message to patch the query route table for the given
     * connection.
     *
     * @param rtm the <tt>PatchTableMessage</tt> for patching the query
     *  route table
     * @param mc the <tt>RoutedConnection</tt> for which the query route
     *  table should be patched
     */
    private void handlePatchTableMessage(PatchTableMessage ptm,
                                         RoutedConnection mc) {
        // if it's not from a leaf or an Ultrapeer advertising 
        // QRP support, ignore it
        if(!isQRPConnection(mc)) return;

        // patch the query route table for this connection
        synchronized(mc.getQRPLock()) {
            mc.patchQueryRouteTable(ptm);
        }

        // if this is coming from a leaf, make sure we update
        // our tables so that the dynamic querier has correct
        // data
        if(mc.isLeafConnection()) {
            _lastQueryRouteTable = createRouteTable();
        }
    }

    private void updateMessage(QueryRequest request, ReplyHandler handler) {
        
        
    }

    /**
     * Utility method for checking whether or not the given connection
     * is able to pass QRP messages.
     *
     * @param c the <tt>Connection</tt> to check
     * @return <tt>true</tt> if this is a QRP-enabled connection,
     *  otherwise <tt>false</tt>
     */
    private static boolean isQRPConnection(Connection c) {
        
        return false;
    }

    /** Thread the processing of QRP Table delivery. */
    private class QRPPropagator extends ManagedThread {
        public QRPPropagator() {
            setName("QRPPropagator");
            setDaemon(true);
        }

        /** While the connection is not closed, sends all data delay. */
        public void run() {
            try {
                while (true) {
					// Check for any scheduled QRP table propagations
					// every 10 seconds
                    Thread.sleep(10*1000);
    				forwardQueryRouteTables();
                }
            } catch(Throwable t) {
                ErrorService.error(t);
            }
        }
    }


    /**
     * Sends updated query routing tables to all connections which haven't
     * been updated in a while.  You can call this method as often as you want;
     * it takes care of throttling.
     *     @modifies connections
     */
    // default access for testing
    void forwardQueryRouteTables() {
		
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#getQueryRouteTable()
     */
    public QueryRouteTable getQueryRouteTable() {
        return _lastQueryRouteTable;
    }

    /**
     * Creates a query route table appropriate for forwarding to connection c.
     * This will not include information from c.
     *     @requires queryUpdateLock held
     */
    //default access for testing
    QueryRouteTable createRouteTable() {
       return null;
    }


	/**
	 * Adds all query routing tables of leaves to the query routing table for
	 * this node for propagation to other Ultrapeers at 1 hop.
	 * 
	 * Added "busy leaf" support to prevent a busy leaf from having its QRT
	 * 	table added to the Ultrapeer's last-hop QRT table.  This should reduce
	 *  BW costs for UPs with busy leaves.  
	 *
	 * @param qrt the <tt>QueryRouteTable</tt> to add to
	 */
	private void addQueryRoutingEntriesForLeaves(QueryRouteTable qrt) {
		
	}

    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#registerMessageListener(byte[], com.limegroup.gnutella.MessageListener)
     */
    public void registerMessageListener(byte[] guid, MessageListener ml) {
        ml.registered(guid);
        synchronized(MESSAGE_LISTENER_LOCK) {
            Map<byte[], List<MessageListener>> listeners =
                new TreeMap<byte[], List<MessageListener>>(GUID.GUID_BYTE_COMPARATOR);
            listeners.putAll(_messageListeners);
            List<MessageListener> all = listeners.get(guid);
            if(all == null) {
                all = new ArrayList<MessageListener>(1);
                all.add(ml);
            } else {
                List<MessageListener> temp = new ArrayList<MessageListener>(all.size() + 1);
                temp.addAll(all);
                all = temp;
                all.add(ml);
            }
            listeners.put(guid, Collections.unmodifiableList(all));
            _messageListeners = Collections.unmodifiableMap(listeners);
        }
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#unregisterMessageListener(byte[], com.limegroup.gnutella.MessageListener)
     */
    public void unregisterMessageListener(byte[] guid, MessageListener ml) {
        boolean removed = false;
        synchronized(MESSAGE_LISTENER_LOCK) {
            List<MessageListener> all = _messageListeners.get(guid);
            if(all != null) {
                all = new ArrayList<MessageListener>(all);
                if(all.remove(ml)) {
                    removed = true;
                    Map<byte[], List<MessageListener>> listeners =
                        new TreeMap<byte[], List<MessageListener>>(GUID.GUID_BYTE_COMPARATOR);
                    listeners.putAll(_messageListeners);
                    if(all.isEmpty())
                        listeners.remove(guid);
                    else
                        listeners.put(guid, Collections.unmodifiableList(all));
                    _messageListeners = Collections.unmodifiableMap(listeners);
                }
            }
        }
        if(removed)
            ml.unregistered(guid);
    }


    /**
     * Replies to a head ping sent from the given ReplyHandler.
     */
    private void handleHeadPing(HeadPing ping, ReplyHandler handler) {
        
   }
    
    /**
     * Pure testing method. 
     */
    Map<byte[], List<MessageListener>> getMessageListenerMap() {
        return _messageListeners;
    }
    
    
    /** 
     * Handles a pong received from the given handler.
     */ 
    private void handleHeadPong(HeadPong pong, ReplyHandler handler) { 
        
    } 
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#forwardInspectionRequestToLeaves(com.limegroup.gnutella.messages.vendor.InspectionRequest)
     */
    public void forwardInspectionRequestToLeaves(InspectionRequest ir) {
        
    }
    
    /**
     * Should only be used for testing.
     */
    RouteTable getPushRouteTable() {
        return _pushRouteTable;
    }
    
    RouteTable getHeadPongRouteTable() {
        return _headPongRouteTable;
    }
    
    /**
     * Default access for testing only. 
     */
    Map<GUID.TimedGUID, QueryResponseBundle> getOutOfBandReplies() {
        return _outOfBandReplies;
    }
    
    /**
     * Default access use for testing only. 
     */
    void setMaxBufferedReplies(int maxBufferedReplies) {
        this.maxBufferedReplies = maxBufferedReplies;
    }
    
    private static class QueryResponseBundle {
        public final QueryRequest _query;
        public final Response[] _responses;
        
        public QueryResponseBundle(QueryRequest query, Response[] responses) {
            _query = query;
            _responses = responses;
        }
    }

    /** Expires the UDP-Reply cache. */
    private class UDPReplyCleaner implements Runnable {
        public void run() {
            messageDispatcher.get().dispatch(new Runnable() {
                public void run() {
                    udpReplyHandlerCache.clear();
                }
            });
        }
        
    }

    /** Can be run to invalidate out-of-band ACKs that we are waiting for....
     */
    private class Expirer implements Runnable {
        public void run() {
            Set<GUID.TimedGUID> toRemove = new HashSet<GUID.TimedGUID>();
            synchronized (_outOfBandReplies) {
                long now = System.currentTimeMillis();
                for(GUID.TimedGUID currQB : _outOfBandReplies.keySet()) {
                    if ((currQB != null) && (currQB.shouldExpire(now)))
                        toRemove.add(currQB);
                }
                // done iterating through _outOfBandReplies, remove the 
                // keys now...
                for(GUID.TimedGUID next : toRemove)
                    _outOfBandReplies.remove(next);
            }
        }
    }


    /** This is run to clear out the registry of connect back attempts...
     *  Made package access for easy test access.
     */
    static class ConnectBackExpirer implements Runnable {
        public void run() {
            _tcpConnectBacks.clear();
            _udpConnectBacks.clear();
        }
    }

    
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.MessageRouter#getOOBExpireTime()
     */
    public long getOOBExpireTime() {
    	return OOB_SESSION_EXPIRE_TIME;
    }
    
    /*
     * ===================================================
     *                   "REGULAR" HANDLER
     * ===================================================
     */
    private class PingRequestHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handlePingRequestPossibleDuplicate((PingRequest)msg, handler);
        }
    }
    
    private class PingReplyHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handlePingReply((PingReply)msg, handler);
        }
    }
    
    private class QueryRequestHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handleQueryRequestPossibleDuplicate(
                (QueryRequest)msg, (RoutedConnection)handler);
        }
    }
    
    private class QueryReplyHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            // if someone sent a TCP QueryReply with the MCAST header,
            // that's bad, so ignore it.
            QueryReply qmsg = (QueryReply)msg;
            handleQueryReply(qmsg, handler);
        }
    }
    
    private class ResetTableHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handleResetTableMessage((ResetTableMessage)msg,
                    (RoutedConnection)handler);
        }
    }
    
    private class PatchTableHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handlePatchTableMessage((PatchTableMessage)msg,
                    (RoutedConnection)handler); 
        }
    }
    
    private class TCPConnectBackHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handleTCPConnectBackRequest((TCPConnectBackVendorMessage) msg,
                    (RoutedConnection)handler);
        }
    }
    
    private class UDPConnectBackHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handleUDPConnectBackRequest((UDPConnectBackVendorMessage) msg,
                    (RoutedConnection)handler);
        }
    }
    
    private class TCPConnectBackRedirectHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handleTCPConnectBackRedirect((TCPConnectBackRedirect) msg,
                    (RoutedConnection)handler);
        }
    }
    
    private class UDPConnectBackRedirectHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handleUDPConnectBackRedirect((UDPConnectBackRedirect) msg,
                    (RoutedConnection)handler);
        }
    }
    
    private class PushProxyRequestHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handlePushProxyRequest((PushProxyRequest) msg, (RoutedConnection)handler);
        }
    }
    
    private class QueryStatusResponseHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handleQueryStatus((QueryStatusResponse) msg, (RoutedConnection)handler);
        }
    }
    
    private class HeadPingHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            //TODO: add the statistics recording code
            handleHeadPing((HeadPing)msg, handler);
        }
    }
    
    private class UpdateRequestHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handleUpdateRequest((UpdateRequest)msg, handler);
        }
    }
    
    private class UpdateResponseHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handleUpdateResponse((UpdateResponse)msg, handler);
        }
    }
    
    private class HeadPongHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handleHeadPong((HeadPong)msg, handler); 
        }
    }
    
    private class DHTContactsMessageHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            //handleDHTContactsMessage((DHTContactsMessage)msg, handler); 
        }
    }
    
    public class VendorMessageHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            Connection c = (Connection)handler;
            c.handleVendorMessage((VendorMessage)msg);
        }
    }
    
    /*
     * ===================================================
     *                     UDP HANDLER
     * ===================================================
     */
    private class UDPQueryRequestHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            InetAddress address = addr.getAddress();
            int port = addr.getPort();
            
            // TODO: compare AddressSecurityToken with old generation params.  if it matches
            //send a new one generated with current params 
            if (hasValidQueryKey(address, port, (QueryRequest) msg)) {
                sendAcknowledgement(addr, msg.getGUID());
                // a TTL above zero may indicate a malicious client, as UDP
                // messages queries should not be sent with TTL above 1.
                //if(msg.getTTL() > 0) return;
                handleUDPQueryRequestPossibleDuplicate((QueryRequest)msg, handler);
            }
        }
    }
    
    private class UDPPingRequestHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handleUDPPingRequestPossibleDuplicate((PingRequest)msg, handler, addr);
        }
    }
    
    private class UDPPingReplyHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handleUDPPingReply((PingReply)msg, handler, addr.getAddress(), addr.getPort());
        }
    }
    
    private class UDPLimeACKVendorMessageHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handleLimeACKMessage((LimeACKVendorMessage)msg, addr);
        }
    }
    
    private class UDPHeadPingHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            //TODO: add the statistics recording code
            handleHeadPing((HeadPing)msg, handler);
        }
    }
    
    private class UDPUpdateRequestHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handleUpdateRequest((UpdateRequest)msg, handler);
        }
    }
    
    private class UDPContentResponseHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, ReplyHandler handler) {
            handleContentResponse((ContentResponse)msg, handler);
        }
    }
    
    /*
     * ===================================================
     *                  MULTICAST HANDLER
     * ===================================================
     */
    public class MulticastQueryRequestHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr,
                ReplyHandler handler) {
            handleUDPQueryRequestPossibleDuplicate((QueryRequest) msg, handler);
        }
    }
    
    public class MulticastQueryReplyHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, 
                ReplyHandler handler) {
            handleQueryReply((QueryReply)msg, handler);
        }
    }
    
    public class MulticastPingRequestHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, 
                ReplyHandler handler) {
            handleUDPPingRequestPossibleDuplicate((PingRequest)msg, handler, addr);
        }
    }
    
    public class MulticastPingReplyHandler implements MessageHandler {
        public void handleMessage(Message msg, InetSocketAddress addr, 
                ReplyHandler handler) {
            handleUDPPingReply((PingReply)msg, handler, addr.getAddress(), addr.getPort());
        }
    }
 
    /**
     * This class handles UDP query replies and forwards them to the 
     * {@link OOBHandler} if they are not replies to multicast or unicast
     * queries.
     */
    
    
    /**
     * tracks information about messages with a specified guid.
     */
    private class GUIDTracker implements Inspectable, MessageListener, SettingListener {
        private final List<Map<String,Object>> l = 
            Collections.synchronizedList(new ArrayList<Map<String,Object>>());
        
        private volatile long start;
        
        private volatile byte[] lastGuid;
        public GUIDTracker() {
            MessageSettings.TRACKING_GUID.addSettingListener(this);
        }
        
        public Object inspect() {
            Map<String,Object> ret = new HashMap<String,Object>();
            ret.put("start",start);
            if (lastGuid != null)
                ret.put("guid", Base32.encode(lastGuid));
            ret.put("messages",l);
            return ret;
        }
        
        public void processMessage(Message m, ReplyHandler handler) {
            Map<String,Object> data = new HashMap<String,Object>();
            data.put("ver",1);
            data.put("time", System.currentTimeMillis());
            data.put("source",handler.getInetAddress()+":"+handler.getPort());
            data.put("type",m.getClass().getName());
            data.put("hops",m.getHops());
            data.put("ttl",m.getTTL());
            data.put("net",m.getNetwork());
            data.put("len",m.getLength()); // why not
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                m.writeQuickly(baos);
                data.put("pay",baos.toByteArray());
            } catch (IOException impossible){}
            l.add(data);
        }
        
        public void registered(final byte[] guid) {
            start = System.currentTimeMillis();
            lastGuid = guid;
            // don't keep history for ever.
            backgroundExecutor.schedule(new Runnable() {
                public void run () {
                    byte [] last = lastGuid;
                    if (last != null && Arrays.equals(guid,last))
                        unregisterMessageListener(guid, GUIDTracker.this);
                }
            }, 600, TimeUnit.SECONDS);
        }
        
        public void unregistered(byte[] guid) {
            start = 0;
            lastGuid = null;
            l.clear(); // this is the only list manipulation outside of messagedispatch thread
        }
        
        public void settingChanged(SettingEvent evt) {
            if (evt.getEventType() != SettingEvent.EventType.VALUE_CHANGED ||
                    evt.getSetting() != MessageSettings.TRACKING_GUID)
                return;
            byte [] last = lastGuid;
            if (last != null)
                unregisterMessageListener(last, this);
            String newGuid = MessageSettings.TRACKING_GUID.getValue();
            if (newGuid.length() == 0)
                return;
            byte[] guid = Base32.decode(newGuid);
            registerMessageListener(guid, this);
        }
    }        
    
    /**
     * Counts reasons why a reply would be dropped.
     */
    private static class DroppedReplyCounter implements Inspectable {
        // buffer storage is actually inited lazily
        private final Buffer<Short> tooManyResults = new Buffer<Short>(50);
        private final Buffer<Byte> ttls = new Buffer<Byte>(50);
        private final Buffer<Integer> bytesRouted = new Buffer<Integer>(50);
        private long totalDropped, droppedTTL0;
        
        public synchronized Object inspect() {
            byte [] tooManyResultsByte = new byte[tooManyResults.getSize() * 2];
            for (int i = 0; i < tooManyResults.getSize(); i++)
                ByteOrder.short2beb(tooManyResults.get(i), tooManyResultsByte, i * 2);
            
            byte [] ttlsByte = new byte[ttls.getSize()];
            for (int i = 0; i < ttls.getSize(); i++)
                ttlsByte[i] = ttls.get(i);
            
            byte [] bytesRoutedByte = new byte[bytesRouted.getSize() * 4];
            for (int i = 0; i < bytesRouted.getSize(); i++)
                ByteOrder.int2beb(bytesRouted.get(i), bytesRoutedByte, i * 4);
            
            Map<String,Object> ret = new HashMap<String,Object>();
            ret.put("tooMany",tooManyResultsByte);
            ret.put("ttls",ttlsByte);
            ret.put("bytes",bytesRoutedByte);
            ret.put("total",totalDropped);
            ret.put("ttl0",droppedTTL0);
            return ret;
        }
        
        public synchronized void tooManyResults(short numResults) {
            if (!FrostWireUtils.isBetaRelease())
                return;
            totalDropped++;
            tooManyResults.add(numResults);
        }
        
        public synchronized void ttlByteDrop(byte ttl, int bytes) {
            if (!FrostWireUtils.isBetaRelease())
                return;
            totalDropped++;
            ttls.add(ttl);
            bytesRouted.add(bytes);
        }
        
        public synchronized void dropTTL0() {
            // not checking isBeta since these fields are already allocated
            totalDropped++;
            droppedTTL0++;
        }
    }
    
    private static class DuplicateQueryCounter implements Inspectable {
        private final Buffer<Long> duplicateTimes = new Buffer<Long>(100);
        private final Buffer<Byte> duplicateHops = new Buffer<Byte>(100);
        private final Buffer<Byte> duplicateTTLs = new Buffer<Byte>(100);
        private long totalDropped;
        
        private final Buffer<GUID> lastNGUIDs = new Buffer<GUID>(100);
        private final Map<GUID, Integer> counts = new HashMap<GUID,Integer>();
        private int highestEver;
        
        
        public synchronized void duplicateQuery(QueryRequest r) {
            totalDropped++;
            if (!FrostWireUtils.isBetaRelease())
                return;
            
            duplicateTimes.add(System.currentTimeMillis());
            duplicateHops.add(r.getHops());
            duplicateTTLs.add(r.getTTL());
            GUID g = new GUID(r.getGUID());
            Integer num = counts.get(g);
            if (num != null) {
                num++;
                highestEver = Math.max(highestEver, num);
                counts.put(g,num);
                return;
            }
            
            num = 1;
            counts.put(g, num);
            GUID ejected = lastNGUIDs.add(g);
            if (ejected != null)
                counts.remove(ejected);
        }
        
        public synchronized Object inspect() {
            Map<String,Object> ret = new HashMap<String,Object>();
            ret.put("total",totalDropped);
            ret.put("highest",highestEver);
            
            byte [] ttlsByte = new byte[duplicateTTLs.getSize()];
            for (int i = 0; i < duplicateTTLs.getSize(); i++)
                ttlsByte[i] = duplicateTTLs.get(i);
            
            byte [] hopsByte = new byte[duplicateHops.getSize()];
            for (int i = 0; i < duplicateHops.getSize(); i++)
                hopsByte[i] = duplicateHops.get(i);
            
            byte [] timesByte = new byte[duplicateTimes.getSize() * 8];
            for (int i = 0; i < duplicateTimes.getSize(); i++)
                ByteOrder.long2beb(duplicateTimes.get(i), timesByte, i * 8);
            
            ret.put("ttls",ttlsByte);
            ret.put("hops",hopsByte);
            ret.put("times",timesByte);
            ret.put("hist", new HashMap<GUID,Integer>(counts));
            return ret;
        }
    }
    
    private class ConnectionListener implements ConnectionLifecycleListener {

        public void handleConnectionLifecycleEvent(ConnectionLifecycleEvent evt) {
            if (evt.isConnectionClosedEvent()) {
                removeConnection(evt.getConnection());
            }
        }
        
    }
}
