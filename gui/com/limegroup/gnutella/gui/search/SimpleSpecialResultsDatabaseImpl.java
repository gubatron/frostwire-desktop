package com.limegroup.gnutella.gui.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.collection.NameValue;

import com.limegroup.gnutella.xml.LimeXMLDocument;
import com.limegroup.gnutella.xml.LimeXMLDocumentFactory;

/**
 * A default implementation of {@link ThirdPartyResultsDatabase} for testing.
 * Subclasses should be able to return a {@link List} of
 * {@link Map<String,String>} where each item in the list represents a single
 * result of a query mapping keys to values. Those keys must come from
 * {@link SimpleSpecialResultsDatabaseImpl#Attr}.
 */
abstract class SimpleSpecialResultsDatabaseImpl implements ThirdPartyResultsDatabase {
    
    private static final Log LOG = LogFactory.getLog(SimpleSpecialResultsDatabaseImpl.class);
    
    /**
     * The factory we use to create a {@link LimeXMLDocument} when creating search results.
     */
    private final LimeXMLDocumentFactory limeXMLDocumentFactory;
    
    /**
     * Return a list of {@link String}<code>x</code>{@link String} mappings
     * representing search results.  For example, given the query <code>cat</code>, an set of results would be
     * <pre>
     * [ {Attr.ARTIST="cat wilson",     Attr.URL="http://catwilson.com",     Attr.ALBUM="greatest hits",    Attr.GENRE="bad music"},
     *   {Attr.ARTIST="dog wilson",     Attr.URL="http://dogwilson.com",     Attr.ALBUM="greatest hits II", Attr.GENRE="bad music"},
     *   {Attr.ARTIST="turtle wilson",  Attr.URL="http://turtlewilson.com",  Attr.ALBUM="greatest hits IV", Attr.GENRE="bad music"}
     * ]
     * </pre>
     * 
     * @param keyword the search query
     * @return a list of {@link String}<code>x</code>{@link String} mappings
     *         representing search results
     */
    protected abstract List<Map<String,String>> getSearchResults(String keyword);
    
    /**
     * This class contains the attributes to use as keys for getting a
     * retrieving values in a query. It is <u>not</u> an <code>enum</code>
     * because sometimes we read from a file to create the DB and we aren't
     * assured that the values of an <code>enum</code> would be created with
     * declaring a variable of that type. And still, the compiler could choose
     * to optimize that away and the instances would not be created.
     * TODO: explain better
     */
    protected final static class Attr {
        
        private Attr() {}
        
        final static String XML_SCHEMA      = "xmlSchema";
        final static String URL             = "url";
        final static String SIZE            = "size";
        final static String CREATION_TIME   = "creation_time";
        final static String VENDOR          = "vendor";
        final static String NAME            = "name";
        final static String FILE_TYPE       = "fileType";
    };
    
    protected SimpleSpecialResultsDatabaseImpl(LimeXMLDocumentFactory limeXMLDocumentFactory) {
        this.limeXMLDocumentFactory = limeXMLDocumentFactory;
    }

    
    public final void find(SearchInformation info, SearchResultsCallback callback) {
        if (!beforeFind()) return;
        String query = info.getQuery();
        find(query, info, callback);
    }
    
