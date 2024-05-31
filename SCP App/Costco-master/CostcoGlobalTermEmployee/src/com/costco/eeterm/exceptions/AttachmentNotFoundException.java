package com.costco.eeterm.exceptions;

public class AttachmentNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public AttachmentNotFoundException() {
        super();
    }
    
    public AttachmentNotFoundException(String message) {
        super(message);
    }
    
    public AttachmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
