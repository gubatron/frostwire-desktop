package org.limewire.nio;

class InterruptedIOException extends java.io.InterruptedIOException {
    

    /**
     * 
     */
    private static final long serialVersionUID = 1747132209501296250L;

    InterruptedIOException(InterruptedException ix) {
        super();
        initCause(ix);
    }
    
}    