package com.costco.au.payadvice.successfactors.pojo;

import java.util.Date;
/**
 * This POJO is for Pay Advice List
 */
public class PayAdvice implements Cloneable {

	private String employeeId;
	private String externalCode;
	private Date effectiveStartDate;
	private String custPeriodEnd;
	private String externalName;
	private String filename;
	private String attachmentId;
	
	public String getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
	public String getExternalCode() {
		return externalCode ;
	}
	public void setExternalCode(String externalCode) {
		this.externalCode = externalCode;
	}

	public Date getEffectiveStartDate() {
		return effectiveStartDate == null ? null : (Date) effectiveStartDate.clone();
	}
	public void setEffectiveStartDate(Date effectiveStartDate) {
		this.effectiveStartDate = effectiveStartDate == null ? null : (Date) effectiveStartDate.clone();
	}

	public String getCustPeriodEnd() {
		return custPeriodEnd;
	}
	public void setCustPeriodEnd(String custPeriodEnd) {
		this.custPeriodEnd = custPeriodEnd;
	}
	public String getExternalName() {
		return externalName;
	}
	public void setExternalName(String externalName) {
		this.externalName = externalName;
	}
	public String getAttachmentId() {
		return attachmentId;
	}
	public void setAttachmentId(String attachmentId) {
		this.attachmentId = attachmentId;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}

}
