package com.limegroup.gnutella.messages.vendor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.PushEndpointFactory;
import com.limegroup.gnutella.messages.BadPacketException;
import com.limegroup.gnutella.messages.Message.Network;

@Singleton
public class HeadPongFactoryImpl implements HeadPongFactory {
    
    private final PushEndpointFactory pushEndpointFactory; 

    /** The real packet size. */
    public static final int DEFAULT_PACKET_SIZE = 1380;
    
    @Inject
    public HeadPongFactoryImpl(
            PushEndpointFactory pushEndpointFactory) {
        this.pushEndpointFactory = pushEndpointFactory;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.vendor.HeadPongFactory#createFromNetwork(byte[], byte, byte, int, byte[])
     */
    public HeadPong createFromNetwork(byte[] guid, byte ttl, byte hops,
            int version, byte[] payload, Network network) throws BadPacketException {
        return new HeadPongImpl(guid, ttl, hops, version, payload, network, pushEndpointFactory);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.vendor.HeadPongFactory#create(com.limegroup.gnutella.messages.vendor.HeadPongRequestor)
     */
    public HeadPong create(HeadPongRequestor ping) {
        return new HeadPongImpl(new GUID(ping.getGUID()), versionFor(ping), derivePayload(ping));
    }

    /** Constructs the payload in GGEP format. */
    private byte[] constructGGEPPayload(HeadPongRequestor ping) {
//        GGEP ggep = new GGEP();
//        
//        URN urn = ping.getUrn();
//        FileDesc desc = fileManager.get().getFileDescForUrn(urn);
//        // Easy case: no file, add code & exit.
//        if(desc == null) {
//            ggep.put(HeadPong.CODE, HeadPong.FILE_NOT_FOUND);
//            return writeGGEP(ggep);
//        }
//        
//        // OK, we have the file, now what!
//        int size = 1;  // begin with 1 because of GGEP magic
//        
//        // If we're not firewalled and support TLS,
//        // spread word about our TLS status.
//        if(networkManager.acceptedIncomingConnection() && 
//                SSLSettings.isIncomingTLSEnabled() ) {
//            ggep.put(HeadPong.FEATURES, HeadPong.TLS_CAPABLE);
//            size += 4;
//        }
//        
//        byte code = calculateCode(desc);
//        ggep.put(HeadPong.CODE, code); size += ggep.getHeaderOverhead(HeadPong.CODE);
//        ggep.put(HeadPong.VENDOR, VendorMessage.F_LIME_VENDOR_ID); size += ggep.getHeaderOverhead(HeadPong.VENDOR);
//        ggep.put(HeadPong.QUEUE, calculateQueueStatus()); size += ggep.getHeaderOverhead(HeadPong.QUEUE);
//        
//        // NOTE: All insertion checks assume that the header is going to take up
//        //       the maximum amount of bytes possible for a GGEP header + overhead.
//        
//        if((code & HeadPong.PARTIAL_FILE) == HeadPong.PARTIAL_FILE && ping.requestsRanges()) {
//            IntervalSet.ByteIntervals ranges = deriveRanges(desc);
//            if(ranges.length() == 0) {
//                // If we have no ranges available, change queue status to busy,
//                // so that they come back and ask us later, when we may have
//                // more ranges available. (but don't increment size, since that
//                // was already done above.)
//                ggep.put(HeadPong.QUEUE, HeadPong.BUSY);
//            } else if(size + ranges.length() + 11 <= PACKET_SIZE) { //5 for "R" and 6 for "R5"
//                if (ranges.ints.length > 0) {
//                    ggep.put(HeadPong.RANGES, ranges.ints);
//                    size += ggep.getHeaderOverhead(HeadPong.RANGES);
//                }
//                if (ranges.longs.length > 0) {
//                    ggep.put(HeadPong.RANGES5, ranges.longs);
//                    size += ggep.getHeaderOverhead(HeadPong.RANGES5);
//                }
//            }
//        }
//        
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        addPushLocations(ping, urn, out, true, size+5, false);
//        if(out.size() > 0) {
//            byte[] pushLocs = out.toByteArray();
//            ggep.put(HeadPong.PUSH_LOCS, pushLocs);
//            size += ggep.getHeaderOverhead(HeadPong.PUSH_LOCS);
//        }
//        
//        out.reset();
//        AtomicReference<BitNumbers> bnRef = new AtomicReference<BitNumbers>();
//        addLocations(ping, urn, out, bnRef, size+5, false);
//        if(out.size() > 0) {
//            byte[] altLocs = out.toByteArray();
//            ggep.put(HeadPong.LOCS, altLocs);
//            size += ggep.getHeaderOverhead(HeadPong.LOCS);
//        }
//        
//        // If it went over, we screwed up somewhere.
//        assert size <= PACKET_SIZE : "size is too big "+size+" vs "+PACKET_SIZE;
//        
//        // Here we fudge a bit -- possibly going over PACKET_SIZE.
//        BitNumbers bn = bnRef.get();
//        if(bn != null) {
//            byte[] bnBytes = bn.toByteArray();
//            if(bnBytes.length > 0) {
//                ggep.put(HeadPong.TLS_LOCS, bnBytes);
//                size += ggep.getHeaderOverhead(HeadPong.TLS_LOCS);
//            }
//        }
//        
//        byte[] output = writeGGEP(ggep);
//        assert output.length == size : "expected: " + size + ", was: " + output.length;
//        return output;
        return null;
    }

    /** Constructs the payload in binary format. */
    private byte[] constructBinaryPayload(HeadPongRequestor ping) {
//    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
//    	CountingOutputStream caos = new CountingOutputStream(baos);
//    	DataOutputStream daos = new DataOutputStream(caos);
//    	byte retCode=0;
//    	URN urn = ping.getUrn();
//    	FileDesc desc = fileManager.get().getFileDescForUrn(urn);
//    	boolean didNotSendAltLocs=false;
//    	boolean didNotSendPushAltLocs = false;
//    	boolean didNotSendRanges = false;
//    	
//    	try {
//    		byte features = ping.getFeatures();
//    		features &= ~HeadPing.GGEP_PING;
//    		daos.write(features);
//    		if (LOG.isDebugEnabled())
//    			LOG.debug("writing features "+features);
//    		
//    		//if we don't have the file or its too large...
//    		if (desc == null || desc.getFileSize() > Integer.MAX_VALUE) {
//    			LOG.debug("we do not have the file");
//    			daos.write(HeadPong.FILE_NOT_FOUND);
//    			return baos.toByteArray();
//    		}
//    
//            retCode = calculateCode(desc);
//    		daos.write(retCode);
//    		
//    		if(LOG.isDebugEnabled())
//    			LOG.debug("our return code is "+retCode);
//    		
//    		//write the vendor id
//    		daos.write(VendorMessage.F_LIME_VENDOR_ID);
//    
//    		//write out the return code and the queue status
//    		daos.writeByte(calculateQueueStatus());
//    		
//    		//if we sent partial file and the remote asked for ranges, send them 
//    		if ((retCode & HeadPong.PARTIAL_FILE) == HeadPong.PARTIAL_FILE && ping.requestsRanges()) 
//    			didNotSendRanges=!writeRanges(caos,desc);
//            
//            didNotSendPushAltLocs = addPushLocations(ping, urn, caos, false, caos.getAmountWritten(), true);
//            didNotSendAltLocs = addLocations(ping, urn, caos, null, caos.getAmountWritten(), true);
//    		
//    	} catch(IOException impossible) {
//    		ErrorService.error(impossible);
//    	}
//    	
//    	//done!
//    	byte []ret = baos.toByteArray();
//    	
//    	//if we did not add ranges or altlocs due to constraints, 
//    	//update the flags now.
//    	
//    	if (didNotSendRanges){
//    		LOG.debug("not sending ranges");
//    		ret[0] = (byte) (ret[0] & ~HeadPing.INTERVALS);
//    	}
//    	if (didNotSendAltLocs){
//    		LOG.debug("not sending altlocs");
//    		ret[0] = (byte) (ret[0] & ~HeadPing.ALT_LOCS);
//    	}
//    	if (didNotSendPushAltLocs){
//    		LOG.debug("not sending push altlocs");
//    		ret[0] = (byte) (ret[0] & ~HeadPing.PUSH_ALTLOCS);
//    	}
//    	return ret;
        return null;
    }

    /**
     * Constructs a byte[] that contains the payload of the HeadPong.
     * 
     * @param ping the original UDP head ping to respond to
     */
    private byte [] derivePayload(HeadPongRequestor ping)  {
        if(!ping.isPongGGEPCapable()) {
            return constructBinaryPayload(ping);
        } else {
            return constructGGEPPayload(ping);
        }
    }

    /** Determines the version that will be used based on the requestor. */
    private int versionFor(HeadPongRequestor ping) {
        if(!ping.isPongGGEPCapable())
            return HeadPong.BINARY_VERSION;
        else
            return HeadPong.VERSION;
    }
}
