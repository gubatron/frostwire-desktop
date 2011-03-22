package org.limewire.io;

/** Thrown when invalid data is parsed. */
public class InvalidDataException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -5289507507603167161L;

    public InvalidDataException() {
        super();
    }
    
    public InvalidDataException(String msg) {
        super(msg);
    }
    
    public InvalidDataException(Throwable cause) {
        super(cause);
    }
    
    public InvalidDataException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
}
