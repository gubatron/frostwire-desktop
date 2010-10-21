package com.limegroup.gnutella.gui.search;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.limegroup.gnutella.xml.LimeXMLDocumentFactory;

/**
 * A default implementation of {@link ThirdPartyResultsDatabase} for testing.
 * This is abstracted out so we can deal with the format of the stream here and
 * then have subclasses provide ways of open an {@link InputStream}.
 */
class BasicSpecialResultsDatabaseImpl extends SimpleSpecialResultsDatabaseImpl {
        
    /**
     * Maps a keyword to a list of playloads. A playload is a map from
     * {@link String} to {@link String} where the keys are taking <u>only</u>
     * from {@link SimpleSpecialResultsDatabaseImpl#Attr}.
     */
    private final Map<String,List<Map<String,String>>> keywords2nameValuePairs 
        = new HashMap<String,List<Map<String,String>>>();
    
    /**
     * Constructs with the passed in database, conforming to this format:
     * <a name="ctor"></a>
     * <pre>
     * S     ::= Line*
     * Line  ::= Term [ '\t' Term ]* '|' Key '=' Value [ '\t' Key '=' Value ]*
     * Term  ::= String
     * Key   ::= String
     * Value ::= String
     * </pre>
     * @param in
     */
    BasicSpecialResultsDatabaseImpl(LimeXMLDocumentFactory limeXMLDocumentFactory, String in) {
        super(limeXMLDocumentFactory);
        createDB(in);
    }
    
    /**
     * Constructs with no database.
     */
    BasicSpecialResultsDatabaseImpl(LimeXMLDocumentFactory limeXMLDocumentFactory) {
        this(limeXMLDocumentFactory,"");
    }
    
    /**
     * Clears and reloads using the input database.
     */
    public final synchronized void reload(String in) {
        clearDB();
        createDB(in);
    }
    
    /**
     * Returns <code>true</code> if there are no entries yet.
     * 
     * @return <code>true</code> if there are no entries yet
     */
    public final boolean isEmpty() {
        return keywords2nameValuePairs.isEmpty();
    }     
    
    /**
     * @return lines containing keys and then the next line containing the sets of values., e.g.
     * <pre>
     * cat
     *  {a=b, b=c}
     * dog
     *  {1=2, 2=3}
     * </pre>
     */
    public String toString() {
        StringBuffer res = new StringBuffer();
        for (Map.Entry<String,List<Map<String,String>>> e : keywords2nameValuePairs.entrySet()) {
            String term = e.getKey();
            List<Map<String,String>> maps = e.getValue();
            res.append(term).append("\n");
            for (Map<String,String> map : maps) {
                res.append(" ").append(map).append("\n");
            }
        }
        return res.toString();
    }
    
    @Override
    protected final List<Map<String, String>> getSearchResults(String query) {
        List<Map<String, String>> res = new ArrayList<Map<String, String>>();
        for (String keyword : keywords2nameValuePairs.keySet()) {
            if (query.equalsIgnoreCase(keyword)) {
                List<Map<String,String>> xmls = keywords2nameValuePairs.get(keyword);
                res.addAll(xmls);
            }
        }
        return res;
    }  
    
    /**
     * Removes all entries from {@link #keywords2nameValuePairs}.
     */
    private void clearDB() {
        keywords2nameValuePairs.clear();
    }
    
    /**
     * The grammar is given in the <a href="#ctor">constructor</a>.
     *
     * @param inputStream
     * @throws IOException 
     */
    private void createDB(String input) {
        for (StringTokenizer t = new StringTokenizer(input, "\n", false); t.hasMoreTokens();) {
            String line = t.nextToken();
            String[] termsAndPayload = line.split("\\|");
            if(termsAndPayload.length >= 2) {
                String terms = termsAndPayload[0];
                String payload = termsAndPayload[1];
                Map<String, String> keysAndValues = new HashMap<String, String>();
                for (StringTokenizer st = new StringTokenizer(payload, "\t", false); st.hasMoreTokens();) {
                    String token = st.nextToken();
                    int equalIdx = token.indexOf('=');
                    if(equalIdx > 0 && equalIdx < token.length() - 1) {
                        String key = token.substring(0, equalIdx);
                        String value = token.substring(equalIdx + 1);
                        keysAndValues.put(key, value);
                    }
                }
                for (StringTokenizer st = new StringTokenizer(terms, "\t", false); st.hasMoreTokens();) {
                    String term = st.nextToken().trim();
                    List<Map<String, String>> lst = keywords2nameValuePairs.get(term);
                    if (lst == null)
                        keywords2nameValuePairs.put(term, lst = new ArrayList<Map<String, String>>());
                    lst.add(keysAndValues);
                }
            }
        }
    }     

}
