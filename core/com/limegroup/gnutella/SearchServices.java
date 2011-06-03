package com.limegroup.gnutella;

import com.limegroup.gnutella.search.HostData;

public interface SearchServices {
    
    /** Purges the query from the QueryUnicaster (GUESS) and the ResultHandler
     *  (which maintains query stats for the purpose of leaf guidance).
     *  @param guid The GUID of the query you want to get rid of....
     */
    public void stopQuery(GUID guid);

    /**
     * Accessor for the last time a query was originated from this host.
     *
     * @return a <tt>long</tt> representing the number of milliseconds since
     *  January 1, 1970, that the last query originated from this host
     */
    public long getLastQueryTime();

    /**
     * Sends a 'What Is New' query on the network.
     */
    public void queryWhatIsNew(final byte[] guid, final MediaType type);

    /**
     * Searches the network for files with the given metadata.
     * 
     * @param richQuery metadata query to insert between the nulls,
     *  typically in XML format
     * @see query(byte[], String, MediaType)
     */
    public void query(final byte[] guid, final String query,
            final String richQuery, final MediaType type);

    /** 
     * Searches the network for files with the given query string and 
     * minimum speed, i.e., same as query(guid, query, minSpeed, null). 
     *
     * @see query(byte[], String, MediaType)
     */
    public void query(byte[] guid, String query);

    /**
     * Searches the network for files of the given type with the given
     * GUID, query string and minimum speed.  If type is null, any file type
     * is acceptable.<p>
     *
     * ActivityCallback is notified asynchronously of responses.  These
     * responses can be matched with requests by looking at their GUIDs.  (You
     * may want to wrap the bytes with a GUID object for simplicity.)  An
     * earlier version of this method returned the reply GUID instead of taking
     * it as an argument.  Unfortunately this caused a race condition where
     * replies were returned before the GUI was prepared to handle them.
     * 
     * @param guid the guid to use for the query.  MUST be a 16-byte
     *  value as returned by newQueryGUID.
     * @param query the query string to use
     * @param minSpeed the minimum desired result speed
     * @param type the desired type of result (e.g., audio, video), or
     *  null if you don't care 
     */
    public void query(byte[] guid, String query, MediaType type);

    /** 
     * Returns a new GUID for passing to query.
     * This method is the central point of decision making for sending out OOB 
     * queries.
     */
    public byte[] newQueryGUID();

	public boolean isFloodQueryReply(HostData data, Response response);

}