package com.costco.eeterm.exceptions;
/**
 * Custom exception for position request handling error
 */
public class RequestHandlingException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public RequestHandlingException(String message) {
        super(message);
    }
    
    public RequestHandlingException(Throwable cause) {
        super(cause);
    }
    
	public RequestHandlingException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
