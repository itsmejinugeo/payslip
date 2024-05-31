package com.costco.eeterm.successfactors;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.olingo.odata2.api.ep.entry.EntryMetadata;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.costco.eeterm.exceptions.ErrorGettingEmployeeDataException;
import com.costco.eeterm.exceptions.RequestHandlingException;
import com.costco.eeterm.exceptions.UnexpectedCompanyCodeException;
import com.costco.eeterm.exceptions.UserNotFoundException;
import com.costco.eeterm.odata.odatav2.ODataClientService;
import com.costco.eeterm.successfactors.pojo.Address;
import com.costco.eeterm.successfactors.pojo.Employee;
import com.costco.eeterm.successfactors.pojo.EmployeeCentralConstant;

/**
 * {@inheritDoc}
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

	@Autowired
	ODataClientService oDataService;

	@Autowired
	ProfilePhotoService profilePhotoService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Employee getEmployeeData(String userId) throws UserNotFoundException, ErrorGettingEmployeeDataException {
		try {
		    Employee employeeData = new Employee();
			// terminated employee. get basic details
			ODataFeed oDataFeedUser = oDataService.readFeed(EmployeeCentralConstant.USER_TABLE, null,
					filterUserIdField(userId) + filterStatusField(), selectEmployeeBasicFields(), null, 0, true);
			employeeData = oDataEntrytoTerminatedEmployee(oDataFeedUser);

			// query address
			ODataFeed oDataFeedAddress = oDataService.readFeed(EmployeeCentralConstant.ADDRESS_TABLE,
					EmployeeCentralConstant.ADDRESS_STATENAV, filterPersonIdField(employeeData.getPersonIdExternal()), null, null, 0, true);
			Address address = oDataEntrytoAddressEmployee(oDataFeedAddress);
			employeeData.setAddress(address);
			employeeData.setProfilePhotoURL(buildProfilePhotoURL(userId));
			
			return employeeData;
		} catch (UserNotFoundException e) {
		    throw new UserNotFoundException("User(" + userId + ") doesn't exist in SuccessFactors.");
		}
		catch (NamingException | IOException | ODataException e) {
		    throw new ErrorGettingEmployeeDataException(
		            "Error reading SuccessFactors.", e);
		}
	}

	private Address oDataEntrytoAddressEmployee(ODataFeed feed) {
		Address result = new Address();

		if (feed != null) {
			for (ODataEntry entry : feed.getEntries()) {

				Map<String, Object> entryDetails = entry.getProperties();
				result.setPersonIdExternal((String) entryDetails.get(EmployeeCentralConstant.ADDRESS_PERSONID));
				result.setAddressType((String) entryDetails.get(EmployeeCentralConstant.ADDRESS_TYPE));
				result.setAddress1((String) entryDetails.get(EmployeeCentralConstant.ADDRESS_ADDRESS1));
				result.setCity((String) entryDetails.get(EmployeeCentralConstant.ADDRESS_CITY));
				result.setCountry((String) entryDetails.get(EmployeeCentralConstant.ADDRESS_COUNTRY));
				result.setStateId((String) entryDetails.get(EmployeeCentralConstant.ADDRESS_STATE));
				result.setZipcode((String) entryDetails.get(EmployeeCentralConstant.ADDRESS_ZIPCODE));
				Calendar startDate = (Calendar) entryDetails.get(EmployeeCentralConstant.ADDRESS_STARTDATE);
				result.setStartDate(startDate.getTime());

				// STATENAV
				Map<String, Object> entryNavDetails = null;
				ODataEntry entryState = (ODataEntry) entryDetails.get(EmployeeCentralConstant.ADDRESS_STATENAV);

				if (entryState != null) {
					EntryMetadata entryMetadata = entryState.getMetadata();
					result.setStateUri(entryMetadata.getUri());

					entryNavDetails = entryState.getProperties();
					result.setState((String) entryNavDetails.get(EmployeeCentralConstant.ADDRESS_STATE_EXTERNALCODE));
				}
			}
		}

		return result;
	}

	private String filterPersonIdField(String userId) {
		String field = EmployeeCentralConstant.ADDRESS_PERSONID + " eq " + "'" + userId + "'";
		return field;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Employee> findEmployeeByUserId(String userId) throws ErrorGettingEmployeeDataException {
		try {
		    ODataFeed oDataFeed = null;
		    List<Employee> employeeList = new ArrayList<Employee>();
			oDataFeed = oDataService.readFeed(EmployeeCentralConstant.USER_TABLE, null, filterUserIdField(userId),
					selectLiveSearchFields(), null, 0, true);
			employeeList = oDataEntrytoLiveResultEmployee(oDataFeed);
			return employeeList;
		} catch (NamingException | IOException | ODataException e) {
		    throw new ErrorGettingEmployeeDataException("Error reading SuccessFactors.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Employee> findActiveAusEmployeeByName(String filter) throws ErrorGettingEmployeeDataException{
		try {
		    ODataFeed oDataFeed = null;
		    List<Employee> employeeList = new ArrayList<Employee>();
			oDataFeed = oDataService.readFeed(EmployeeCentralConstant.USER_TABLE, null,
					filterByNameNActiveAusEmployee(filter.toUpperCase()), selectLiveSearchFields(), null, 0, true);
			employeeList = oDataEntrytoLiveResultEmployee(oDataFeed);
			return employeeList;
		} catch (NamingException | IOException | ODataException e) {
		    throw new ErrorGettingEmployeeDataException("Error reading SuccessFactors.", e);
		}
	}

	private String buildProfilePhotoURL(String userId) {
		try {
		    return profilePhotoService.getProfilePhotoURL().getUrl() + userId;
		} catch (NamingException e) {
			return "";
		}
	}

	private String expandFields() {
		String fields = EmployeeCentralConstant.USER_NAV + "," + EmployeeCentralConstant.COMPANY_NAV;
		return fields;
	}

	private Employee oDataEntrytoEmployeeData(ODataFeed feed, String userId) {
		Employee employeeData = new Employee();

		if (feed != null) {
			for (ODataEntry entry : feed.getEntries()) {

				Map<String, Object> entryDetails = entry.getProperties();
				Map<String, Object> entryNavDetails = null;

				// EMPJOB
				employeeData.setUserId((String) entryDetails.get(EmployeeCentralConstant.USER_ID));

				// USERNAV
				ODataEntry entryUser = (ODataEntry) entryDetails.get(EmployeeCentralConstant.USER_NAV);
				entryNavDetails = entryUser.getProperties();
				employeeData.setFirstName((String) entryNavDetails.get(EmployeeCentralConstant.USER_FIRSTNAME));
				employeeData.setLastName((String) entryNavDetails.get(EmployeeCentralConstant.USER_LASTNAME));
				employeeData.setDefaultFullName((String) entryNavDetails.get(EmployeeCentralConstant.USER_DEFAULTNAME));

				// COMPANYNAV
				ODataEntry entryCompany = (ODataEntry) entryDetails.get(EmployeeCentralConstant.COMPANY_NAV);
				entryNavDetails = entryCompany.getProperties();
				employeeData.setCompanyName((String) entryNavDetails.get(EmployeeCentralConstant.COMPANY_NAME));

			}
		}

		return employeeData;
	}

	private String selectEmpJobFields() {
		String fields = EmployeeCentralConstant.USER_ID + "," 
		        + EmployeeCentralConstant.USER_NAV + "/" + EmployeeCentralConstant.USER_FIRSTNAME + "," 
		        + EmployeeCentralConstant.USER_NAV + "/" + EmployeeCentralConstant.USER_LASTNAME + "," 
		        + EmployeeCentralConstant.USER_NAV + "/" + EmployeeCentralConstant.USER_DEFAULTNAME + "," 
		        + EmployeeCentralConstant.USER_NAV + "/" + EmployeeCentralConstant.USER_TITLE + "," 
		        + EmployeeCentralConstant.USER_NAV + "/" + EmployeeCentralConstant.USER_JOBCODE + "," 
		        + EmployeeCentralConstant.USER_NAV + "/" + EmployeeCentralConstant.USER_LOCATION + "," 
		        + EmployeeCentralConstant.USER_NAV + "/" + EmployeeCentralConstant.USER_DEPARTMENT + "," 
		        + EmployeeCentralConstant.COMPANY_NAV + "/" + EmployeeCentralConstant.COMPANY_NAME;
		return fields;
	}

	private String filterUserIdField(String userId) {
		String field = EmployeeCentralConstant.USER_ID + " eq " + "'" + userId + "'";
		return field;
	}

	private List<Employee> oDataEntrytoLiveResultEmployee(ODataFeed feed) {
		List<Employee> resultList = new ArrayList<Employee>();

		if (feed != null) {
			for (ODataEntry entry : feed.getEntries()) {

				Employee result = new Employee();

				Map<String, Object> entryDetails = entry.getProperties();
				result.setDefaultFullName((String) entryDetails.get(EmployeeCentralConstant.USER_DEFAULTNAME));
				result.setUserId((String) entryDetails.get(EmployeeCentralConstant.USER_ID));
				
				// resolve company code and country code based on company code
				ODataEntry empInfo = 
				        (ODataEntry)entryDetails.get(EmployeeCentralConstant.USER_EMPINFO);
				ODataFeed jobInfoNav = 
				        (ODataFeed)empInfo.getProperties().get(EmployeeCentralConstant.JOBINFONAV);
				List<ODataEntry> jobInfoEntries = jobInfoNav.getEntries();
				if (!jobInfoEntries.isEmpty()) {
				    String companyCode = (String)jobInfoEntries.get(0).getProperties()
				                .get(EmployeeCentralConstant.COMPANY);
				    result.setCompanyCode(companyCode);
				    try {
				        result.setCountryCode(getCountryCodeFromCompanyCode(companyCode));				        
				    } catch (UnexpectedCompanyCodeException e) {
				        result.setCountryCode("N/A");
				    }
				}
				
				String empId = (String) entryDetails.get(EmployeeCentralConstant.USER_EMPID);
				if (empId != null) {
					result.setEmpId(empId);
				    result.setProfilePhotoURL(buildProfilePhotoURL(result.getUserId()));
					resultList.add(result);
				}
			}
		}

		return resultList;
	}
	
	private String getCountryCodeFromCompanyCode(String companyCode) 
	        throws UnexpectedCompanyCodeException {
	    switch(companyCode) {
    	    case EmployeeCentralConstant.COMPANY_CAN_1:
	        case EmployeeCentralConstant.COMPANY_CAN_2:
	            return "CAN";
	        case EmployeeCentralConstant.COMPANY_AUS:
	            return "AUS";
	        default:
	            throw new UnexpectedCompanyCodeException("Received unexpected company code " 
	                    + companyCode + " when search employee");
	    }
	}

	private String selectLiveSearchFields() {
		return EmployeeCentralConstant.USER_DEFAULTNAME + "," 
		        + EmployeeCentralConstant.USER_ID + ","
				+ EmployeeCentralConstant.USER_EMPID;
	}

	private String filterNameStartsWith(String filter) {
		// eg: startswith(firstName, 'Jas') or startswith(lastName, 'Jas')
		String field = EmployeeCentralConstant.FILTER_STARTSWITH + "(" + EmployeeCentralConstant.FILTER_TOUPPER + "("
				+ EmployeeCentralConstant.USER_FIRSTNAME + ")," + "'" + filter + "'" + ")" + " or "
				+ EmployeeCentralConstant.FILTER_STARTSWITH + "(" + EmployeeCentralConstant.FILTER_TOUPPER + "("
				+ EmployeeCentralConstant.USER_LASTNAME + ")," + "'" + filter + "'" + ")";
		return field;
	}

	private String filterByNameNActiveAusEmployee(String filter) {
		// eg: (empInfo/jobInfoNav/company eq '0087' and status eq 't') and
		// (startswith(toupper(firstName),'WILL') or
		// startswith(toupper(lastName),'WILL') or
		// startswith(toupper(firstName),'RYAN') or
		// startswith(toupper(lastName),'RYAN'))
		String filterName = filterByName(filter);
		String field = filterActiveAusEmployee() + " and (" + filterName + ")";
		return field;
	}
	
	private String filterByName(String name) {
	    String filter = "";
        name = name.replaceAll("%20", " "); // replace decoded space
        String[] nameParts = name.split(" ");
        for (String namePart : nameParts) {
            if (filter == "") {
                filter = filterNameStartsWith(namePart);
            } else {
                filter = filter + " or " + filterNameStartsWith(namePart);
            }
        }
        return "(" + filter + ")";
	}

	private String filterActiveAusEmployee() {
		// eg: (empInfo/jobInfoNav/company eq '0087' and status eq 't')
		String field = "(" + EmployeeCentralConstant.USER_EMPINFO + "/" + EmployeeCentralConstant.JOBINFONAV + "/"
				+ EmployeeCentralConstant.COMPANY + " eq '" + EmployeeCentralConstant.COMPANY_AUS + "' and "
				+ EmployeeCentralConstant.STATUS + " eq '" + EmployeeCentralConstant.STATUS_ACTIVE + "')";
		return field;
	}
	
	private String filterByEmployeeStatus(String status) {
	    String filter = "(" + EmployeeCentralConstant.STATUS + " eq '" + status + "')";
        return filter;
	}
	
	private String filterStatusField() {
		// and ( status eq 'inactive' or status eq 'active' )
		String field = " and ( status eq 'inactive' or status eq 'active' )";
		return field;
	}

	private String selectEmployeeBasicFields() {
		return EmployeeCentralConstant.USER_DEFAULTNAME + "," + EmployeeCentralConstant.USER_ID + ","
				+ EmployeeCentralConstant.USER_EMPID;
	}

	private Employee oDataEntrytoTerminatedEmployee(ODataFeed feed) throws UserNotFoundException {
	    if (feed == null || feed.getEntries().size() == 0) {
	        throw new UserNotFoundException();
	    }
		Employee employee = new Employee();
		for (ODataEntry entry : feed.getEntries()) {   
		    Map<String, Object> entryDetails = entry.getProperties();
		    employee.setDefaultFullName((String) entryDetails.get(EmployeeCentralConstant.USER_DEFAULTNAME));
		    employee.setUserId((String) entryDetails.get(EmployeeCentralConstant.USER_ID));
		    employee.setPersonIdExternal((String) entryDetails.get(EmployeeCentralConstant.USER_EMPID));
		}

		return employee;
	}

	@Override
	public void updateEmloyeeAddress(Address address) throws RequestHandlingException {
		Map<String, Object> addressMap = new HashMap<String, Object>();
		addressMap.put("startDate", new Date());
		addressMap.put("personIdExternal", address.getPersonIdExternal());
		addressMap.put("addressType", address.getAddressType());
		addressMap.put("address1", address.getAddress1());
		addressMap.put("city", address.getCity());
		addressMap.put("zipCode", address.getZipcode());
		addressMap.put("country", address.getCountry());
		addressMap.put("state", address.getStateId());

		try {
			oDataService.upsertEntry(null, EmployeeCentralConstant.ADDRESS_TABLE, addressMap);
		} catch (URISyntaxException | IOException | NamingException | ODataException e) {
			String errorMsgTrimmed = trimErrorMessage(e, "Upsert error:");
			throw new RequestHandlingException(errorMsgTrimmed, e);
		}
	}

	private String trimErrorMessage(Exception e, String prefix) {

		String errorMsg = e.getMessage();

		if (prefix != null) {
			errorMsg = errorMsg.replaceFirst("^" + prefix, "");
		}

		String errorMsgTrimmed = errorMsg;

		String[] lines = errorMsg.split("\\r?\\n");
		for (String line : lines) {
			errorMsgTrimmed = line;
			break;
		}

		return errorMsgTrimmed;
	}

    @Override
    public List<Employee> findTerminatedAusNCanEmployeesByName(String name) 
            throws ErrorGettingEmployeeDataException {
        List<Employee> employeeList = new ArrayList<Employee>();
        try {
            ODataFeed oDataFeed = oDataService.readFeed(EmployeeCentralConstant.USER_TABLE, 
                    expandJobInfoNav(),
                    filterByNameNInactiveStatusNAusOrCan(name.toUpperCase()), 
                    selectLiveSearchFiledsWithCompany(), null, 0, true);
            employeeList = oDataEntrytoLiveResultEmployee(oDataFeed);;
        } catch (NamingException | IOException | ODataException e) {
            throw new ErrorGettingEmployeeDataException("Error reading SuccessFactors.", e);
        }
        return employeeList;
    }
    
    private String expandJobInfoNav() {
        return EmployeeCentralConstant.USER_EMPINFO + "/" + EmployeeCentralConstant.JOBINFONAV;
    }
    
    private String filterByNameNInactiveStatusNAusOrCan(String name) {
        String nameFilter = filterByName(name);
        String inactiveStatusFilter = 
                filterByEmployeeStatus(EmployeeCentralConstant.STATUS_INACTIVE);
        String countryFilter = filterByAusOrCan();
        String filter = "(" + nameFilter + " and " + inactiveStatusFilter + " and " + countryFilter + ")";
        return filter;
    }
    
    private String filterByAusOrCan() {
        String ausFilter = filterByCompany(EmployeeCentralConstant.COMPANY_AUS);
        String canFilter1 = filterByCompany(EmployeeCentralConstant.COMPANY_CAN_1);
        String canFilter2 = filterByCompany(EmployeeCentralConstant.COMPANY_CAN_1);
        String filter = "(" + ausFilter + " or " + canFilter1 + " or " + canFilter2 + ")";
        return filter;
    }
    
    private String filterByCompany(String company) {
        String filter = "(" + EmployeeCentralConstant.USER_EMPINFO + "/" 
                + EmployeeCentralConstant.JOBINFONAV + "/"
                + EmployeeCentralConstant.COMPANY + " eq '" + company + "')";
        return filter;
    }
    
    private String selectLiveSearchFiledsWithCompany() {
        String companyField = companyFieldFromUser();
        return selectLiveSearchFields() + "," + companyField;
    }
    
    private String companyFieldFromUser() {
        String field = EmployeeCentralConstant.USER_EMPINFO + "/" 
                + EmployeeCentralConstant.JOBINFONAV + "/" 
                + EmployeeCentralConstant.COMPANY;
        return field;
    }
    
}
