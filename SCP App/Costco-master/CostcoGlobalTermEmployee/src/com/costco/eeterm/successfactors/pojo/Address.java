package com.costco.eeterm.successfactors.pojo;

import java.util.Date;
/**
 * This POJO is for Address
 */
public class Address implements Cloneable {

	private String personIdExternal;
	private Date startDate;
	private String addressType;
	private String stateId;
	private String address1;
	private String city;
	private String country;
	private String zipcode;
	private String stateUri;
	private String state;
	
	public String getStateId() {
		return stateId;
	}
	public void setStateId(String stateId) {
		this.stateId = stateId;
	}
	public String getStateUri() {
		return stateUri;
	}
	public void setStateUri(String stateUri) {
		this.stateUri = stateUri;
	}
	public String getZipcode() {
		return zipcode;
	}
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
	public Date getStartDate() {
		return startDate == null ? null : (Date) startDate.clone();
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate == null ? null : (Date) startDate.clone();
	}
	public String getPersonIdExternal() {
		return personIdExternal;
	}
	public void setPersonIdExternal(String personIdExternal) {
		this.personIdExternal = personIdExternal;
	}
	public String getAddressType() {
		return addressType;
	}
	public void setAddressType(String addressType) {
		this.addressType = addressType;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getAddress1() {
		return address1;
	}
	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}

}
