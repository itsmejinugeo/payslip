package com.costco.eeterm.exceptions;

public class ErrorGettingAddressPicklistException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public ErrorGettingAddressPicklistException(Throwable cause) {
        super(cause);
    }
    
    public ErrorGettingAddressPicklistException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
