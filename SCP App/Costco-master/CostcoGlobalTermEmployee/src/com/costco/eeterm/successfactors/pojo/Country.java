package com.costco.eeterm.successfactors.pojo;

import java.util.List;
import java.util.Locale;
/**
 * This POJO is for Country picklist
 */
public class Country implements Comparable<Country> {


	private String code;
	private String countryName;
	private List<PickList> stateList;
	

	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getCountryName() {
		return countryName;
	}
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public List<PickList> getStateList() {
		return stateList;
	}
	public void setStateList(List<PickList> stateList) {
		this.stateList = stateList;
	}
	@Override
	public int compareTo(Country o) {
		return this.countryName == null ? -1 : this.countryName.toUpperCase(Locale.ENGLISH)
				.compareTo(o.getCountryName().toUpperCase(Locale.ENGLISH));
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((countryName == null) ? 0 : countryName.hashCode());
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((stateList == null) ? 0 : stateList.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Country other = (Country) obj;
		if (countryName == null) {
			if (other.countryName != null)
				return false;
		} else if (!countryName.equals(other.countryName))
			return false;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (stateList == null) {
			if (other.stateList != null)
				return false;
		} else if (!stateList.equals(other.stateList))
			return false;

		return true;
	}
	
	
	
}

