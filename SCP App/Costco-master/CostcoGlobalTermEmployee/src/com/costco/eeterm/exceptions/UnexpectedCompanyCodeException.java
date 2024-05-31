package com.costco.eeterm.exceptions;

public class UnexpectedCompanyCodeException extends Exception {

    private static final long serialVersionUID = 1L;

    public UnexpectedCompanyCodeException() {}

    public UnexpectedCompanyCodeException(String message) {
        super(message);
    }

    public UnexpectedCompanyCodeException(Throwable cause) {
        super(cause);
    }

    public UnexpectedCompanyCodeException(String message, Throwable cause) {
        super(message, cause);
    }

}
