package com.costco.eeterm.exceptions;
/**
 * Custom exception for paydvice
 */
public class PayAdviceNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public PayAdviceNotFoundException() {}
	
	public PayAdviceNotFoundException(String message) {
	    super(message);
	}
	
	public PayAdviceNotFoundException(Throwable cause) {
	    super(cause);
	}

}
