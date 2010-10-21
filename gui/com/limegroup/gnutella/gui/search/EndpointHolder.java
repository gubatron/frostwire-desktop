package com.limegroup.gnutella.gui.search;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.limewire.io.IpPort;

import com.limegroup.gnutella.gui.GuiCoreMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.settings.FilterSettings;



/**
 * Holds one or more hosts.  Used for displaying the IP.
 */
class EndpointHolder implements Comparable<EndpointHolder> {
    
    /**
     * String for "Multiple"
     */
    private static final String MULTIPLE =
        I18n.tr("Multiple");    

    /**
     * The host this represents.
     */
    private final String _hostName;
    
    /**
     * The port of this host.
     */
    private final int _port;

    /**
     * Whether or not this IP is private.
     */
    private boolean _isPrivate;
    
    /**
     * The tag to display.
     */
    private String _tag;
    
    /**
     * The hosts this holds.
     */
    private Set<String> _hosts;
    
    private int _hardCodedNumLocations = -1;
    
    
        
    /**
     * Builds an EndpointHolder with the specified host/port.
     */
    EndpointHolder(final String host, int port, boolean replyToMCast) {
        _hostName = host;
        _port = port;
        _isPrivate = !replyToMCast
                && GuiCoreMediator.getNetworkInstanceUtils().isPrivateAddress(host);
        _tag = host;
    }
    
    void addHost(final String host, int port) {
        if(_hosts == null) {
            _hosts = new HashSet<String>();
            _hosts.add(_hostName + ":" + _port);
        }
        _hosts.add(host + ":" + port);
        _tag = MULTIPLE + " (" + _hosts.size() + ")";
        _isPrivate = false;
    }
    
    void addHosts(Set alts) {
        if(_hosts == null) {
            _hosts = new HashSet<String>();
            _hosts.add(_hostName + ":" + _port);
        }
        // only add a few altlocs per reply
        int added = 0;
        for(Iterator i = alts.iterator(); i.hasNext() &&
        added++ < FilterSettings.MAX_ALTS_TO_DISPLAY.getValue(); ) {
            IpPort next = (IpPort)i.next();
            _hosts.add(next.getAddress() + ":" + next.getPort());
        }
        _tag = MULTIPLE + " (" + _hosts.size() + ")";
        _isPrivate = false;
    }
    
    /**
     * Gets the set of hosts.
     */
    Set<String> getHosts() {
        return _hosts;
    }
    
    /**
     * Set to -1 to un-hardcode
     * @param num
     */
    public void setHardCodedNumLocations(int num) {
    	_hardCodedNumLocations = num;
    }
    
    /**
     * Returns the number of locations this holder knows about.
     */
    int numLocations() {
    	if (_hardCodedNumLocations != -1)
    		return _hardCodedNumLocations;
    	
        return _hosts == null ? 1 : _hosts.size();
    }
    
    /**
     * Whether or not this endpoint represents a private address.
     */
    boolean isPrivateAddress() {
        return _isPrivate;
    }

    /**
     * Returns the tag of this holder.
     */
    public String toString() {
        return _tag;
    }
    
    public int compareTo(EndpointHolder other) {
        int n1 = numLocations(), n2 = other.numLocations();
        if(n1 == 1 && n2 == 1)
            return _tag.compareTo(other._tag);
        else
            return n1 - n2;
    }
}
    
