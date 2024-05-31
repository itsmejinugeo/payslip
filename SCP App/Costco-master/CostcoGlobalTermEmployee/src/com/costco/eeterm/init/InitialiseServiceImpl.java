package com.costco.eeterm.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.costco.eeterm.successfactors.PicklistCacheTenantUpdate;

/**
 * {@inheritDoc}
 */
@Service
public class InitialiseServiceImpl implements InitialiseService {

	private static final Logger logger = LoggerFactory.getLogger(InitialiseService.class);

	@Autowired
	PicklistCacheTenantUpdate picklistCacheTenantUpdateService;


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialise() {

		logger.info("Initialising application");
		picklistCacheTenantUpdateService.refreshPicklists();
	}

}
