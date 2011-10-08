package org.limewire.util;

/**
 * Interface that provides lookup based on string.
 */
public interface StringLookup {
    /**
     * @return value for certain key.
     * A return value of null means there is no such key.
     */
    public String lookup(String key);
}
