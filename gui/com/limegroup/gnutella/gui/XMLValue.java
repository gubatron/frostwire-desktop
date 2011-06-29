package com.limegroup.gnutella.gui;

import com.limegroup.gnutella.SchemaFieldInfo;

// Marks an object as being an XMLValue for tables.
public class XMLValue implements Comparable<XMLValue> {
    
    private final String value;
    private final SchemaFieldInfo sfi;
    
    public XMLValue(String value, SchemaFieldInfo sfi) {
        this.value = value;
        this.sfi = sfi;
    }
    
    public String getValue() {
        return value;
    }
    
    public SchemaFieldInfo getSchemaFieldInfo() {
        return sfi;
    }
    
    public int hashCode() {
        return value.hashCode();
    }
    
    public boolean equals(Object o) {
        if(o instanceof XMLValue)
            return value.equals(((XMLValue)o).value);
        else
            return false;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public int compareTo(XMLValue other) {
        if(other == null)
            return 1;
        
        Comparable a = XMLUtils.getComparable(sfi, value);
        Comparable b = XMLUtils.getComparable(other.sfi, other.value);
        if(a == null && b == null)
            return 0;
        else if(b == null)
            return 1;
        else if(a == null)
            return -1;
        else if((a instanceof String) && (b instanceof String))
            return ((String)a).compareToIgnoreCase((String)b);
        else
            return a.compareTo(b);
    }
}
