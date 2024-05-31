package com.costco.au.payadvice.successfactors;

import javax.naming.NamingException;

import com.costco.au.payadvice.successfactors.pojo.ProfilePhotoDetails;
/**
 * This interface provides method to efficiently retrieve SF EC Profile photo URL
 */
public interface ProfilePhotoService {
	/**
	 * Get profile photo URL
	 * 
	 * @return URL
	 * @throws NamingException
	 *             Configuration not found in context
	 */
	ProfilePhotoDetails getProfilePhotoURL() throws NamingException;

}
