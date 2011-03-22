package org.limewire.http;

import org.apache.http.ProtocolException;

/**
 * Signals that a header does not have the correct format.
 */
public class MalformedHeaderException extends ProtocolException {

    /**
     * 
     */
    private static final long serialVersionUID = 3922391811793665254L;

    public MalformedHeaderException() {
        super();
    }

    public MalformedHeaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedHeaderException(String message) {
        super(message);
    }

}
