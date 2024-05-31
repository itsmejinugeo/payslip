package com.costco.eeterm.successfactors;

import java.util.List;

import com.costco.eeterm.exceptions.ErrorGettingEmployeeDataException;
import com.costco.eeterm.exceptions.RequestHandlingException;
import com.costco.eeterm.exceptions.UserNotFoundException;
import com.costco.eeterm.successfactors.pojo.Address;
import com.costco.eeterm.successfactors.pojo.Employee;
/**
 * This interface provides method to efficiently reads employee information from SuccessFactors EC
 */
public interface EmployeeService {
    /**
     * Get Employee Data
     *
     * @return Employee 
     * @param userId Employee's User Id
     * @throws UserNotFoundException Invalid user
     * @throws ErrorGettingEmployeeDataException
     */	
	Employee getEmployeeData(String userId) 
	        throws UserNotFoundException, ErrorGettingEmployeeDataException;
	
    /**
     * Find Active Aus Employee by Name
     *
     * @return List of employees
     * @param filter Filter for employee name
     * @throws ErrorGettingEmployeeDataException
     */	
	List<Employee> findActiveAusEmployeeByName(String filter) 
	        throws ErrorGettingEmployeeDataException;
	
    /**
     * Find Employee by User ID
     *
     * @return List of employees
     * @param userId Filter for user ID
     * @throws ErrorGettingEmployeeDataException
     */	
	List<Employee> findEmployeeByUserId(String userId) throws ErrorGettingEmployeeDataException;
	
    /**
     * Update Employee Address
     *
     * @param address Employee's Address
     * @throws RequestHandlingException Request Error
     */	
	void updateEmloyeeAddress(Address address) throws RequestHandlingException;
	
	/**
	 * Find terminated aus and can employee by name
	 * @param name employee name
	 * @return Employees with name containing given name
	 * @throws ErrorGettingEmployeeDataException
	 */
	List<Employee> findTerminatedAusNCanEmployeesByName(String name) 
	        throws ErrorGettingEmployeeDataException;
}
