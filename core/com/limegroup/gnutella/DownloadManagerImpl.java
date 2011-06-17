package com.limegroup.gnutella;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.gudy.azureus2.core3.global.GlobalManager;
import org.limewire.collection.DualIterator;
import org.limewire.collection.MultiIterable;

import com.frostwire.AzureusStarter;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.limegroup.gnutella.downloader.CoreDownloader;
import com.limegroup.gnutella.downloader.DownloaderType;
import com.limegroup.gnutella.downloader.InNetworkDownloader;
import com.limegroup.gnutella.downloader.ManagedDownloader;
import com.limegroup.gnutella.messages.BadPacketException;
import com.limegroup.gnutella.messages.QueryReply;
import com.limegroup.gnutella.messages.QueryRequest;
import com.limegroup.gnutella.search.HostData;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.settings.UpdateSettings;
import com.limegroup.gnutella.version.DownloadInformation;

@Singleton
public class DownloadManagerImpl implements DownloadManager {

    /** The list of all ManagedDownloader's attempting to download.
     *  INVARIANT: active.size()<=slots() && active contains no duplicates 
     *  LOCKING: obtain this' monitor */
    private final List <CoreDownloader> active=new LinkedList<CoreDownloader>();
    /** The list of all queued ManagedDownloader. 
     *  INVARIANT: waiting contains no duplicates 
     *  LOCKING: obtain this' monitor */
    
    private final List <CoreDownloader> waiting=new LinkedList<CoreDownloader>();
    
    private final MultiIterable<CoreDownloader> activeAndWaiting = 
        new MultiIterable<CoreDownloader>(active,waiting);
    
    /** The number if IN-NETWORK active downloaders.  We don't count these when
     * determing how many downloaders are active.
     */
    private int innetworkCount = 0;

    /**
     * The number of times we've been bandwidth measures
     */
    private int numMeasures = 0;
    
    /**
     * The average bandwidth over all downloads.
     * This is only counted while downloads are active.
     */
    private float averageBandwidth = 0;
    
    private final NetworkManager networkManager;
    private final Provider<DownloadCallback> downloadCallback;
    private final Provider<MessageRouter> messageRouter;
    
    @Inject
    public DownloadManagerImpl(NetworkManager networkManager,
            Provider<DownloadCallback> downloadCallback,
            Provider<MessageRouter> messageRouter) {
        this.networkManager = networkManager;
        this.downloadCallback = downloadCallback;
        this.messageRouter = messageRouter;
    }


    //////////////////////// Creation and Saving /////////////////////////

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#initialize()
     */
    public void initialize() {
        //scheduleWaitingPump();
    }
    
