package com.costco.eeterm.exceptions;

public class ErrorReadingAttachmentException extends Exception {

    private static final long serialVersionUID = 1L;

    public ErrorReadingAttachmentException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
