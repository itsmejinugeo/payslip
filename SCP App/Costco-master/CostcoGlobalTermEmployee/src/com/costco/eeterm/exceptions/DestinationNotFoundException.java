package com.costco.eeterm.exceptions;

public class DestinationNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public DestinationNotFoundException() {
        super();
    }
    
    public DestinationNotFoundException(String message) {
        super(message);
    }
    
    public DestinationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