    private void addDownloaderManager(org.gudy.azureus2.core3.download.DownloadManager downloader) {
        synchronized(this) {
            callback(downloader).addDownloadManager(downloader);
        }
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#postGuiInit()
     */
    public void loadSavedDownloadsAndScheduleWriting() {
        loadTorrentDownloads();
        //loadSavedDownloads();
        //scheduleSnapshots();
    }
    
    /**
     * This is where torrents are loaded from the last session.
     * If seeding is not enaebled, completed torrents won't be started, they'll be stopped.
     */
    private void loadTorrentDownloads() {
        GlobalManager globalManager = AzureusStarter.getAzureusCore().getGlobalManager();
        List<?> downloadManagers = globalManager.getDownloadManagers();
        for (Object obj : downloadManagers) {
            if (obj instanceof org.gudy.azureus2.core3.download.DownloadManager) {

            	org.gudy.azureus2.core3.download.DownloadManager downloadManager = (org.gudy.azureus2.core3.download.DownloadManager) obj;
                
            	if (!SharingSettings.SEED_FINISHED_TORRENTS.getValue()) {
            		if (downloadManager.getAssumedComplete()) {
            		    downloadManager.pause();
            		}
            	}

                addDownloaderManager(downloadManager);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#killDownloadersNotListed(java.util.Collection)
     */
    public synchronized void killDownloadersNotListed(Collection<? extends DownloadInformation> updates) {
        if (updates == null)
            return;
        
        Set<String> urns = new HashSet<String>(updates.size());
        for(DownloadInformation ui : updates)
            urns.add(ui.getUpdateURN().httpStringValue());
        
        for (Iterator<CoreDownloader> iter = new DualIterator<CoreDownloader>(waiting.iterator(),active.iterator());
        iter.hasNext();) {
            CoreDownloader d = iter.next();
            if (d.getDownloadType() == DownloaderType.INNETWORK  && 
                    !urns.contains(d.getSha1Urn().httpStringValue())) 
                d.stop();
        }
        
        Set<String> hopeless = UpdateSettings.FAILED_UPDATES.getValue();
        hopeless.retainAll(urns);
        UpdateSettings.FAILED_UPDATES.setValue(hopeless);
    }

    /**
     * Delegates the incoming socket out to BrowseHostHandler & then attempts to assign it
     * to any ManagedDownloader.
     * 
     * Closes the socket if neither BrowseHostHandler nor any ManagedDownloaders wanted it.
     * 
     * @param file
     * @param index
     * @param clientGUID
     * @param socket
     */
    private synchronized boolean handleIncomingPush(String file, int index, byte [] clientGUID, Socket socket) {
         boolean handled = false;
         for (CoreDownloader md : activeAndWaiting) {
            if (! (md instanceof ManagedDownloader))
                continue; // pushes apply to gnutella downloads only
            ManagedDownloader mmd = (ManagedDownloader)md;
            if (mmd.acceptDownload(file, socket, index, clientGUID))
                handled = true;
         }                 
         return handled;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#acceptPushedSocket(java.lang.String, int, byte[], java.net.Socket)
     */
    public boolean acceptPushedSocket(String file, int index,
            byte[] clientGUID, Socket socket) {
        return handleIncomingPush(file, index, clientGUID, socket);
    }
    
    public boolean allowNewTorrents() {
    	return true;
    	
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#isIncomplete(com.limegroup.gnutella.URN)
     */
    public boolean isIncomplete(URN urn) {
        return false;//incompleteFileManager.getFileForUrn(urn) != null;
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#isActivelyDownloading(com.limegroup.gnutella.URN)
     */
    public boolean isActivelyDownloading(URN urn) {
        Downloader md = getDownloaderForURN(urn);
        
        if(md == null)
            return false;
            
        switch(md.getState()) {
        case QUEUED:
        case BUSY:
        case ABORTED:
        case GAVE_UP:
        case DISK_PROBLEM:
        case CORRUPT_FILE:
        case REMOTE_QUEUED:
        case WAITING_FOR_USER:
            return false;
        default:
            return true;
        }
    }  
 
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#downloadsInProgress()
     */
    public synchronized int downloadsInProgress() {
        return active.size() + waiting.size();
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#getNumIndividualDownloaders()
     */
    public synchronized int getNumIndividualDownloaders() {
        int ret = 0;
        for (Iterator<CoreDownloader> iter=active.iterator(); iter.hasNext(); ) {  //active
            Object next = iter.next();
            if (! (next instanceof ManagedDownloader))
                continue; // TODO: count torrents separately
            ManagedDownloader md=(ManagedDownloader)next;
            ret += md.getNumDownloaders();
       }
       return ret;
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#getNumActiveDownloads()
     */
    public synchronized int getNumActiveDownloads() {
        return active.size() - innetworkCount;
    }
   
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#getNum<Downloads()
     */
    public synchronized int getNumWaitingDownloads() {
        return waiting.size();
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#getDownloaderForURN(com.limegroup.gnutella.URN)
     */
    public synchronized Downloader getDownloaderForURN(URN sha1) {
        for (CoreDownloader md : activeAndWaiting) {
            if (md.getSha1Urn() != null && sha1.equals(md.getSha1Urn()))
                return md;
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#getDownloaderForURNString(java.lang.String)
     */
    public synchronized Downloader getDownloaderForURNString(String urn) {
        for (CoreDownloader md : activeAndWaiting) {
            if (md.getSha1Urn() != null && urn.equals(md.getSha1Urn().toString()))
                return md;
        }
        return null;
    }    
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#getDownloaderForIncompleteFile(java.io.File)
     */
    public synchronized Downloader getDownloaderForIncompleteFile(File file) {
        for (CoreDownloader dl : activeAndWaiting) {
            if (dl.conflictsWithIncompleteFile(file)) {
                return dl;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#isGuidForQueryDownloading(com.limegroup.gnutella.GUID)
     */
    public synchronized boolean isGuidForQueryDownloading(GUID guid) {
        for (CoreDownloader md : activeAndWaiting) {
            GUID dGUID = md.getQueryGUID();
            if ((dGUID != null) && (dGUID.equals(guid)))
                return true;
        }
        return false;
    }
    
    void clearAllDownloads() {
        List<Downloader> buf;
        synchronized(this) {
            buf = new ArrayList<Downloader>(active.size() + waiting.size());
            buf.addAll(active);
            buf.addAll(waiting);
            active.clear();
            waiting.clear();
        }
        for(Downloader md : buf ) 
            md.stop();
    }
    
    private DownloadCallback callback(org.gudy.azureus2.core3.download.DownloadManager dm) {
        return downloadCallback.get();
    }
        
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#conflicts(com.limegroup.gnutella.URN, long, java.io.File)
     */
    public boolean conflicts(URN urn, long fileSize, File... fileName) {
        
        if (urn == null && fileSize == 0) {
            return false;
        }
        
        synchronized (this) {
            for (CoreDownloader md : activeAndWaiting) {
                if (md.conflicts(urn, fileSize, fileName)) 
                    return true;
            }
            return false;
        }
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#isSaveLocationTaken(java.io.File)
     */
    public synchronized boolean isSaveLocationTaken(File candidateFile) {
        for (CoreDownloader md : activeAndWaiting) {
            if (md.conflictsSaveFile(candidateFile)) 
                return true;
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#handleQueryReply(com.limegroup.gnutella.messages.QueryReply)
     */
    public void handleQueryReply(QueryReply qr) {
        // first check if the qr is of 'sufficient quality', if not just
        // short-circuit.
        if (qr.calculateQualityOfService(
                !networkManager.acceptedIncomingConnection(), networkManager) < 1)
            return;

        List<Response> responses;
        HostData data;
        try {
            responses = qr.getResultsAsList();
            data = qr.getHostData();
        } catch(BadPacketException bpe) {
            return; // bad packet, do nothing.
        }
        
        addDownloadWithResponses(responses, data);
    }

    /**
     * Iterates through all responses seeing if they can be matched
     * up to any existing downloaders, adding them as possible
     * sources if they do.
     */
    private void addDownloadWithResponses(List<? extends Response> responses, HostData data) {
//        if(responses == null)
//            throw new NullPointerException("null responses");
//        if(data == null)
//            throw new NullPointerException("null hostdata");
//
//        // need to synch because active and waiting are not thread safe
//        List<CoreDownloader> downloaders = new ArrayList<CoreDownloader>(active.size() + waiting.size());
//        synchronized (this) { 
//            // add to all downloaders, even if they are waiting....
//            downloaders.addAll(active);
//            downloaders.addAll(waiting);
//        }
//        
//        // short-circuit.
//        if(downloaders.isEmpty())
//            return;
//
//        //For each response i, offer it to each downloader j.  Give a response
//        // to at most one downloader.
//        // TODO: it's possible that downloader x could accept response[i] but
//        //that would cause a conflict with downloader y.  Check for this.
//        for(Response r : responses) {
//            // Don't bother with making XML from the EQHD.
//            RemoteFileDesc rfd = r.toRemoteFileDesc(data, remoteFileDescFactory);
//            for(Downloader current : downloaders) {
//                if ( !(current instanceof ManagedDownloader))
//                    continue; // can't add sources to torrents yet
//                ManagedDownloader currD = (ManagedDownloader) current;
//                // If we were able to add this specific rfd,
//                // add any alternates that this response might have
//                // also.
//                if (currD.addDownload(rfd, true)) {
//                    for(IpPort ipp : r.getLocations()) {
//                        // don't cache alts.
//                        currD.addDownload(remoteFileDescFactory.createRemoteFileDesc(rfd, ipp), false);
//                    }
//                    break;
//                }
//            }
//        }
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#remove(com.limegroup.gnutella.downloader.CoreDownloader, boolean)
     */
    public synchronized void remove(CoreDownloader downloader, 
                                    boolean completed) {
//        active.remove(downloader);
//        if(downloader.getDownloadType() == DownloaderType.INNETWORK)
//            innetworkCount--;
//        
//        waiting.remove(downloader);
//        if(completed)
//            cleanupCompletedDownload(downloader, true);
//        else
//            waiting.add(downloader);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#bumpPriority(com.limegroup.gnutella.Downloader, boolean, int)
     */
    public synchronized void bumpPriority(Downloader downl,
                                          boolean up, int amt) {
        CoreDownloader downloader = (CoreDownloader)downl;
        int idx = waiting.indexOf(downloader);
        if(idx == -1)
            return;

        if(up && idx != 0) {
            waiting.remove(idx);
            if (amt > idx)
                amt = idx;
            if (amt != 0)
                waiting.add(idx - amt, downloader);
            else
                waiting.add(0, downloader);     //move to top of list
        } else if(!up && idx != waiting.size() - 1) {
            waiting.remove(idx);
            if (amt != 0) {
                amt += idx;
                if (amt > waiting.size())
                    amt = waiting.size();
                waiting.add(amt, downloader);
            } else {
                waiting.add(downloader);    //move to bottom of list
            }
        }
    }

    /**
     * Cleans up the given Downloader after completion.
     *
     * If ser is true, also writes a snapshot to the disk.
     */
//    private void cleanupCompletedDownload(CoreDownloader dl, boolean ser) {
//        dl.finish();
//        if (dl.getQueryGUID() != null)
//            messageRouter.get().downloadFinished(dl.getQueryGUID());
//        callback(dl).removeDownload(dl);
//        
//        //Save this' state to disk for crash recovery.
//        if(ser)
//            writeSnapshot();
//
//        // Enable auto shutdown
//        if(active.isEmpty() && waiting.isEmpty())
//            callback(dl).downloadsComplete();
//    }           
//    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#sendQuery(com.limegroup.gnutella.downloader.ManagedDownloader, com.limegroup.gnutella.messages.QueryRequest)
     */
    public void sendQuery(QueryRequest query) {
        messageRouter.get().sendDynamicQuery(query);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#measureBandwidth()
     */
    public void measureBandwidth() {
        List<CoreDownloader> activeCopy;
        synchronized(this) {
            activeCopy = new ArrayList<CoreDownloader>(active);
        }
        
        float currentTotal = 0f;
        boolean c = false;
        for (BandwidthTracker bt : activeCopy) {
            if (bt instanceof InNetworkDownloader)
                continue;
            
            c = true;
            bt.measureBandwidth();
            currentTotal += bt.getAverageBandwidth();
        }
        
        if ( c ) {
            synchronized(this) {
                averageBandwidth = ( (averageBandwidth * numMeasures) + currentTotal ) 
                    / ++numMeasures;
            }
        }
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#getMeasuredBandwidth()
     */
    public float getMeasuredBandwidth() {
        List<CoreDownloader> activeCopy;
        synchronized(this) {
            activeCopy = new ArrayList<CoreDownloader>(active);
        }
        
        float sum=0;
        for (BandwidthTracker bt : activeCopy) {
            if (bt instanceof InNetworkDownloader)
                continue;
            
            float curr = 0;
            try{
                curr = bt.getMeasuredBandwidth();
            } catch(InsufficientDataException ide) {
                curr = 0;//insufficient data? assume 0
            }
            sum+=curr;
        }
                
        return sum;
    }
    
    /* (non-Javadoc)
     * @see com.limegroup.gnutella.DownloadMI#getAverageBandwidth()
     */
    public synchronized float getAverageBandwidth() {
        return averageBandwidth;
    }
}
