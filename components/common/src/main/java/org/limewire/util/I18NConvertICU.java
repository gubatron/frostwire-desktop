package org.limewire.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import sun.text.normalizer.NormalizerImpl;
import sun.text.normalizer.UnicodeSet;

/**
 * Removes accents and symbols, and normalizes strings.
 * 
 */
final class I18NConvertICU extends AbstractI18NConverter {

    /** excluded codepoints (like accents) */
    private java.util.BitSet _excluded;
    /** certain chars to be replaced by space (like commas, etc) */
    private java.util.BitSet _replaceWithSpace;
    private Map<?, ?> _cMap;

    /**
     * initializer:
     * this subclass of AbstractI18NConverter uses the icu4j's 
     * pacakges to normalize Strings.  
     * _excluded and _replaceWithSpace (BitSet) are read in from
     * files created by UDataFileCreator and are used to 
     * remove accents, etc. and replace certain code points with
     * ascii space (\u0020)
     */
    I18NConvertICU()
        throws IOException, ClassNotFoundException {
    	java.util.BitSet bs = null;
        java.util.BitSet bs2 = null;
    	Map<?, ?> hm = null;

        InputStream fi = CommonUtils.getResourceStream("org/limewire/util/excluded.dat");
        //read in the explusion bitset
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(fi));
        bs = (java.util.BitSet)ois.readObject();
        ois.close();
        
        fi = CommonUtils.getResourceStream("org/limewire/util/caseMap.dat");
        //read in the case map
        ois = new ConverterObjectInputStream(new BufferedInputStream(fi));
        hm = (HashMap<?, ?>)ois.readObject();
        ois.close();
        
        fi = CommonUtils.getResourceStream("org/limewire/util/replaceSpace.dat");
        ois = new ObjectInputStream(new BufferedInputStream(fi));
        bs2 = (java.util.BitSet)ois.readObject();
        ois.close();

    	_excluded = bs;
    	_cMap = hm;
        _replaceWithSpace = bs2;
    }
    
    /**
     * Return the converted form of the string s
     * this method will also split the s into the different
     * unicode blocks
     * @param s String to be converted
     * @return the converted string
     */
    public String getNorm(String s) {
        return convert(s);
    } 
    
    /**
     * Simple composition of a String.
     */
    public String compose(String s) {
        return compose(s, false);
    }
    
    /**
     * convert the string into NFKC + removal of accents, symbols, etc.
     * uses icu4j's Normalizer to first decompose to NFKD form,
     * then removes all codepoints in the exclusion BitSet 
     * finally composes to NFC and adds spaces '\u0020' between
     * different unicode blocks
     *
     * @param String to convert
     * @return converted String
     */
    private String convert(String s) {
    	//decompose to NFKD
    	String nfkd = decompose(s, true);
        StringBuilder buf = new StringBuilder();
    	int len = nfkd.length();
    	String lower;
    	char c;
    
    	//loop through the string and check for excluded chars
    	//and lower case if necessary
    	for(int i = 0; i < len; i++) {
    	    c = nfkd.charAt(i);
            if(_replaceWithSpace.get(c)) {
                buf.append(" ");
            }
    	    else if(!_excluded.get(c)) {
                lower = (String)_cMap.get(String.valueOf(c));
                if(lower != null)
                    buf.append(lower);
                else
                    buf.append(c);
    	    }
    	}
    	
    	//compose to nfc and split
    	return blockSplit(compose(buf.toString(), false));
    }

    ///////////////////////////////////////////
    // From icu4j Normalizer
    /////////////////////////////////////////////
    private static final int MAX_BUF_SIZE_COMPOSE = 2;
    private static final int MAX_BUF_SIZE_DECOMPOSE = 3;
    
    /**
     * Decompose a string.
     * The string will be decomposed to according the the specified mode.
     * @param str       The string to decompose.
     * @param compat    If true the string will be decomposed accoding to NFKD 
     *                   rules and if false will be decomposed according to NFD 
     *                   rules.
     * @return String   The decomposed string  
     * @draft ICU 2.2 
     */         
    public static String decompose(String str, boolean compat){
       return decompose(str,compat,0);                  
    }
    
    /**
     * Decompose a string.
     * The string will be decomposed to according the the specified mode.
     * @param str     The string to decompose.
     * @param compat  If true the string will be decomposed accoding to NFKD 
     *                 rules and if false will be decomposed according to NFD 
     *                 rules.
     * @param options The normalization options, ORed together (0 for no options).
     * @return String The decomposed string 
     * @draft ICU 2.6
     */         
    public static String decompose(String str, boolean compat, int options){
        
        char[] dest = new char[str.length()*MAX_BUF_SIZE_DECOMPOSE];
        int[] trailCC = new int[1];
        int destSize=0;
        UnicodeSet nx = NormalizerImpl.getNX(options);
        for(;;){
            destSize=NormalizerImpl.decompose(str.toCharArray(),0,str.length(),
                                              dest,0,dest.length,
                                              compat,trailCC, nx);
            if(destSize<=dest.length){
                return new String(dest,0,destSize); 
            }else{
                dest = new char[destSize];
            }
        } 
                
    }
    
    /**
     * Compose a string.
     * The string will be composed to according the the specified mode.
     * @param str        The string to compose.
     * @param compat     If true the string will be composed accoding to 
     *                    NFKC rules and if false will be composed according to 
     *                    NFC rules.
     * @return String    The composed string   
     * @draft ICU 2.2
     */            
    public static String compose(String str, boolean compat){
         return compose(str,compat,0);           
    }
    
    /**
     * Compose a string.
     * The string will be composed to according the the specified mode.
     * @param str        The string to compose.
     * @param compat     If true the string will be composed accoding to 
     *                    NFKC rules and if false will be composed according to 
     *                    NFC rules.
     * @param options    The only recognized option is UNICODE_3_2
     * @return String    The composed string   
     * @draft ICU 2.6
     */            
    public static String compose(String str, boolean compat, int options){
           
        char[] dest = new char[str.length()*MAX_BUF_SIZE_COMPOSE];
        int destSize=0;
        char[] src = str.toCharArray();
        UnicodeSet nx = NormalizerImpl.getNX(options);
        for(;;){
            destSize=NormalizerImpl.compose(src,0,src.length,
                                            dest,0,dest.length,compat ? NormalizerImpl.OPTIONS_COMPAT : 0,
                                            nx);
            if(destSize<=dest.length){
                return new String(dest,0,destSize);  
            }else{
                dest = new char[destSize];
            }
        }                   
    }
}





