package com.limegroup.gnutella.messages;

import java.util.Set;

import org.limewire.util.I18NConvert;
import org.limewire.util.OSUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.NetworkManager;
import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.UrnSet;
import com.limegroup.gnutella.messages.Message.Network;
import com.limegroup.gnutella.xml.LimeXMLDocumentFactory;

@Singleton
public class QueryRequestFactoryImpl implements QueryRequestFactory {

    private final NetworkManager networkManager;
    private final LimeXMLDocumentFactory limeXMLDocumentFactory;

    @Inject
    public QueryRequestFactoryImpl(NetworkManager networkManager, 
            LimeXMLDocumentFactory limeXMLDocumentFactory) {
        this.networkManager = networkManager;
        this.limeXMLDocumentFactory = limeXMLDocumentFactory;
        
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createRequery(com.limegroup.gnutella.URN)
     */
    public QueryRequest createRequery(URN sha1) {
        if (sha1 == null) {
            throw new NullPointerException("null sha1");
        }
        Set<URN> sha1Set = new UrnSet(sha1);
        return null;

    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createQuery(com.limegroup.gnutella.URN)
     */
    public QueryRequest createQuery(URN sha1) {
        if (sha1 == null) {
            throw new NullPointerException("null sha1");
        }
        Set<URN> sha1Set = new UrnSet(sha1);
        return null;

    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createQuery(com.limegroup.gnutella.URN, java.lang.String)
     */
    public QueryRequest createQuery(URN sha1, String filename) {
        if (sha1 == null) {
            throw new NullPointerException("null sha1");
        }
        if (filename == null) {
            throw new NullPointerException("null query");
        }
        if (filename.length() == 0) {
            filename = QueryRequest.DEFAULT_URN_QUERY;
        }
        Set<URN> sha1Set = new UrnSet(sha1);
        return null;

    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createRequery(com.limegroup.gnutella.URN, byte)
     */
    public QueryRequest createRequery(URN sha1, byte ttl) {
        if (sha1 == null) {
            throw new NullPointerException("null sha1");
        }
        if (ttl <= 0 || ttl > 6) {
            throw new IllegalArgumentException("invalid TTL: " + ttl);
        }
        Set<URN> sha1Set = new UrnSet(sha1);
        return null;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createQuery(java.util.Set)
     */
    public QueryRequest createQuery(Set<? extends URN> urnSet) {
        if (urnSet == null)
            throw new NullPointerException("null urnSet");
        return null;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createRequery(java.lang.String)
     */
    public QueryRequest createRequery(String query) {
        if (query == null) {
            throw new NullPointerException("null query");
        }
        if (query.length() == 0) {
            throw new IllegalArgumentException("empty query");
        }
        return create(QueryRequestImpl.newQueryGUID(true), query);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createQuery(java.lang.String)
     */
    public QueryRequest createQuery(String query) {
        if (query == null) {
            throw new NullPointerException("null query");
        }
        if (query.length() == 0) {
            throw new IllegalArgumentException("empty query");
        }
        return create(QueryRequestImpl.newQueryGUID(false), query);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createOutOfBandQuery(byte[], java.lang.String, java.lang.String)
     */
    public QueryRequest createOutOfBandQuery(byte[] guid, String query,
            String xmlQuery) {
        query = I18NConvert.instance().getNorm(query);
        if (query == null) {
            throw new NullPointerException("null query");
        }
        if (xmlQuery == null) {
            throw new NullPointerException("null xml query");
        }
        if (query.length() == 0 && xmlQuery.length() == 0) {
            throw new IllegalArgumentException("empty query");
        }
        if (xmlQuery.length() != 0 && !xmlQuery.startsWith("<?xml")) {
            throw new IllegalArgumentException("invalid XML");
        }
        return create(guid, QueryRequest.DEFAULT_TTL, query, xmlQuery, true);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createOutOfBandQuery(byte[], java.lang.String, java.lang.String, com.limegroup.gnutella.MediaType)
     */
    public QueryRequest createOutOfBandQuery(byte[] guid, String query,
            String xmlQuery, MediaType type) {
        query = I18NConvert.instance().getNorm(query);
        if (query == null) {
            throw new NullPointerException("null query");
        }
        if (xmlQuery == null) {
            throw new NullPointerException("null xml query");
        }
        if (query.length() == 0 && xmlQuery.length() == 0) {
            throw new IllegalArgumentException("empty query");
        }
        if (xmlQuery.length() != 0 && !xmlQuery.startsWith("<?xml")) {
            throw new IllegalArgumentException("invalid XML");
        }
        return create(guid, QueryRequest.DEFAULT_TTL, query, xmlQuery, true,
                type);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createOutOfBandQuery(java.lang.String, byte[], int)
     */
    public QueryRequest createOutOfBandQuery(String query, byte[] ip, int port) {
        byte[] guid = GUID.makeAddressEncodedGuid(ip, port);
        return createOutOfBandQuery(guid, query, "");
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createWhatIsNewQuery(byte[], byte)
     */
    public QueryRequest createWhatIsNewQuery(byte[] guid, byte ttl) {
        return createWhatIsNewQuery(guid, ttl, null);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createWhatIsNewQuery(byte[], byte, com.limegroup.gnutella.MediaType)
     */
    public QueryRequest createWhatIsNewQuery(byte[] guid, byte ttl,
            MediaType type) {
        if (ttl < 1)
            throw new IllegalArgumentException("Bad TTL.");
        return null;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createWhatIsNewOOBQuery(byte[], byte)
     */
    public QueryRequest createWhatIsNewOOBQuery(byte[] guid, byte ttl) {
        return createWhatIsNewOOBQuery(guid, ttl, null);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createWhatIsNewOOBQuery(byte[], byte, com.limegroup.gnutella.MediaType)
     */
    public QueryRequest createWhatIsNewOOBQuery(byte[] guid, byte ttl,
            MediaType type) {
        if (ttl < 1)
            throw new IllegalArgumentException("Bad TTL.");
        return null;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createQuery(java.lang.String, java.lang.String)
     */
    public QueryRequest createQuery(String query, String xmlQuery) {
        if (query == null) {
            throw new NullPointerException("null query");
        }
        if (xmlQuery == null) {
            throw new NullPointerException("null xml query");
        }
        if (query.length() == 0 && xmlQuery.length() == 0) {
            throw new IllegalArgumentException("empty query");
        }
        if (xmlQuery.length() != 0 && !xmlQuery.startsWith("<?xml")) {
            throw new IllegalArgumentException("invalid XML");
        }
        return create(QueryRequestImpl.newQueryGUID(false), query, xmlQuery);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createQuery(java.lang.String, byte)
     */
    public QueryRequest createQuery(String query, byte ttl) {
        if (query == null) {
            throw new NullPointerException("null query");
        }
        if (query.length() == 0) {
            throw new IllegalArgumentException("empty query");
        }
        if (ttl <= 0 || ttl > 6) {
            throw new IllegalArgumentException("invalid TTL: " + ttl);
        }
        return create(QueryRequestImpl.newQueryGUID(false), ttl, query);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createQuery(byte[], java.lang.String, java.lang.String)
     */
    public QueryRequest createQuery(byte[] guid, String query, String xmlQuery) {
        query = I18NConvert.instance().getNorm(query);
        if (guid == null) {
            throw new NullPointerException("null guid");
        }
        if (guid.length != 16) {
            throw new IllegalArgumentException("invalid guid length");
        }
        if (query == null) {
            throw new NullPointerException("null query");
        }
        if (xmlQuery == null) {
            throw new NullPointerException("null xml query");
        }
        if (query.length() == 0 && xmlQuery.length() == 0) {
            throw new IllegalArgumentException("empty query");
        }
        if (xmlQuery.length() != 0 && !xmlQuery.startsWith("<?xml")) {
            throw new IllegalArgumentException("invalid XML");
        }
        return create(guid, query, xmlQuery);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createQuery(byte[], java.lang.String, java.lang.String, com.limegroup.gnutella.MediaType)
     */
    public QueryRequest createQuery(byte[] guid, String query, String xmlQuery,
            MediaType type) {
        query = I18NConvert.instance().getNorm(query);
        if (guid == null) {
            throw new NullPointerException("null guid");
        }
        if (guid.length != 16) {
            throw new IllegalArgumentException("invalid guid length");
        }
        if (query == null) {
            throw new NullPointerException("null query");
        }
        if (xmlQuery == null) {
            throw new NullPointerException("null xml query");
        }
        if (query.length() == 0 && xmlQuery.length() == 0) {
            throw new IllegalArgumentException("empty query");
        }
        if (xmlQuery.length() != 0 && !xmlQuery.startsWith("<?xml")) {
            throw new IllegalArgumentException("invalid XML");
        }
        return create(guid, QueryRequest.DEFAULT_TTL, query, xmlQuery, type);
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createProxyQuery(com.limegroup.gnutella.messages.QueryRequest, byte[])
     */
    public QueryRequest createProxyQuery(QueryRequest qr, byte[] guid) {
//        if (guid.length != 16)
//            throw new IllegalArgumentException("bad guid size: " + guid.length);
//
//        // i can't just call a new constructor, since there might be stuff in
//        // the payload we don't understand and would get lost
//        byte[] payload = qr.getPayload();
//        byte[] newPayload = new byte[payload.length];
//        System.arraycopy(payload, 0, newPayload, 0, newPayload.length);
//        // disable old out of band if requested
//        if (SearchSettings.DISABLE_OOB_V2.getBoolean())
//            newPayload[0] &= ~QueryRequest.SPECIAL_OUTOFBAND_MASK;
//        else
//            newPayload[0] |= QueryRequest.SPECIAL_OUTOFBAND_MASK;
//        GGEP ggep = new GGEP(true);
//        // signal oob capability
//        ggep.put(GGEP.GGEP_HEADER_SECURE_OOB);
//
//        try {
//            newPayload = QueryRequestImpl.patchInGGEP(newPayload, ggep, MACCalculatorRepositoryManager);
//            return createNetworkQuery(guid, qr.getTTL(), qr.getHops(),
//                    newPayload, qr.getNetwork());
//        } catch (BadPacketException ioe) {
//            throw new IllegalArgumentException(ioe.getMessage());
//        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createDoNotProxyQuery(com.limegroup.gnutella.messages.QueryRequest)
     */
    public QueryRequest createDoNotProxyQuery(QueryRequest qr) {
        return null;
        // normalization
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createQuery(com.limegroup.gnutella.messages.QueryRequest, byte)
     */
    public QueryRequest createQuery(QueryRequest qr, byte ttl) {
        // Construct a query request that is EXACTLY like the other query,
        // but with a different TTL.
        try {
            return createNetworkQuery(qr.getGUID(), ttl, qr.getHops(), qr
                    .getPayload(), qr.getNetwork());
        } catch (BadPacketException ioe) {
            throw new IllegalArgumentException(ioe.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#unmarkOOBQuery(com.limegroup.gnutella.messages.QueryRequest)
     */
    public QueryRequest unmarkOOBQuery(QueryRequest qr) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createQueryKeyQuery(java.lang.String, org.limewire.security.AddressSecurityToken)
     */
    public QueryRequest createQueryKeyQuery(String query) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createQueryKeyQuery(com.limegroup.gnutella.URN, org.limewire.security.AddressSecurityToken)
     */
    public QueryRequest createQueryKeyQuery(URN sha1) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createMulticastQuery(byte[], com.limegroup.gnutella.messages.QueryRequest)
     */
    public QueryRequest createMulticastQuery(byte[] guid, QueryRequest qr) {
        if (qr == null)
            throw new NullPointerException("null query");

        // modify the payload to not be OOB.
        byte[] payload = qr.getPayload();
        byte[] newPayload = new byte[payload.length];
        System.arraycopy(payload, 0, newPayload, 0, newPayload.length);
        newPayload[0] &= ~QueryRequest.SPECIAL_OUTOFBAND_MASK;
        newPayload[0] |= QueryRequest.SPECIAL_XML_MASK;

        try {
            return createNetworkQuery(guid, (byte) 1, qr.getHops(), newPayload,
                    Network.MULTICAST);
        } catch (BadPacketException ioe) {
            throw new IllegalArgumentException(ioe.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createQueryKeyQuery(com.limegroup.gnutella.messages.QueryRequest, org.limewire.security.AddressSecurityToken)
     */
    public QueryRequest createQueryKeyQuery(QueryRequest qr) {

        return null;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createBrowseHostQuery()
     */
    public QueryRequest createBrowseHostQuery() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createNonFirewalledQuery(java.lang.String, byte)
     */
    public QueryRequest createNonFirewalledQuery(String query, byte ttl) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createNetworkQuery(byte[], byte, byte, byte[], com.limegroup.gnutella.messages.Message.Network)
     */
    public QueryRequest createNetworkQuery(byte[] guid, byte ttl, byte hops,
            byte[] payload, Network network) throws BadPacketException {
        return null;
    }

    /**
     * Builds a new query from scratch, with no metadata, using the given GUID.
     * Whether or not this is a repeat query is encoded in guid. GUID must have
     * been created via newQueryGUID; this allows the caller to match up results
     */
    private QueryRequest create(byte[] guid, String query) {
        return create(guid, query, "");
    }

    /**
     * Builds a new query from scratch, with no metadata, using the given GUID.
     * Whether or not this is a repeat query is encoded in guid. GUID must have
     * been created via newQueryGUID; this allows the caller to match up results
     */
    private QueryRequest create(byte[] guid, byte ttl, String query) {
        return create(guid, ttl, query, "");
    }

    /**
     * Builds a new query from scratch, with no metadata, using the given GUID.
     * Whether or not this is a repeat query is encoded in guid. GUID must have
     * been created via newQueryGUID; this allows the caller to match up results
     */
    private QueryRequest create(byte[] guid, String query, String xmlQuery) {
        return create(guid, QueryRequest.DEFAULT_TTL, query, xmlQuery);
    }

    /**
     * Builds a new query from scratch, with metadata, using the given GUID.
     * Whether or not this is a repeat query is encoded in guid. GUID must have
     * been created via newQueryGUID; this allows the caller to match up
     * results.
     * 
     * @requires 0<=minSpeed<2^16 (i.e., can fit in 2 unsigned bytes)
     */
    private QueryRequest create(byte[] guid, byte ttl, String query,
            String richQuery) {
        return null;
    }

    /**
     * Builds a new query from scratch, with metadata, using the given GUID.
     * Whether or not this is a repeat query is encoded in guid. GUID must have
     * been created via newQueryGUID; this allows the caller to match up
     * results.
     * 
     * @requires 0<=minSpeed<2^16 (i.e., can fit in 2 unsigned bytes)
     */
    private QueryRequest create(byte[] guid, byte ttl, String query,
            String richQuery, MediaType type) {
        return null;
    }

    /**
     * Builds a new query from scratch, with metadata, using the given GUID.
     * Whether or not this is a repeat query is encoded in guid. GUID must have
     * been created via newQueryGUID; this allows the caller to match up
     * results.
     * 
     * @requires 0<=minSpeed<2^16 (i.e., can fit in 2 unsigned bytes)
     */
    private QueryRequest create(byte[] guid, byte ttl, String query,
            String richQuery, boolean canReceiveOutOfBandReplies) {
        return null;
    }

    /**
     * Builds a new query from scratch, with metadata, using the given GUID.
     * Whether or not this is a repeat query is encoded in guid. GUID must have
     * been created via newQueryGUID; this allows the caller to match up
     * results.
     * 
     * @requires 0<=minSpeed<2^16 (i.e., can fit in 2 unsigned bytes)
     */
    private QueryRequest create(byte[] guid, byte ttl, String query,
            String richQuery, boolean canReceiveOutOfBandReplies, MediaType type) {
        return null;
    }

    private int getMetaFlag(MediaType type) {
        int metaFlag = 0;
        if (type == null)
            ;
        else if (type == MediaType.getAudioMediaType())
            metaFlag |= QueryRequest.AUDIO_MASK;
        else if (type == MediaType.getVideoMediaType())
            metaFlag |= QueryRequest.VIDEO_MASK;
        else if (type == MediaType.getImageMediaType())
            metaFlag |= QueryRequest.IMAGE_MASK;
        else if (type == MediaType.getDocumentMediaType())
            metaFlag |= QueryRequest.DOC_MASK;
        else if (type == MediaType.getProgramMediaType()) {
            if (OSUtils.isLinux() || OSUtils.isAnyMac())
                metaFlag |= QueryRequest.LIN_PROG_MASK;
            else if (OSUtils.isWindows())
                metaFlag |= QueryRequest.WIN_PROG_MASK;
            else
                // Other OS, search any type of programs
                metaFlag |= (QueryRequest.LIN_PROG_MASK | QueryRequest.WIN_PROG_MASK);
        }
        return metaFlag;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createQueryRequest(byte[], byte, java.lang.String, java.lang.String, java.util.Set, org.limewire.security.AddressSecurityToken, boolean, com.limegroup.gnutella.messages.Message.Network, boolean, int)
     */
    public QueryRequest createQueryRequest(byte[] guid, byte ttl, String query,
            String richQuery, Set<? extends URN> queryUrns,
            boolean isFirewalled,
            Network network, boolean canReceiveOutOfBandReplies,
            int featureSelector) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createQueryRequest(byte[], byte, java.lang.String, java.lang.String, java.util.Set, org.limewire.security.AddressSecurityToken, boolean, com.limegroup.gnutella.messages.Message.Network, boolean, int, boolean, int)
     */
    public QueryRequest createQueryRequest(byte[] guid, byte ttl, String query,
            String richQuery, Set<? extends URN> queryUrns,
            boolean isFirewalled,
            Network network, boolean canReceiveOutOfBandReplies,
            int featureSelector, boolean doNotProxy, int metaFlagMask) {
        return null;
    }

    /**
     * Constructs a query with an optional 'normalize' parameter, which if
     * false, does not normalize the query string.
     */
    private QueryRequest createQueryRequest(byte[] guid, byte ttl,
            String query, String richQuery, Set<? extends URN> queryUrns,
            boolean isFirewalled,
            Network network, boolean canReceiveOutOfBandReplies,
            int featureSelector, boolean doNotProxy, int metaFlagMask,
            boolean normalize) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createQueryRequest(byte[], byte, int, java.lang.String, java.lang.String, java.util.Set, org.limewire.security.AddressSecurityToken, boolean, com.limegroup.gnutella.messages.Message.Network, boolean, int, boolean, int)
     */
    public QueryRequest createQueryRequest(byte[] guid, byte ttl, int minSpeed,
            String query, String richQuery, Set<? extends URN> queryUrns,
            boolean isFirewalled,
            Network network, boolean canReceiveOutOfBandReplies,
            int featureSelector, boolean doNotProxy, int metaFlagMask) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.limegroup.gnutella.messages.QueryRequestFactory#createQueryRequest(byte[], byte, int, java.lang.String, java.lang.String, java.util.Set, org.limewire.security.AddressSecurityToken, boolean, com.limegroup.gnutella.messages.Message.Network, boolean, int, boolean, int, boolean)
     */
    public QueryRequest createQueryRequest(byte[] guid, byte ttl, int minSpeed,
            String query, String richQuery, Set<? extends URN> queryUrns,
            boolean isFirewalled,
            Network network, boolean canReceiveOutOfBandReplies,
            int featureSelector, boolean doNotProxy, int metaFlagMask,
            boolean normalize) {
        return null;
    }

}
