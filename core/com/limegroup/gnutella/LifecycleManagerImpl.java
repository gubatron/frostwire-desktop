package com.limegroup.gnutella;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.concurrent.ThreadExecutor;
import org.limewire.i18n.I18nMarker;
import org.limewire.inspection.InspectablePrimitive;
import org.limewire.lifecycle.ServiceRegistry;
import org.limewire.listener.EventListener;
import org.limewire.listener.EventListenerList;
import org.limewire.service.ErrorService;
import org.limewire.setting.SettingsGroupManager;
import org.limewire.statistic.StatisticAccumulator;
import org.limewire.util.OSUtils;
import org.limewire.util.SystemUtils;

import com.frostwire.bittorrent.AzureusStarter;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.limegroup.gnutella.auth.ContentManager;
import com.limegroup.gnutella.licenses.LicenseFactory;
import com.limegroup.gnutella.settings.ApplicationSettings;
import com.limegroup.gnutella.settings.ConnectionSettings;
import com.limegroup.gnutella.spam.RatingTable;

@Singleton
public class LifecycleManagerImpl implements LifecycleManager {
    
    private static final Log LOG = LogFactory.getLog(LifecycleManagerImpl.class);
   
    private final AtomicBoolean preinitializeBegin = new AtomicBoolean(false);
    private final AtomicBoolean preinitializeDone = new AtomicBoolean(false);
    private final AtomicBoolean backgroundBegin = new AtomicBoolean(false);
    private final AtomicBoolean backgroundDone = new AtomicBoolean(false);
    private final AtomicBoolean startBegin = new AtomicBoolean(false);
    private final AtomicBoolean startDone = new AtomicBoolean(false);
    private final AtomicBoolean shutdownBegin = new AtomicBoolean(false);
    private final AtomicBoolean shutdownDone = new AtomicBoolean(false);
    
    private final CountDownLatch startLatch = new CountDownLatch(1);
    
    private static enum State { NONE, STARTING, STARTED, STOPPED };

    private final Provider<ActivityCallback> activityCallback;
    private final Provider<ContentManager> contentManager;
    private final Provider<MessageRouter> messageRouter;
    private final Provider<DownloadManager> downloadManager;
    private final Provider<NodeAssigner> nodeAssigner;
    private final Provider<RatingTable> ratingTable;
    private final Provider<NetworkManager> networkManager;
    private final Provider<Statistics> statistics;
    private final Provider<LimeCoreGlue> limeCoreGlue;
    private final Provider<StatisticAccumulator> statisticAccumulator;
    
    /** A list of items that require running prior to shutting down LW. */
    private final List<Thread> SHUTDOWN_ITEMS =  Collections.synchronizedList(new LinkedList<Thread>());
    /** The time when this finished starting. */
    @InspectablePrimitive("time lifecycle finished starting") 
    private long startFinishedTime;


    private final Provider<LicenseFactory> licenseFactory;
    
    private final EventListenerList<LifeCycleEvent> listenerList;
    
    public static enum LifeCycleEvent {
        STARTING, STARTED, SHUTINGDOWN, SHUTDOWN
    }
    
    //private static enum State { NONE, STARTING, STARTED, STOPPED };

    private final ServiceRegistry serviceRegistry;

