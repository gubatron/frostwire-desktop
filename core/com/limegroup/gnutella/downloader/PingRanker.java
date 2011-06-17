package com.limegroup.gnutella.downloader;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.collection.Cancellable;
import org.limewire.io.IpPort;

import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.MessageListener;
import com.limegroup.gnutella.MessageRouter;
import com.limegroup.gnutella.NetworkManager;
import com.limegroup.gnutella.RemoteFileDesc;
import com.limegroup.gnutella.ReplyHandler;
import com.limegroup.gnutella.UDPPinger;
import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.messages.Message;
import com.limegroup.gnutella.messages.vendor.HeadPing;
import com.limegroup.gnutella.settings.DownloadSettings;

public class PingRanker extends AbstractSourceRanker implements MessageListener, Cancellable {

    private static final Log LOG = LogFactory.getLog(PingRanker.class);
    
    private static final Comparator<RemoteFileDesc> RFD_COMPARATOR = new RFDComparator();    
    private static final Comparator<RemoteFileDesc> ALT_DEPRIORITIZER = new RFDAltDeprioritizer();
    
    
    /**
     * new hosts (as RFDs) that we've learned about
     */
    private Set<RemoteFileDesc> newHosts;
    
    /**
     * Mapping IpPort -> RFD to which we have sent pings.
     * Whenever we send pings to push proxies, each proxy points to the same
     * RFD.  Used to check whether we receive a pong from someone we have sent
     * a ping to.
     */
    private TreeMap<IpPort, RemoteFileDesc> pingedHosts;
    
    /**
     * A set containing the unique remote file locations that we have pinged.  It
     * differs from pingedHosts because it contains only RemoteFileDesc objects 
     */
    private Set<RemoteFileDesc> testedLocations;
    
    /**
     * RFDs that have responded to our pings.
     */
    private TreeSet<RemoteFileDesc> verifiedHosts;
    
    /**
     * The urn to use to create pings
     */
    private URN sha1;
    
    /**
     * The guid to use for my headPings
     */
    private GUID myGUID;
    
    /**
     * whether the ranker has been stopped.
     */
    private boolean running;
    
    /**
     * The last time we sent a bunch of hosts for pinging.
     */
    private long lastPingTime;
    
    private final NetworkManager networkManager;
    private final UDPPinger udpPinger;

    private final MessageRouter messageRouter;
    
    protected PingRanker(NetworkManager networkManager, UDPPinger udpPinger, MessageRouter messageRouter) {
        this.networkManager = networkManager; 
        this.udpPinger = udpPinger;
        this.messageRouter = messageRouter;
        pingedHosts = new TreeMap<IpPort, RemoteFileDesc>(IpPort.COMPARATOR);
        testedLocations = new HashSet<RemoteFileDesc>();
        newHosts = new HashSet<RemoteFileDesc>();
        verifiedHosts = new TreeSet<RemoteFileDesc>(RFD_COMPARATOR);
    }
    
    @SuppressWarnings("unchecked")
    public synchronized boolean addToPool(Collection<? extends RemoteFileDesc> c)  {
        List<? extends RemoteFileDesc> l;
        if (c instanceof List)
            l = (List<? extends RemoteFileDesc>)c;
        else
            l = new ArrayList<RemoteFileDesc>(c);
        Collections.sort(l, ALT_DEPRIORITIZER);
        return addInternal(l);
    }
    
    /**
     * adds the collection of hosts to to the internal structures
     */
    private boolean addInternal(Collection<? extends RemoteFileDesc> c) {
        return false;
    }
    
    public synchronized boolean addToPool(RemoteFileDesc host){
        return false;
    }
    
    private boolean addInternal(RemoteFileDesc host) {
        // initialize the sha1 if we don't have one
        if (sha1 == null) {
            if( host.getSHA1Urn() != null)
                sha1 = host.getSHA1Urn();
            else    //  BUGFIX:  We can't discard sources w/out a SHA1 when we dont' have  
                    //  a SHA1 for the download, or else it won't be possible to download a
                    //  file from a query hit without a SHA1, if we can received UDP pings
                return testedLocations.add(host); // we can't do anything yet
        }
         
        // do not allow duplicate hosts 
        if (running && knowsAboutHost(host))
                return false;
        
        if(LOG.isDebugEnabled())
            LOG.debug("adding new host "+host+" "+host.getPushAddr());
        
        boolean ret = false;
        
        // don't bother ranking multicasts
        if (host.isReplyToMulticast())
            ret = verifiedHosts.add(host);
        else 
        	ret = newHosts.add(host); // rank
        
        // make sure that if we were stopped, we return true
        ret = ret | !running;
        
        // initialize the guid if we don't have one
        if (myGUID == null && meshHandler != null) {
            myGUID = new GUID(GUID.makeGuid());
            messageRouter.registerMessageListener(myGUID.bytes(),this);
        }
        
        return ret;
    }
    
