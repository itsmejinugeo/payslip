package com.costco.au.payadvice.successfactors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.costco.au.payadvice.exceptions.UserNotFoundException;
import com.costco.au.payadvice.odata.odatav2.ODataClientService;
import com.costco.au.payadvice.successfactors.pojo.Employee;
import com.costco.au.payadvice.successfactors.pojo.EmployeeCentralConstant;

/**
 * {@inheritDoc}
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

	@Autowired
	ODataClientService oDataService;

	@Autowired
	ProfilePhotoService profilePhotoService;

	private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

	private static final String INACTIVE = "INACTIVE";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Employee getEmployeeData(String userId) throws UserNotFoundException {

		logger.info("Started retrieve of user's own details");
		ODataFeed oDataFeed = null;
		Employee employeeData = new Employee();
		try {
			oDataFeed = oDataService.readFeed(EmployeeCentralConstant.EMPJOB_TABLE, expandFields(),
					filterUserIdField(userId), selectEmpJobFields(), null, 0, true);
			employeeData = oDataEntrytoEmployeeData(oDataFeed, userId);
			//check if employee is terminated
			if(employeeData.getJobCode() == null){
				//terminated employee. get basic details
				oDataFeed = oDataService.readFeed(EmployeeCentralConstant.USER_TABLE, null, filterUserIdField(userId) + filterStatusField(),
						selectLiveSearchFields(), null, 0, true);
				Employee termEmployee = oDataEntrytoTerminatedEmployee(oDataFeed);
				employeeData.setDefaultFullName(termEmployee.getDefaultFullName());
				employeeData.setPositionTitle(INACTIVE);
			}
			

		} catch (NamingException | IOException | ODataException e) {
			logger.error("something went wrong attempting read SuccessFactors.", e);
			// String errorMessage = e.getMessage();

		}
		logger.info("Finished retrieve of user's own details");

		logger.info("Generate profile photo url");
		try {
			employeeData.setProfilePhotoURL(buildProfilePhotoURL(userId));
		} catch (NamingException e) {
			logger.error("something went wrong attempting to retrieve profile photo url.", e);
			// String errorMessage = e.getMessage();
		}
		return employeeData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Employee> findEmployeeByUserId(String userId) {
		
		ODataFeed oDataFeed = null;
		List<Employee> employeeList = new ArrayList<Employee>();
		
		try {
			oDataFeed = oDataService.readFeed(EmployeeCentralConstant.USER_TABLE, null, filterUserIdField(userId) + filterStatusField(),
					selectLiveSearchFields(), null, 0, true);
			employeeList = oDataEntrytoLiveResultEmployee(oDataFeed);
//			Employee employeeData = oDataEntrytoUserIdSearch(oDataFeed);
//			employeeList.add(employeeData);

		} catch (NamingException | IOException | ODataException e) {
			logger.error("Something went wrong attempting read SuccessFactors.", e);
			// String errorMessage = e.getMessage();
		}
		return employeeList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Employee> findEmployeeByName(String filter) {

		ODataFeed oDataFeed = null;
		List<Employee> employeeList = new ArrayList<Employee>();
		try {
			oDataFeed = oDataService.readFeed(EmployeeCentralConstant.USER_TABLE, null,
					filterByName(filter.toUpperCase()), selectLiveSearchFields(), null, 0, true);
			employeeList = oDataEntrytoLiveResultEmployee(oDataFeed);

//			String inputStr= "";
//			List<String> testList= new ArrayList<String>();
//			boolean test = testList.parallelStream().allMatch(inputStr::contains);
			
		} catch (NamingException | IOException | ODataException e) {
			logger.error("Something went wrong attempting read SuccessFactors.", e);
		}
		return employeeList;
	}

	private String buildProfilePhotoURL(String userId) throws NamingException {
		String profilePhotoURL;
		try {
			profilePhotoURL = profilePhotoService.getProfilePhotoURL().getUrl() + userId;
		} catch (NamingException e) {
			throw e;
		}

		return profilePhotoURL;
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
				if(entryUser != null){
				entryNavDetails = entryUser.getProperties();
				employeeData.setFirstName((String) entryNavDetails.get(EmployeeCentralConstant.USER_FIRSTNAME));
				employeeData.setLastName((String) entryNavDetails.get(EmployeeCentralConstant.USER_LASTNAME));
				employeeData.setDefaultFullName((String) entryNavDetails.get(EmployeeCentralConstant.USER_DEFAULTNAME));
				employeeData.setLocation((String) entryNavDetails.get(EmployeeCentralConstant.USER_LOCATION));
				employeeData.setDepartment((String) entryNavDetails.get(EmployeeCentralConstant.USER_DEPARTMENT));
				employeeData.setJobCode((String) entryNavDetails.get(EmployeeCentralConstant.USER_JOBCODE));
				employeeData.setPositionTitle((String) entryNavDetails.get(EmployeeCentralConstant.USER_TITLE));
				}

				// COMPANYNAV
				ODataEntry entryCompany = (ODataEntry) entryDetails.get(EmployeeCentralConstant.COMPANY_NAV);
				entryNavDetails = entryCompany.getProperties();
				employeeData.setCompanyName((String) entryNavDetails.get(EmployeeCentralConstant.COMPANY_NAME));

			}
		}

		return employeeData;
	}

	private String selectEmpJobFields() {
		String fields = EmployeeCentralConstant.USER_ID + "," + EmployeeCentralConstant.USER_NAV + "/"
				+ EmployeeCentralConstant.USER_FIRSTNAME + "," + EmployeeCentralConstant.USER_NAV + "/"
				+ EmployeeCentralConstant.USER_LASTNAME + "," + EmployeeCentralConstant.USER_NAV + "/"
				+ EmployeeCentralConstant.USER_DEFAULTNAME + "," + EmployeeCentralConstant.USER_NAV + "/"
				+ EmployeeCentralConstant.USER_TITLE + "," + EmployeeCentralConstant.USER_NAV + "/"
				+ EmployeeCentralConstant.USER_JOBCODE + "," + EmployeeCentralConstant.USER_NAV + "/"
				+ EmployeeCentralConstant.USER_LOCATION + "," + EmployeeCentralConstant.USER_NAV + "/"
				+ EmployeeCentralConstant.USER_DEPARTMENT + "," + EmployeeCentralConstant.COMPANY_NAV + "/"
				+ EmployeeCentralConstant.COMPANY_NAME;
		return fields;
	}

	private String filterUserIdField(String userId) {
		String field = EmployeeCentralConstant.USER_ID + " eq " + "'" + userId + "'";
		return field;
	}

	private String filterStatusField() {
		//and ( status eq 'inactive' or status eq 'active' )
		String field = " and ( status eq 'inactive' or status eq 'active' )";
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
				String userId = (String) entryDetails.get(EmployeeCentralConstant.USER_EMPID);
				if(userId != null){
				resultList.add(result);
				}
			}
		}

		return resultList;
	}

	private Employee oDataEntrytoTerminatedEmployee(ODataFeed feed) {
		Employee result = new Employee();
		if (feed != null) {
			for (ODataEntry entry : feed.getEntries()) {

				Map<String, Object> entryDetails = entry.getProperties();
				result.setDefaultFullName((String) entryDetails.get(EmployeeCentralConstant.USER_DEFAULTNAME));
				result.setUserId((String) entryDetails.get(EmployeeCentralConstant.USER_ID));
			
			}
		}

		return result;
	}
	
	private String selectLiveSearchFields() {
		return EmployeeCentralConstant.USER_DEFAULTNAME + "," + EmployeeCentralConstant.USER_ID + "," + EmployeeCentralConstant.USER_EMPID;
	}

	private String filterStartsWithField(String filter) {
		// eg: startswith(firstName, 'Jas') or startswith(lastName, 'Jas')
		String field = EmployeeCentralConstant.FILTER_STARTSWITH + "(" + EmployeeCentralConstant.FILTER_TOUPPER + "("
				+ EmployeeCentralConstant.USER_FIRSTNAME + ")," + "'" + filter + "'" + ")" + " or "
				+ EmployeeCentralConstant.FILTER_STARTSWITH + "(" + EmployeeCentralConstant.FILTER_TOUPPER + "("
				+ EmployeeCentralConstant.USER_LASTNAME + ")," + "'" + filter + "'" + ")";
		return field;
	}

	private String filterByName(String filter) {
		// eg: (empInfo/jobInfoNav/company eq '0087' and status eq 't') and
		// (startswith(toupper(firstName),'WILL') or
		// startswith(toupper(lastName),'WILL') or
		// startswith(toupper(firstName),'RYAN') or
		// startswith(toupper(lastName),'RYAN'))
		String filterName = "";
		filter = filter.replaceAll("%20", " "); // replace decoded space
		String[] filters = filter.split(" ");
		for (String entry : filters) {
			if (filterName == "") {
				filterName = filterStartsWithField(entry);
			} else {
				filterName = filterName + " or " + filterStartsWithField(entry);
			}
		}

		String field = filterAusEmployee() + " and (" + filterName + ")" + filterStatusField();

		return field;
	}

	@SuppressWarnings("unused")
	private String filterActiveAusEmployee() {
		// eg: (empInfo/jobInfoNav/company eq '0087' and status eq 't')
		String field = "(" + EmployeeCentralConstant.USER_EMPINFO + "/" + EmployeeCentralConstant.JOBINFONAV + "/"
				+ EmployeeCentralConstant.COMPANY + " eq '" + EmployeeCentralConstant.COMPANY_AUS + "' and "
				+ EmployeeCentralConstant.STATUS + " eq '" + EmployeeCentralConstant.STATUS_ACTIVE + "')";
		return field;
	}

	private String filterAusEmployee() {
		// eg: (empInfo/jobInfoNav/company eq '0087' and status eq 't')
		String field = "(" + EmployeeCentralConstant.USER_EMPINFO + "/" + EmployeeCentralConstant.JOBINFONAV + "/"
				+ EmployeeCentralConstant.COMPANY + " eq '" + EmployeeCentralConstant.COMPANY_AUS + "'";
		return field;
	}
}
