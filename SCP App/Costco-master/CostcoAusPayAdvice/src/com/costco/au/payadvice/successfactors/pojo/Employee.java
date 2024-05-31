package com.costco.au.payadvice.successfactors.pojo;
/**
 * This POJO is for employee details
 */
public class Employee {

	private String firstName;
	private String lastName;
    private String userId;
    private String companyName;
    private String profilePhotoURL;
    private String defaultFullName;
    private String positionTitle;
    private String location;
    private String department;
    private String jobCode;
    
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
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getJobCode() {
		return jobCode;
	}
	public void setJobCode(String jobCode) {
		this.jobCode = jobCode;
	}
	public String getPositionTitle() {
		return positionTitle;
	}
	public void setPositionTitle(String positionTitle) {
		this.positionTitle = positionTitle;
	}
	
    
    
}
