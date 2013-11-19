package com.frostwire.util;

public class SignedMessage {
    public final byte[] unsignedData;
    public final byte[] signedHashBytes;
    public final byte[] signature;
    public final String signedHashString;
    
    public SignedMessage(final byte[] unsignedData, final byte[] signedHashBytes, final byte[] signature) {
        this.unsignedData = unsignedData;
        this.signedHashBytes = signedHashBytes;
        this.signature = signature;
        this.signedHashString = Base32.encode(signedHashBytes);
    }
}