package com.costco.eeterm.odata.odatav2.pojo;

/**
 * This POJO is for OData UPSERT response details
 */
public class UpsertResponseDetail {
	
	private String key;
	private String status;
	private String editStatus;
	private String message;
	private long index;
	private int httpCode;
	private String inlineResults;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getEditStatus() {
		return editStatus;
	}
	public void setEditStatus(String editStatus) {
		this.editStatus = editStatus;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public long getIndex() {
		return index;
	}
	public void setIndex(long index) {
		this.index = index;
	}
	public int getHttpCode() {
		return httpCode;
	}
	public void setHttpCode(int httpCode) {
		this.httpCode = httpCode;
	}
	public String getInlineResults() {
		return inlineResults;
	}
	public void setInlineResults(String inlineResults) {
		this.inlineResults = inlineResults;
	}

}
