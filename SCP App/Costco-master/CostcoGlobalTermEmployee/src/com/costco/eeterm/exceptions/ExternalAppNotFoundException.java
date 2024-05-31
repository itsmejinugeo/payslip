package com.costco.eeterm.exceptions;

public class ExternalAppNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public ExternalAppNotFoundException(String message) {
        super(message);
    }

}
