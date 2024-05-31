package com.costco.eeterm.successfactors;

import java.util.Collection;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sap.cloud.account.Tenant;
import com.sap.cloud.account.TenantContext;
import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

/**
 * This class provides a method to schedule a refresh of all the picklist in the
 * cache
 */
@Service
public class PicklistCacheTenantUpdateImpl implements PicklistCacheTenantUpdate {

	private static final Logger logger = LoggerFactory.getLogger(PicklistCacheTenantUpdateImpl.class);
	@Autowired
	AddressPicklistService addressService;

	private static final String DESTINATION_NAME_TECH = "sap_hcmcloud_core_odata_technical_user";

	/**
	 * Refresh all picklists
	 */
	@Override
//	@Scheduled(cron = "0 0 6,19 * * *", zone = "Australia/Victoria")
	public void refreshPicklists() {

		Context ctx;
		try {
			ctx = new InitialContext();
			PicklistRefresh picklistRefresh = new PicklistRefresh(addressService);
			TenantContext tenantContext = (TenantContext) ctx.lookup("java:comp/env/TenantContext");

			String currentTenantId = tenantContext.getTenant().getAccount().getId();
			if (currentTenantId.equals("dev_default")) {
				picklistRefresh.call();
			} else {
				// run for all tenants
				Collection<Tenant> allTenants = tenantContext.getSubscribedTenants();

				for (Tenant tenant : allTenants) {
					// don't run in the deployed tenant only the subscribed
					// tenants
					if (!tenant.getId().equals(tenantContext.getTenant().getId())) {

						// check if this tenant has destination we want

						ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx
								.lookup("java:comp/env/connectivityConfiguration");

						DestinationConfiguration destConfiguration = configuration.getConfiguration(tenant.getAccount().getId(),
								DESTINATION_NAME_TECH);
						if (destConfiguration != null) {
							logger.info("Running picklist update for tenant: {}", tenant.getId());
							tenantContext.execute(tenant.getId(), picklistRefresh);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Problem with initializing picklist update", e);

		}
	}
}
