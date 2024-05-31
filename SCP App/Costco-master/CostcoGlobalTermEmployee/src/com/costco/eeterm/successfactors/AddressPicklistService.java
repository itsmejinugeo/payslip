package com.costco.eeterm.successfactors;

import com.costco.eeterm.exceptions.ErrorGettingAddressPicklistException;
import com.costco.eeterm.successfactors.pojo.AddressPicklist;

/**
 * This interface provides methods to retrieve employee's pay advices
 */
public interface AddressPicklistService {
	/**
	 * Retrieves country and state picklist for address
	 * 
	 * @return picklist Country with state picklist
	 * @throws ErrorGettingAddressPicklistException 
	 */
	AddressPicklist getAddressPicklist() throws ErrorGettingAddressPicklistException;
	
	/**
	 * Clear all picklist cache
	 */
	void clearAllCachePicklist();

}
