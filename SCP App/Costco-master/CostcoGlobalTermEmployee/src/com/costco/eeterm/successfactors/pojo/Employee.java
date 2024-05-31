package com.costco.eeterm.successfactors.pojo;
/**
 * This POJO is for employee details
 */
public class Employee {

	private String firstName;
	private String lastName;
    private String userId;
    private String personIdExternal;
    private String companyCode;
    private String companyName;
    private String profilePhotoURL;
    private String defaultFullName;
    private Address address;
	private String countryCode;
	private String empId;
    
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getProfilePhotoURL() {
		return profilePhotoURL;
	}
	public void setProfilePhotoURL(String profilePhotoURL) {
		this.profilePhotoURL = profilePhotoURL;
	}
	public String getDefaultFullName() {
		return defaultFullName;
	}
	public void setDefaultFullName(String defaultFullName) {
		this.defaultFullName = defaultFullName;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public String getPersonIdExternal() {
		return personIdExternal;
	}
	public void setPersonIdExternal(String personIdExternal) {
		this.personIdExternal = personIdExternal;
	}
    public String getCompanyCode() {
        return companyCode;
    }
    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }
    public String getCountryCode() {
        return countryCode;
    }
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
	}
	public String getEmpId() {
		return empId;
	}
	public void setEmpId(String empId) {
		this.empId = empId;
	}

}
