package com.frostwire.util;

public class SignedMessage {
    public final byte[] unsignedData;
    public final byte[] signedData;
    public final byte[] signature;
    public final String signedString;
    
    public SignedMessage(final byte[] unsignedData, final byte[] signedData, final byte[] signature) {
        this.unsignedData = unsignedData;
        this.signedData = signedData;
        this.signature = signature;
        this.signedString = Base32.encode(signedData);
    }
}