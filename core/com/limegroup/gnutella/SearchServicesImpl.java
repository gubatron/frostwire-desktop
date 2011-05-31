package com.limegroup.gnutella;

import java.util.Map.Entry;
import java.util.Set;

import org.limewire.concurrent.ThreadExecutor;
import org.limewire.io.Connectable;
import org.limewire.io.IpPort;
import org.limewire.service.ErrorService;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.limegroup.gnutella.filters.MutableGUIDFilter;
import com.limegroup.gnutella.messages.QueryRequest;
import com.limegroup.gnutella.messages.QueryRequestFactory;
import com.limegroup.gnutella.search.HostData;
import com.limegroup.gnutella.search.QueryDispatcher;
import com.limegroup.gnutella.search.SearchResultHandler;
import com.limegroup.gnutella.settings.FilterSettings;
import com.limegroup.gnutella.settings.MessageSettings;
import com.limegroup.gnutella.statistics.OutOfBandStatistics;
import com.limegroup.gnutella.statistics.QueryStats;

@Singleton
public class SearchServicesImpl implements SearchServices {
    
    private final Provider<ResponseVerifier> responseVerifier;
    private final Provider<QueryUnicaster> queryUnicaster;
    private final Provider<SearchResultHandler> searchResultHandler;
    private final Provider<MessageRouter> messageRouter;
    private final Provider<QueryDispatcher> queryDispatcher;
    private final Provider<MutableGUIDFilter> mutableGUIDFilter;
    private final Provider<QueryStats> queryStats; 
    private final Provider<NetworkManager> networkManager;
    private final Provider<QueryRequestFactory> queryRequestFactory;
    private final OutOfBandStatistics outOfBandStatistics;
    
