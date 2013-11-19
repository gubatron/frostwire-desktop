package com.frostwire.util;

public class SignedMessage {
    public final byte[] unsignedData;
    public final byte[] signature;
    public final String base32DataString;
    
    public SignedMessage(final byte[] unsignedData, final byte[] signature) {
        this.unsignedData = unsignedData;
        this.signature = signature;
        this.base32DataString = Base32.encode(unsignedData);
    }
}