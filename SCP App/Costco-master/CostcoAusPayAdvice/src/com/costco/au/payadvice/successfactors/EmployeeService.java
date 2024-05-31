package com.costco.au.payadvice.successfactors;

import java.util.List;

import com.costco.au.payadvice.exceptions.UserNotFoundException;
import com.costco.au.payadvice.successfactors.pojo.Employee;
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
     */	
	Employee getEmployeeData(String userId) throws UserNotFoundException;
	
    /**
     * Find Employee by Name
     *
     * @return List of employees
     * @param filter Filter for employee name
     */	
	List<Employee> findEmployeeByName(String filter);
	
    /**
     * Find Employee by User ID
     *
     * @return List of employees
     * @param userId Filter for user ID
     */	
	List<Employee> findEmployeeByUserId(String userId);
	
}