    private boolean knowsAboutHost(RemoteFileDesc host) {
        return newHosts.contains(host) || 
            verifiedHosts.contains(host) || 
            testedLocations.contains(host);
    }
    
    protected Collection<RemoteFileDesc> getPotentiallyBusyHosts() {
        return newHosts;
    }
    
    /**
     * @return the appropriate ping flags based on current conditions
     */
    private int getPingFlags() {
        int flags = HeadPing.INTERVALS | HeadPing.ALT_LOCS;
        if (networkManager.acceptedIncomingConnection() ||
                networkManager.canDoFWT())
            flags |= HeadPing.PUSH_ALTLOCS;
        return flags;
    }
    
    public synchronized boolean hasMore() {
        return !(verifiedHosts.isEmpty() && newHosts.isEmpty() && testedLocations.isEmpty());
    }
    
    /**
     * Informs the Ranker that a host has replied with a HeadPing
     */
    public void processMessage(Message m, ReplyHandler handler) {
    }


    public synchronized void registered(byte[] guid) {
        if (LOG.isDebugEnabled())
            LOG.debug("ranker registered with guid "+(new GUID(guid)).toHexString());
        running = true;
    }

    public synchronized void unregistered(byte[] guid) {
        if (LOG.isDebugEnabled())
            LOG.debug("ranker unregistered with guid "+(new GUID(guid)).toHexString());
	
        running = false;
        newHosts.addAll(verifiedHosts);
        newHosts.addAll(testedLocations);
        verifiedHosts.clear();
        pingedHosts.clear();
        testedLocations.clear();
        lastPingTime = 0;
    }
    
    public synchronized boolean isCancelled(){
        return !running || verifiedHosts.size() >= DownloadSettings.MAX_VERIFIED_HOSTS.getValue();
    }
    
    protected synchronized void clearState(){
        if (myGUID != null) {
            messageRouter.unregisterMessageListener(myGUID.bytes(),this);
            myGUID = null;
        }
    }
    
    public synchronized Collection<RemoteFileDesc> getShareableHosts(){
        List<RemoteFileDesc>  ret = new ArrayList<RemoteFileDesc> (verifiedHosts.size()+newHosts.size()+testedLocations.size());
        ret.addAll(verifiedHosts);
        ret.addAll(newHosts);
        ret.addAll(testedLocations);
        return ret;
    }
    
    public synchronized int getNumKnownHosts() {
        return verifiedHosts.size()+newHosts.size()+testedLocations.size();
    }
    
    /**
     * class that actually does the preferencing of RFDs
     */
    private static final class RFDComparator implements Comparator<RemoteFileDesc> {
        public int compare(RemoteFileDesc pongA, RemoteFileDesc pongB) {
            // Multicasts are best
            if (pongA.isReplyToMulticast() != pongB.isReplyToMulticast()) {
                if (pongA.isReplyToMulticast())
                    return -1;
                else
                    return 1;
            }
            
            // HeadPongs with highest number of free slots get the highest priority
            if (pongA.getQueueStatus() > pongB.getQueueStatus())
                return 1;
            else if (pongA.getQueueStatus() < pongB.getQueueStatus())
                return -1;
       
            // Within the same queue rank, firewalled hosts get priority
            if (pongA.needsPush() != pongB.needsPush()) {
                if (pongA.needsPush())
                    return -1;
                else 
                    return 1;
            }
            
            // Within the same queue/fwall, partial hosts get priority
            if (pongA.isPartialSource() != pongB.isPartialSource()) {
                if (pongA.isPartialSource())
                    return -1;
                else
                    return 1;
            }
            
            // the two pongs seem completely the same
            return pongA.hashCode() - pongB.hashCode();
        }
    }
    
    /**
     * a ranker that deprioritizes RFDs from altlocs, used to make sure
     * we ping the hosts that actually returned results first
     */
    private static final class RFDAltDeprioritizer implements Comparator<RemoteFileDesc>{
        public int compare(RemoteFileDesc rfd1, RemoteFileDesc rfd2) {
            if (rfd1.isFromAlternateLocation() != rfd2.isFromAlternateLocation()) {
                if (rfd1.isFromAlternateLocation())
                    return 1;
                else
                    return -1;
            }
            return 0;
        }
    }
}
