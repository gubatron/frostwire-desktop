package com.limegroup.gnutella.gui.search.tests;

import com.frostwire.JsonEngine;

public class SignedMessage {
    public final byte[] unsignedData;
    public final byte[] signature;
    
    
    public final String base32DataString;
    
    public SignedMessage() {
        unsignedData = null;
        signature = null;
        base32DataString = null;
    }
    
    public SignedMessage(final byte[] unsignedData, final byte[] signature) {
        this.unsignedData = unsignedData;
        this.signature = signature;
        this.base32DataString = Base32.encode(unsignedData);
    }

    /**
     * Returns the JSON String that represents this object converted to bytes.
     * @return
     */
    public byte[] toBytes() {
        return new JsonEngine().toJson(this).getBytes();
    }
    
    /**
     * 
     * @param json
     * @return
     */
    public static SignedMessage fromBytes(byte[] jsonBytes) {
        String json = new String(jsonBytes);
        return new JsonEngine().toObject(json, SignedMessage.class);
    }
}