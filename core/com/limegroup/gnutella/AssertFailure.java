package com.limegroup.gnutella;

public class AssertFailure extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -896876449448208134L;

	public AssertFailure(String message) {
        super(message);
    }

    public AssertFailure(Throwable cause) {
        super(cause);
    }

    public AssertFailure(String message, Throwable cause) {
        super(message, cause);
    }
}
