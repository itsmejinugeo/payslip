package com.costco.eeterm.successfactors;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a method to refresh of all the picklist in the cache
 */
public class PicklistRefresh implements Callable<Boolean> {
	/**
     * Refresh all picklists
     *
     * @param addressService EC picklists service object
     */
	public PicklistRefresh(AddressPicklistService addressService) {
		super();
		this.addressService = addressService;
	}

	private static final Logger logger = LoggerFactory.getLogger(PicklistRefresh.class);

	private AddressPicklistService addressService;


	/**
	  * {@inheritDoc}
	  */
	@Override
	public Boolean call() throws Exception {

		logger.info("Refreshing picklists");

		addressService.clearAllCachePicklist();
		addressService.getAddressPicklist();

		return true;
	}

}
