package com.costco.eeterm.exceptions;

public class ErrorGettingEmployeeDataException extends Exception {

    private static final long serialVersionUID = 1L;

    public ErrorGettingEmployeeDataException() {}

    public ErrorGettingEmployeeDataException(String message) {
        super(message);
    }

    public ErrorGettingEmployeeDataException(Throwable cause) {
        super(cause);
    }

    public ErrorGettingEmployeeDataException(String message, Throwable cause) {
        super(message, cause);
    }

}