    /**
     * We use this for testing, so we don't have to construct
     * {@link SearchInformation} in test cases.
     * 
     * @see #find(SearchInformation,
     *      com.limegroup.gnutella.gui.search.ThirdPartyResultsDatabase.SearchResultsCallback)
     */
    void find(String query, SearchInformation info, SearchResultsCallback callback) {
        List<SearchResult> res = new ArrayList<SearchResult>();
        if (query != null) {
            String lc = query.toLowerCase();
            List<Map<String,String>> nameValuePairsList = getSearchResults(lc);
            for (Map<String,String> nameValuePairs : nameValuePairsList) {

                String url            = nameValuePairs.get(Attr.URL);
                int size              = (int)getOrDefaultWithMax(nameValuePairs, Attr.SIZE, -1, Integer.MAX_VALUE);
                long creationTime     = getOrDefaultWithMax(nameValuePairs, Attr.CREATION_TIME, 0, Long.MAX_VALUE);
                String vendor         = nameValuePairs.get(Attr.VENDOR);
                String name           = nameValuePairs.get(Attr.NAME);
                String fileType       = nameValuePairs.get(Attr.FILE_TYPE);
                String xmlSchema      = nameValuePairs.get(Attr.XML_SCHEMA);
                if(xmlSchema == null)
                    xmlSchema = "audio";
                Collection<NameValue<String>> xmlValues = xmlValuesIn(nameValuePairs, xmlSchema);
                
                SearchResult sr = newSearchResult(name, fileType, xmlSchema, url, size, creationTime, vendor, xmlValues, query);
                if (sr != null && name != null) {
                    res.add(sr);
                }
            }
        }
        //
        // Hand back to the callback
        //
        callback.process(res, info);
    }
    
    private Collection<NameValue<String>> xmlValuesIn(Map<String, String> allNameValues, String xmlSchema) {
        String plural = xmlSchema + "s";
        List<NameValue<String>> xmlValues = new ArrayList<NameValue<String>>();
        for(Map.Entry<String, String> entry : allNameValues.entrySet()) {
            if(entry.getKey().startsWith("xml_"))
                xmlValues.add(new NameValue<String>(plural + "__" + xmlSchema + "__" + entry.getKey().substring(4) + "__", entry.getValue()));
        }
        return xmlValues;
    }
    
    /**
     * Returns <code>true</code> if we should proceed in a call to
     * {@link #find(SearchInformation, com.limegroup.gnutella.gui.search.ThirdPartyResultsDatabase.SearchResultsCallback)}
     * after doing any work before proceeding with the call (defaults to
     * <code>true</code>). Examples of work to do would be to check a remote
     * setting, lazily intialize a buffer of results, etc. This allows an
     * implementation to add any functionality needed to do before calling
     * {@link #find(SearchInformation, com.limegroup.gnutella.gui.search.ThirdPartyResultsDatabase.SearchResultsCallback)}
     * without a client of this object worry about checking.
     * <p>
     * Subclasses <b>must</b> call the super implementation and return
     * <code>false</code> on a <code>false</code> return value to respect
     * the super classes implementation.
     * 
     * @return <code>true</code> if we should proceed in a call to
     *         {@link #find(SearchInformation, com.limegroup.gnutella.gui.search.ThirdPartyResultsDatabase.SearchResultsCallback)}
     *         after doing any work before proceeding with the call (defaults to
     *         <code>true</code>)
     */
    protected boolean beforeFind() { return true; }    
        
    /**
     * May return <code>null</code>
     * @param fileType TODO
     * @param keyword TODO
     */
    private SearchResult newSearchResult(String name, String fileType, String xmlSchema, String url, int size, long creationTime, String vendor, Collection<NameValue<String>> xmlValues, String keyword) {
        LimeXMLDocument xmlDoc = null;
        if(xmlValues.size() > 0) {
            try {
                xmlDoc = limeXMLDocumentFactory.createLimeXMLDocument(xmlValues, "http://www.limewire.com/schemas/" + xmlSchema + ".xsd");
            } catch (IllegalArgumentException iae) {
                LOG.error("error creating document", iae);
            }
        }
        
        return new ThirdPartySearchResult(name, fileType, url, size, creationTime, xmlDoc, vendor, keyword);
    }
    
    /**
     * Returns the value found in <code>nameValuePairs</code> or
     * 'def' if it's not found.  If the value is over 'max', 'max' is returned.
     * 
     * @param nameValuePairs name value pair mapping
     * @param attr name of the value to find in <code>nameValuePairs</code>
     */
    private long getOrDefaultWithMax(Map<String, String> nameValuePairs, String attr, long def, long max) {
        String value = nameValuePairs.get(attr);
        if (value == null)
            return def;
        try {
            return Math.min(max, Long.parseLong(value));
        } catch (NumberFormatException ignored) {}
        return def;
    }
}
