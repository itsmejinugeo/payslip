package com.costco.eeterm.exceptions;
/**
 * Custom exception for Unauthorized user
 */
public class UserNotAuthorisedException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String apiAccessed;
	
	public UserNotAuthorisedException(String message) {
	    super(message);
	}
	
	public UserNotAuthorisedException(String message, String apiAccessed) {
        super(message);
        this.apiAccessed = apiAccessed;
    }

    public String getApiAccessed() {
        return apiAccessed;
    }

}
