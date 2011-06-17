package com.limegroup.gnutella;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.concurrent.ThreadExecutor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.limegroup.gnutella.messages.PingReply;
import com.limegroup.gnutella.messages.QueryReply;
import com.limegroup.gnutella.messages.QueryRequest;

/** 
 * This class runs a single thread which sends unicast UDP queries to a master
 * list of unicast-enabled hosts every n milliseconds.  It interacts with
 * HostCatcher to find unicast-enabled hosts.  It also allows for stopping of
 * individual queries by reply counts.
 */ 
@Singleton
public final class QueryUnicaster {
    
    private static final Log LOG = LogFactory.getLog(QueryUnicaster.class);

    /** The time in between successive unicast queries.
     */
    public static final int ITERATION_TIME = 100; // 1/10th of a second...

    /** The number of Endpoints where you should start sending pings to them.
     */
    public static final int MIN_ENDPOINTS = 25;

    /** The max number of unicast pongs to store.
     */
    //public static final int MAX_ENDPOINTS = 2000;
    public static final int MAX_ENDPOINTS = 30;

    /** One hour in milliseconds.
     */
    public static final long ONE_HOUR = 1000 * 60 * 60; // 60 minutes

    /** Actually sends any QRs via unicast UDP messages.
     */
    private final Thread _querier;

    /** 
     * The map of Queries I need to send every iteration.
     * The map is from GUID to QueryBundle.  The following invariant is
     * maintained:
     * GUID -> QueryBundle where GUID == QueryBundle._qr.getGUID()
     */
    private final Map<GUID, QueryBundle> _queries;

    /**
     * Maps leaf connections to the queries they've spawned.
     * The map is from ReplyHandler to a Set (of GUIDs).
     */
    private final Map<ReplyHandler, Set<GUID>> _querySets;

    /** A List of query GUIDS to purge.
     */
    private final List<GUID> _qGuidsToRemove;

	@Inject
    public QueryUnicaster() {
        _queries = new Hashtable<GUID, QueryBundle>();
        _querySets = new Hashtable<ReplyHandler, Set<GUID>>();
        _qGuidsToRemove = new Vector<GUID>();
    
        // start service...
        _querier = ThreadExecutor.newManagedThread(new Runnable() {
            public void run() {
                queryLoop();
            }
        });
        
        _querier.setName("QueryUnicaster");
        _querier.setDaemon(true);
    }
	
    /** Returns the number of Queries unicasted by this guy...
     */
    int getQueryNumber() {
        return _queries.size();
    }

    
    /**
     * Starts the query unicaster thread.
     */
    public synchronized void start() {
//        if (!_initialized) {
//            _querier.start();
//            
//            QueryKeyExpirer expirer = new QueryKeyExpirer();
//            backgroundExecutor.scheduleWithFixedDelay(expirer, 0, 3 * ONE_HOUR, TimeUnit.MILLISECONDS);// every 3 hours
//
//            _initialized = true;
//        }
    }

    /** 
     * The main work to be done.
     * If there are queries, get a unicast enabled UP, and send each Query to
     * it.  Then sleep and try some more later...
     */
    private void queryLoop() {
//        while (true) {
//            try {
//                waitForQueries();
//                GUESSEndpoint toQuery = getUnicastHost();
//                // no query key to use in my query!
//                if (!_queryKeys.containsKey(toQuery)) {
//                    // send a AddressSecurityToken Request
//                    PingRequest pr = pingRequestFactory.createQueryKeyRequest();
//                    udpService.get().send(pr,toQuery.getInetAddress(), toQuery.getPort());
//                    // DO NOT RE-ADD ENDPOINT - we'll do that if we get a
//                    // AddressSecurityToken Reply!!
//                    continue; // try another up above....
//                }
//                AddressSecurityToken addressSecurityToken = _queryKeys.get(toQuery)._queryKey;
//
//                purgeGuidsInternal(); // in case any were added while asleep
//				boolean currentHostUsed = false;
//                synchronized (_queries) {
//                    for(QueryBundle currQB : _queries.values()) {
//                        if (currQB._hostsQueried.size() > QueryBundle.MAX_QUERIES)
//                            // query is now stale....
//                            _qGuidsToRemove.add(new GUID(currQB._qr.getGUID()));
//                        else if (currQB._hostsQueried.contains(toQuery))
//                            ; // don't send another....
//                        else {
//							InetAddress ip = toQuery.getInetAddress();
//							QueryRequest qrToSend = 
//							    queryRequestFactory.createQueryKeyQuery(currQB._qr, 
//																 addressSecurityToken);
//                            udpService.get().send(qrToSend, 
//                                            ip, toQuery.getPort());
//							currentHostUsed = true;
//							currQB._hostsQueried.add(toQuery);
//                        }
//                    }
//                }
//
//				// add the current host back to the list if it was not used for 
//				// any query
//				if(!currentHostUsed) {
//					addUnicastEndpoint(toQuery);
//				}
//                
//                // purge stale queries, hold lock so you don't miss any...
//                synchronized (_qGuidsToRemove) {
//                    purgeGuidsInternal();
//                    _qGuidsToRemove.clear();
//                }
//
//                Thread.sleep(ITERATION_TIME);
//            }
//            catch (InterruptedException ignored) {}
//        }
    }

 
    /** 
     * A quick purging of query GUIDS from the _queries Map.  The
     * queryLoop uses this to so it doesn't have to hold the _queries
     * lock for too long.
     */
    private void purgeGuidsInternal() {
        synchronized (_qGuidsToRemove) {
            for(GUID currGuid : _qGuidsToRemove)
                _queries.remove(currGuid);
        }
    }

