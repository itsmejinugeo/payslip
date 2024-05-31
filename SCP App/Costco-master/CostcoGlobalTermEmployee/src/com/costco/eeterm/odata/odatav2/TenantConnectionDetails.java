package com.costco.eeterm.odata.odatav2;

import org.apache.olingo.odata2.api.edm.Edm;

import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

/**
 * This class provides method to efficiently handles tenant connection details
 */
public class TenantConnectionDetails {

	private Edm edm;
	private String url;
	private DestinationConfiguration destConfiguration;

	/**
	 * Get Entity Data Model
	 *
	 * @return {@link Edm}
	 */
	public Edm getEdm() {
		return edm;
	}

	/**
	 * Set Entity Data Model
	 *
	 * @param edm
	 *            {@link Edm}
	 */
	public void setEdm(Edm edm) {
		this.edm = edm;
	}

	/**
	 * Get URL
	 *
	 * @return URL
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Set URL
	 *
	 * @param url
	 *            URL
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Get estination configuration
	 *
	 * @return {@link DestinationConfiguration}
	 */
	public DestinationConfiguration getDestConfiguration() {
		return destConfiguration;
	}

	/**
	 * Set destination configuration
	 *
	 * @param destConfiguration
	 *            {@link DestinationConfiguration}
	 */
	public void setDestConfiguration(DestinationConfiguration destConfiguration) {
		this.destConfiguration = destConfiguration;
	}


}
