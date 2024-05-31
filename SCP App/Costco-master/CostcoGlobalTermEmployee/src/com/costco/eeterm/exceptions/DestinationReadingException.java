package com.costco.eeterm.exceptions;

public class DestinationReadingException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public DestinationReadingException() {
        super();
    }
    
    public DestinationReadingException(String message) {
        super(message);
    }
    
    public DestinationReadingException(String message, Throwable cause) {
        super(message, cause);
    }

}