    /** 
     * @return true if the query was added (maybe false if it existed).
     * @param query The Query to add, to start unicasting.
     * @param reference The originating connection.  OK if NULL.
     */
    public boolean addQuery(QueryRequest query, ReplyHandler reference) {
        LOG.debug("QueryUnicaster.addQuery(): entered.");
        boolean retBool = false;
        GUID guid = new GUID(query.getGUID());
        // first map the QueryBundle using the guid....
        synchronized (_queries) {
            if (!_queries.containsKey(guid)) {
                QueryBundle qb = new QueryBundle(query);
                _queries.put(guid, qb);
                retBool = true;
            }
            if (retBool) {
                _queries.notifyAll();
			}
        }

		// return if this node originated the query
        if (reference == null)
            return retBool;

        // then record the guid in the set of leaf's queries...
        synchronized (_querySets) {
            Set<GUID> guids = _querySets.get(reference);
            if (guids == null) {
                guids = new HashSet<GUID>();
                _querySets.put(reference, guids);
            }
            guids.add(guid);
        }
        if(LOG.isDebugEnabled())
            LOG.debug("QueryUnicaster.addQuery(): returning " + retBool);
        return retBool;
    }

    /** Just feed me ExtendedEndpoints - I'll check if I could use them or not.
     */
    public void addUnicastEndpoint(InetAddress address, int port) {
//        if (!SearchSettings.GUESS_ENABLED.getValue()) return;
//        if (notMe(address, port) && NetworkUtils.isValidPort(port) &&
//          NetworkUtils.isValidAddress(address)) {
//			GUESSEndpoint endpoint = new GUESSEndpoint(address, port);
//			addUnicastEndpoint(endpoint);
//        }
    }
    
    /** 
     * Gets rid of a Query according to ReplyHandler.  
     * Use this if a leaf connection dies and you want to stop the query.
     */
    void purgeQuery(ReplyHandler reference) {
        LOG.debug("QueryUnicaster.purgeQuery(RH): entered.");
        if (reference == null)
            return;
        synchronized (_querySets) {
            Set<GUID> guids = _querySets.remove(reference);
            if (guids == null)
                return;
            for(GUID guid : guids)
                purgeQuery(guid);
        }
        LOG.debug("QueryUnicaster.purgeQuery(RH): returning.");
    }

    /** 
     * Gets rid of a Query according to GUID.  Use this if a leaf connection
     * dies and you want to stop the query.
     */
    void purgeQuery(GUID queryGUID) {
        LOG.debug("QueryUnicaster.purgeQuery(GUID): entered.");
        _qGuidsToRemove.add(queryGUID);
        LOG.debug("QueryUnicaster.purgeQuery(GUID): returning.");
    }


    /** Feed me QRs so I can keep track of stuff.
     */
    public void handleQueryReply(QueryReply qr) {
        addResults(new GUID(qr.getGUID()), qr.getResultCount());
    }


    /** Feed me AddressSecurityToken pongs so I can query people....
     *  pre: pr.getQueryKey() != null
     */
    public void handleQueryKeyPong(PingReply pr) {
//        if(pr == null) {
//            throw new NullPointerException("null pong");
//        }
//        AddressSecurityToken qk = pr.getQueryKey();
//        if(qk == null)
//            throw new IllegalArgumentException("no key in pong");
//        
//        InetAddress address = pr.getInetAddress();
//
//        int port = pr.getPort();
//        GUESSEndpoint endpoint = new GUESSEndpoint(address, port);
//        _queryKeys.put(endpoint, new QueryKeyBundle(qk));
//        addUnicastEndpoint(endpoint);
    }


    /** 
     * Add results to a query so we can invalidate it when enough results are
     * received.
     */
    private void addResults(GUID queryGUID, int numResultsToAdd) {
        synchronized (_queries) {
            QueryBundle qb = _queries.get(queryGUID);
            if (qb != null) {// add results if possible...
                qb._numResults += numResultsToAdd;
                
                //  This code moved from queryLoop() since that ftn. blocks before
                //      removing stale queries, when out of hosts to query.
                if( qb._numResults>QueryBundle.MAX_RESULTS ) {
                    synchronized( _qGuidsToRemove ) {
                        _qGuidsToRemove.add(new GUID(qb._qr.getGUID()));
                        purgeGuidsInternal();
                        _qGuidsToRemove.clear();                        
                    }
                }

            }
            
        }
    }
    
    /** removes all Unicast Endpoints, reset associated members
     */
    void resetUnicastEndpointsAndQueries() {
//        LOG.debug("Resetting unicast endpoints.");        
//        synchronized (_queries) {
//            _queries.clear();
//            _queries.notifyAll();
//        }
//
//        synchronized (_queryHosts) {
//            _queryHosts.clear();
//            _queryHosts.notifyAll();
//        }
//        synchronized (_pingList) {
//            _pingList.clear();
//            _pingList.notifyAll();
//        }
//
//        //_lastPingTime=0;        
//        _testUDPPingsSent=0;
//        
    }


    private static class QueryBundle {
        public static final int MAX_RESULTS = 250;
        //public static final int MAX_QUERIES = 1000;
        final QueryRequest _qr;
        // the number of results received per Query...
        int _numResults = 0;
        /** The Set of Endpoints queried for this Query.
         */
        //final Set<GUESSEndpoint> _hostsQueried = new HashSet<GUESSEndpoint>();

        public QueryBundle(QueryRequest qr) {
            _qr = qr;
        }
		
		// overrides toString to provide more information
		public String toString() {
			return "QueryBundle: "+_qr;
		}
    }

    
}
