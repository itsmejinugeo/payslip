package com.costco.eeterm.odata.odatav2.pojo;

import java.util.List;
/**
 * This POJO is for OData UPSERT response
 */
public class UpsertResponse {
	
	private List<UpsertResponseDetail> d;

	public List<UpsertResponseDetail> getD() {
		return d;
	}

	public void setD(List<UpsertResponseDetail> d) {
		this.d = d;
	}

	
}