    /*
    @Inject
    public LifecycleManagerImpl(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.listenerList = new EventListenerList<LifeCycleEvent>();
    }
    */
    
    
    /**/
    @Inject
    public LifecycleManagerImpl(             
            Provider<ActivityCallback> activityCallback,
            Provider<ContentManager> contentManager,
            Provider<MessageRouter> messageRouter,
            Provider<DownloadManager> downloadManager,
            Provider<NodeAssigner> nodeAssigner,
            Provider<RatingTable> ratingTable,
            @Named("backgroundExecutor") Provider<ScheduledExecutorService> backgroundExecutor,
            Provider<NetworkManager> networkManager,
            Provider<Statistics> statistics,
            Provider<LicenseFactory> licenseFactory,
            Provider<LimeCoreGlue> limeCoreGlue,
            Provider<StatisticAccumulator> statisticAccumulator,
            ServiceRegistry serviceRegistry) {
        
        this.serviceRegistry = serviceRegistry;
        this.listenerList = new EventListenerList<LifeCycleEvent>();

        
        this.activityCallback = activityCallback;
        this.contentManager = contentManager;
        this.messageRouter = messageRouter;
        this.downloadManager = downloadManager;
        this.nodeAssigner = nodeAssigner;
        this.ratingTable = ratingTable;
        this.networkManager = networkManager;
        this.statistics = statistics;
        this.licenseFactory = licenseFactory;
        this.limeCoreGlue = limeCoreGlue;
        this.statisticAccumulator = statisticAccumulator;
    }
    /**/
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.LifecycleManager#isLoaded()
     */
    public boolean isLoaded() {
        State state = getCurrentState();
        return state == State.STARTED || state == State.STARTING;
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.LifecycleManager#isStarted()
     */
    public boolean isStarted() {
        State state = getCurrentState();
        return state == State.STARTED || state == State.STOPPED;
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.LifecycleManager#isShutdown()
     */
    public boolean isShutdown() {
        return getCurrentState() == State.STOPPED;
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.LifecycleManager#installListeners()
     */
    public void installListeners() {
        if(preinitializeBegin.getAndSet(true))
            return;
        
        LimeCoreGlue.preinstall();
       
        serviceRegistry.initialize();
        
        //fileManager.get().addFileEventListener(activityCallback.get());
        
        //connectionManager.get().addEventListener(activityCallback.get());
        
        preinitializeDone.set(true);

        //NEW, for azureus core to be notified of FrostWire's lifecycle events.
        installAzureusCoreListeners();
    }
    
    
    /**
     * Make sure the azureus core knows about our LifeCycleEvents and does the right thing
     * when we shut down.
     */
    private void installAzureusCoreListeners() {
    	addListener(new EventListener<LifecycleManagerImpl.LifeCycleEvent>() {

			@Override
			public void handleEvent(
					com.limegroup.gnutella.LifecycleManagerImpl.LifeCycleEvent event) {
				if (event == LifeCycleEvent.SHUTINGDOWN) {
					
					if (AzureusStarter.getAzureusCore().isStarted()) {
						LOG.debug("LifecycleManagerImpl.handleEvent - SHUTINGDOWN - Azureus core pauseDownloads()!");
						AzureusStarter.getAzureusCore().getGlobalManager().pauseDownloads();
						AzureusStarter.getAzureusCore().stop();
					}
				} 
			}
			
		});
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.LifecycleManager#loadBackgroundTasks()
     */
    public void loadBackgroundTasks() {
        if(backgroundBegin.getAndSet(true))
            return;

        installListeners();
        
        ThreadExecutor.startThread(new Runnable() {
            public void run() {
                doBackgroundTasks();
            }
        }, "BackgroundTasks");
    }
    
    private void loadBackgroundTasksBlocking() {
        if(backgroundBegin.getAndSet(true))
            return;

        installListeners();
        
        doBackgroundTasks();
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.LifecycleManager#start()
     */
    public void start() {
        if(startBegin.getAndSet(true))
            return;
        
        try {
            listenerList.broadcast(LifeCycleEvent.STARTING);
            doStart();
            listenerList.broadcast(LifeCycleEvent.STARTED);
        } finally {
            startLatch.countDown();
        }
    }
    
    private void doStart() {
        loadBackgroundTasksBlocking();
        
	    LOG.trace("START RouterService");
	    
	    statisticAccumulator.get().start();

//        if(SSLSettings.isIncomingTLSEnabled() || SSLSettings.isOutgoingTLSEnabled()) {
//            LOG.trace("START SSL Test");
//            activityCallback.get().componentLoading(I18nMarker.marktr("Loading TLS Encryption..."));
//            SSLEngineTest sslTester = new SSLEngineTest(SSLUtils.getTLSContext(), SSLUtils.getTLSCipherSuites(), byteBufferCache.get());
//            if(!sslTester.go()) {
//                Throwable t = sslTester.getLastFailureCause();
//                SSLSettings.disableTLS(t);
//                if(!SSLSettings.IGNORE_SSL_EXCEPTIONS.getValue() && !sslTester.isIgnorable(t))
//                    ErrorService.error(t);
//            }
//            LOG.trace("END SSL Test");
//        }
		// Now, link all the pieces together, starting the various threads.            
//        LOG.trace("START ContentManager");
//        activityCallback.get().componentLoading(I18nMarker.marktr("Loading Safe Content Management..."));
//        contentManager.get().start();
//        LOG.trace("STOP ContentManager");

//        LOG.trace("START MessageRouter");
//        activityCallback.get().componentLoading(I18nMarker.marktr("Loading Message Router..."));
//		messageRouter.get().start();
//		LOG.trace("STOPMessageRouter");
		
//        LOG.trace("START UpdateManager.instance");
//        activityCallback.get().componentLoading(I18nMarker.marktr("Checking for Updates..."));
//        updateHandler.get().initialize();
//        LOG.trace("STOP UpdateManager.instance");

//        LOG.trace("START HTTPUploadManager");
//        activityCallback.get().componentLoading(I18nMarker.marktr("Loading Upload Management..."));
//        uploadManager.get().start(); 
//        LOG.trace("STOP HTTPUploadManager");

//        LOG.trace("START HTTPUploadAcceptor");
//        httpUploadAcceptor.get().start(); 
//        connectionDispatcher.get().addConnectionAcceptor(httpUploadAcceptor.get(), false, httpUploadAcceptor.get().getHttpMethods());
//        LOG.trace("STOP HTTPUploadAcceptor");

//        LOG.trace("START Acceptor");
//        activityCallback.get().componentLoading(I18nMarker.marktr("Loading Connection Listener..."));
//		acceptor.get().start();
//		LOG.trace("STOP Acceptor");
		
//        LOG.trace("START loading StaticMessages");
//        staticMessages.get().initialize();
//        LOG.trace("END loading StaticMessages");
        
//		LOG.trace("START ConnectionManager");
//		activityCallback.get().componentLoading(I18nMarker.marktr("Loading Connection Management..."));
//        connectionManager.get().initialize();
//		LOG.trace("STOP ConnectionManager");
		
//		LOG.trace("Running download upgrade task");
//		downloadUpgradeTask.get().upgrade();
//		LOG.trace("Download upgrade task run!");
		
//		LOG.trace("START DownloadManager");
//		activityCallback.get().componentLoading(I18nMarker.marktr("Loading Download Management..."));
//		downloadManager.get().initialize();
//        connectionDispatcher.get().addConnectionAcceptor(pushDownloadManager.get(), false, "GIV");
//		LOG.trace("STOP DownloadManager");
		
//		LOG.trace("START NodeAssigner");
//		activityCallback.get().componentLoading(I18nMarker.marktr("Loading Ultrapeer/DHT Management..."));
//		nodeAssigner.get().start();
//		LOG.trace("STOP NodeAssigner");
		
//        LOG.trace("START HostCatcher.initialize");
//        activityCallback.get().componentLoading(I18nMarker.marktr("Locating Peers..."));
//		hostCatcher.get().initialize();
//		LOG.trace("STOP HostCatcher.initialize");

//		if(ConnectionSettings.CONNECT_ON_STARTUP.getValue()) {
//			// Make sure connections come up ultra-fast (beyond default keepAlive)		
//			int outgoing = ConnectionSettings.NUM_CONNECTIONS.getValue();
//			if ( outgoing > 0 ) {
//			    LOG.trace("START connect");
//				connectionServices.get().connect();
//                LOG.trace("STOP connect");
//            }
//		}
        

//        LOG.trace("START TorrentManager");
//        activityCallback.get().componentLoading(I18nMarker.marktr("Loading BitTorrent Management..."));
//		torrentManager.get().initialize(connectionDispatcher.get());
//		LOG.trace("STOP TorrentManager");
        
        // Restore any downloads in progress.
        LOG.trace("START DownloadManager.postGuiInit");
        activityCallback.get().componentLoading(I18nMarker.marktr("Loading Old Downloads..."));
        downloadManager.get().loadSavedDownloadsAndScheduleWriting();
        LOG.trace("STOP DownloadManager.postGuiInit");
        
//        LOG.trace("START QueryUnicaster");
//        activityCallback.get().componentLoading(I18nMarker.marktr("Loading Directed Querier..."));
//		queryUnicaster.get().start();
//		LOG.trace("STOP QueryUnicaster");
		
//		LOG.trace("START LocalHTTPAcceptor");
//		activityCallback.get().componentLoading(I18nMarker.marktr("Loading Magnet Listener..."));
//        localHttpAcceptor.get().start();
//        localConnectionDispatcher.get().addConnectionAcceptor(localHttpAcceptor.get(), true, localHttpAcceptor.get().getHttpMethods());
//        LOG.trace("STOP LocalHTTPAcceptor");

//        LOG.trace("START LocalAcceptor");
//        initializeLocalConnectionDispatcher();
//        localAcceptor.get().start();
//        LOG.trace("STOP LocalAcceptor");
        
//        LOG.trace("START Pinger");
//        activityCallback.get().componentLoading(I18nMarker.marktr("Loading Peer Listener..."));
//        pinger.get().start();
//        LOG.trace("STOP Pinger");
        
//        LOG.trace("START ConnectionWatchdog");
//        activityCallback.get().componentLoading(I18nMarker.marktr("Loading Stale Connection Management..."));
//        connectionWatchdog.get().start();
//        LOG.trace("STOP ConnectionWatchdog");
        
//        LOG.trace("START SavedFileManager");
//        activityCallback.get().componentLoading(I18nMarker.marktr("Loading Saved Files..."));
//        savedFileManager.get();
//        LOG.trace("STOP SavedFileManager");
		
//		LOG.trace("START loading spam data");
//		activityCallback.get().componentLoading(I18nMarker.marktr("Loading Spam Management..."));
//		ratingTable.get();
//		LOG.trace("START loading spam data");
        
//        LOG.trace("START register connection dispatchers");
//        activityCallback.get().componentLoading(I18nMarker.marktr("Loading Network Listeners..."));
//        initializeConnectionDispatcher();
//        LOG.trace("STOP register connection dispatchers");
        
//        LOG.trace("START Random Initializings");
//        outOfBandThroughputMeasurer.get().initialize();
//        browseHostHandlerManager.get().initialize();
//        LOG.trace("STOP Random Initializings");

        //serviceRegistry.start();

        if(ApplicationSettings.AUTOMATIC_MANUAL_GC.getValue())
            startManualGCThread();
        
        LOG.trace("STOP RouterService.");
        startDone.set(true);
        startFinishedTime = System.currentTimeMillis();
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.LifecycleManager#shutdown()
     */
    public void shutdown() {
        try {
            listenerList.broadcast(LifeCycleEvent.SHUTINGDOWN);
            doShutdown();
            listenerList.broadcast(LifeCycleEvent.SHUTDOWN);
        } catch(Throwable t) {
            ErrorService.error(t);
        }
    }
    
    private void doShutdown() {
        if(!startBegin.get() || shutdownBegin.getAndSet(true))
            return;
        
        try {
            // TODO: should we have a time limit on how long we wait?
            startLatch.await(); // wait for starting to finish...
        } catch(InterruptedException ie) {
            LOG.error("Interrupted while waiting to finish starting", ie);
            return;
        }
        
        serviceRegistry.stop();
        
        nodeAssigner.get().stop();

//        try {
//            acceptor.get().setListeningPort(0);
//        } catch (IOException e) {
//            LOG.error("Error stopping acceptor", e);
//        }
//        acceptor.get().shutdown();
        
        //clean-up connections and record connection uptime for this session
        //connectionManager.get().disconnect(false);
        
        //Update fractional uptime statistics (before writing frostwire.props)
        statistics.get().shutdown();
        
		// start closing all active torrents
		//torrentManager.shutdown();
		
        //Update firewalled status
        ConnectionSettings.EVER_ACCEPTED_INCOMING.setValue(networkManager.get().acceptedIncomingConnection());

//        //Write gnutella.net
//        try {
//            hostCatcher.get().write();
//        } catch (IOException e) {
//            LOG.error("Error saving host catcher file", e);   
//        }
        
        // save frostwire.props & other settings
        SettingsGroupManager.instance().save();
		
		ratingTable.get().ageAndSave();
        
        cleanupPreviewFiles();
        
        //cleanupTorrentMetadataFiles();
        
        downloadManager.get().writeSnapshot();
        
        licenseFactory.get().persistCache();
        
        contentManager.get().stop();
        
        messageRouter.get().stop();
        
        //localAcceptor.get().stop();
        
        statisticAccumulator.get().stop();
        
        //TODO IMPORTANT: Bring this back
        //runShutdownItems();
        
        AzureusStarter.getAzureusCore().stop();
        
        shutdownDone.set(true);
    }

    
    private static String parseCommand(String toCall) {
        if (toCall.startsWith("\"")) {
            int end;
            if ((end = toCall.indexOf("\"", 1)) > -1) {
                return toCall.substring(0,end+1);
            }
            else {
                return toCall+"\"";
            }
        }
        int space;
        if ((space = toCall.indexOf(" ")) > -1) {
            return toCall.substring(0, space);
        }
        
        return toCall;
    }
    
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.LifecycleManager#shutdown(java.lang.String)
     */
    public void shutdown(String toExecute) {
        shutdown();
        if (toExecute != null) {
            try {
                if (OSUtils.isWindowsVista()) {
                    String cmd = parseCommand(toExecute).trim();
                    String params = toExecute.substring(cmd.length()).trim();
                    SystemUtils.openFile(cmd, params);
                }
                else {
                    Runtime.getRuntime().exec(toExecute);
                }
            } catch (IOException tooBad) {}
        }
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.LifecycleManager#getStartFinishedTime()
     */
    public long getStartFinishedTime() {
        return startFinishedTime;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.LifecycleManager#addShutdownItem(java.lang.Thread)
     */
    public boolean addShutdownItem(Thread t) {
        if(shutdownBegin.get())
            return false;
    
        SHUTDOWN_ITEMS.add(t);
        return true;
    }

    /** Runs all shutdown items. */
//    private void runShutdownItems() {
//        if(!shutdownBegin.get())
//            return;
//        
//        // Start each shutdown item.
//        for(Thread t : SHUTDOWN_ITEMS)
//            t.start();
//        
//        // Now that we started them all, iterate back and wait for each one to finish.
//        for(Thread t : SHUTDOWN_ITEMS) {
//            try {
//                t.join();
//            } catch(InterruptedException ie) {}
//        }
//    }
    
    /** Runs all tasks that can be done in the background while the gui inits. */
    private void doBackgroundTasks() {
        serviceRegistry.start("SuperEarly");
        serviceRegistry.start("EarlyBackground");
        
        limeCoreGlue.get().install(); // ensure glue is set before running tasks.
        
        //acceptor.get().init();
        backgroundDone.set(true);
    }

    /** Gets the current state of the lifecycle. */
    private State getCurrentState() {
        if(shutdownBegin.get())
            return State.STOPPED;
        else if(startDone.get())
            return State.STARTED;
        else if(startBegin.get())
            return State.STARTING;
        else
            return State.NONE;
    }

    /** Starts a manual GC thread. */
    private void startManualGCThread() {
        Thread t = ThreadExecutor.newManagedThread(new Runnable() {
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(5 * 60 * 1000);
                    } catch(InterruptedException ignored) {}
                    LOG.trace("Running GC");
                    System.gc();
                    LOG.trace("GC finished, running finalizers");
                    System.runFinalization();
                    LOG.trace("Finalizers finished.");
                }
            }
        }, "ManualGC");
        t.setDaemon(true);
        t.start();
        LOG.trace("Started manual GC thread.");
    }

    /** Deletes all preview files. */
    private void cleanupPreviewFiles() {
        
    }

    public void addListener(EventListener<LifeCycleEvent> listener) {
        listenerList.addListener(listener);
    }

    public boolean removeListener(EventListener<LifeCycleEvent> listener) {
        return listenerList.removeListener(listener);
    }
    
}