    @Inject
    public SearchServicesImpl(Provider<ResponseVerifier> responseVerifier,
            Provider<QueryUnicaster> queryUnicaster,
            Provider<SearchResultHandler> searchResultHandler,
            Provider<MessageRouter> messageRouter,
            Provider<QueryDispatcher> queryDispatcher,
            Provider<MutableGUIDFilter> mutableGUIDFilter,
            Provider<QueryStats> queryStats,
            Provider<NetworkManager> networkManager,
            Provider<QueryRequestFactory> queryRequestFactory,
            OutOfBandStatistics outOfBandStatistics) {
        this.responseVerifier = responseVerifier;
        this.queryUnicaster = queryUnicaster;
        this.searchResultHandler = searchResultHandler;
        this.messageRouter = messageRouter;
        this.queryDispatcher = queryDispatcher;
        this.mutableGUIDFilter = mutableGUIDFilter;
        this.queryStats = queryStats;
        this.networkManager = networkManager;
        this.queryRequestFactory = queryRequestFactory;
        this.outOfBandStatistics = outOfBandStatistics;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.SearchServices#isMandragoreWorm(byte[], com.limegroup.gnutella.Response)
     */
    public boolean isMandragoreWorm(byte[] guid, Response response) {
        return responseVerifier.get().isMandragoreWorm(guid, response);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.SearchServices#matchesQuery(byte[], com.limegroup.gnutella.Response)
     */
    public boolean matchesQuery(byte [] guid, Response response) {
        return responseVerifier.get().matchesQuery(guid, response);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.SearchServices#matchesType(byte[], com.limegroup.gnutella.Response)
     */
    public boolean matchesType(byte[] guid, Response response) {
        return responseVerifier.get().matchesType(guid, response);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.SearchServices#stopQuery(com.limegroup.gnutella.GUID)
     */
    public void stopQuery(GUID guid) {
//        queryUnicaster.get().purgeQuery(guid);
//        searchResultHandler.get().removeQuery(guid);
//        messageRouter.get().queryKilled(guid);
//        if(connectionServices.get().isSupernode())
//            queryDispatcher.get().addToRemove(guid);
//        mutableGUIDFilter.get().removeGUID(guid.bytes());
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.SearchServices#getLastQueryTime()
     */
    public long getLastQueryTime() {
    	return queryStats.get().getLastQueryTime();
    }

    /** Just aggregates some common code in query() and queryWhatIsNew().
     */ 
    private void recordAndSendQuery(final QueryRequest qr, 
                                           final MediaType type) {
        queryStats.get().recordQuery();
        responseVerifier.get().record(qr, type);
        searchResultHandler.get().addQuery(qr); // so we can leaf guide....
        messageRouter.get().sendDynamicQuery(qr);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.SearchServices#queryWhatIsNew(byte[], com.limegroup.gnutella.MediaType)
     */
    public void queryWhatIsNew(final byte[] guid, final MediaType type) {
    	try {
            QueryRequest qr = null;
            if (GUID.addressesMatch(guid, networkManager.get().getAddress(), networkManager.get().getPort())) {
                // if the guid is encoded with my address, mark it as needing out
                // of band support.  note that there is a VERY small chance that
                // the guid will be address encoded but not meant for out of band
                // delivery of results.  bad things may happen in this case but 
                // it seems tremendously unlikely, even over the course of a 
                // VERY long lived client
                qr = queryRequestFactory.get().createWhatIsNewOOBQuery(guid, (byte)2, type);
                outOfBandStatistics.addSentQuery();
            }
            else
                qr = queryRequestFactory.get().createWhatIsNewQuery(guid, (byte)2, type);
    
            if(FilterSettings.FILTER_WHATS_NEW_ADULT.getValue())
                mutableGUIDFilter.get().addGUID(guid);
    
            recordAndSendQuery(qr, type);
    	} catch(Throwable t) {
    		ErrorService.error(t);
    	}
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.SearchServices#query(byte[], java.lang.String, java.lang.String, com.limegroup.gnutella.MediaType)
     */
    public void query(final byte[] guid, 
    						 final String query, 
    						 final String richQuery, 
    						 final MediaType type) {
    
    	try {
            QueryRequest qr = null;
            
            if (networkManager.get().isIpPortValid() && (new GUID(guid)).addressesMatch(networkManager.get().getAddress(), 
                    networkManager.get().getPort())) {
                // if the guid is encoded with my address, mark it as needing out
                // of band support.  note that there is a VERY small chance that
                // the guid will be address encoded but not meant for out of band
                // delivery of results.  bad things may happen in this case but 
                // it seems tremendously unlikely, even over the course of a 
                // VERY long lived client
                qr = queryRequestFactory.get().createOutOfBandQuery(guid, query, richQuery, type);
                outOfBandStatistics.addSentQuery();
            }
            else
                qr = queryRequestFactory.get().createQuery(guid, query, richQuery, type);
            
            recordAndSendQuery(qr, type);
    	} catch(Throwable t) {
    		ErrorService.error(t);
    	}
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.SearchServices#query(byte[], java.lang.String)
     */
    public void query(byte[] guid, String query) {
        query(guid, query, null);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.SearchServices#query(byte[], java.lang.String, com.limegroup.gnutella.MediaType)
     */
    public void query(byte[] guid, String query, MediaType type) {
    	query(guid, query, "", type);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.SearchServices#newQueryGUID()
     */
    public byte[] newQueryGUID() {
        byte []ret;
        if (networkManager.get().isOOBCapable() && outOfBandStatistics.isOOBEffectiveForMe())
            ret = GUID.makeAddressEncodedGuid(networkManager.get().getAddress(), networkManager.get().getPort());
        else
            ret = GUID.makeGuid();
        if (MessageSettings.STAMP_QUERIES.getValue())
            GUID.timeStampGuid(ret);
        return ret;
    }

	@Override
	public boolean isFloodQueryReply(HostData data, Response response) {
		
		if (response == null || response.getDocument() == null) {
			return false;
		}

		//several 'pleasers' forgot to set creation time. filters a lot
		if (response.getCreateTime() == -1) {
			return true;
		}
		
		Set<Entry<String, String>> nameValueSet = response.getDocument().getNameValueSet();

		if (nameValueSet == null) {
			return false;
		}
		
		for (Entry<String,String> nameValue : nameValueSet) {
			String key = nameValue.getKey();
			String value = nameValue.getValue();
			//System.out.println(key + "=" + value);
			
			if (key.equals("audios__audio__album__") &&
				(value.equals("LIMEWIRE COURT SETTLEMENT, LIMEWIRE USERS GET A FREE APPLE IPHONE 4/IPAD AT WWW.LIMEWIRELAW.COM") ||
				value.contains("LIMEWIRE COURT") ||
				value.contains("APPLE") || value.contains("IPHONE") || value.contains("IPAD") || 
				value.contains("LIMEWIRELAW.COM"))) {
				return true;
			}
		}
		
		long size = response.getSize();
		
		if (size == 2915581 || 
			size == 2470272 || 
			size == 2928329 ||
			size == 3416192 ||
			size == 241272) {
			return true;	
		}
		
		return false;
	}
}
