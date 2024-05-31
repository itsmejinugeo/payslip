package com.costco.au.payadvice.successfactors;

import java.util.Date;
import java.util.List;

import com.costco.au.payadvice.exceptions.PayAdviceNotFoundException;
import com.costco.au.payadvice.successfactors.pojo.Attachment;
import com.costco.au.payadvice.successfactors.pojo.PayAdvice;

/**
 * This interface provides methods to retrieve employee's pay advices
 */
public interface PayAdviceService {
	/**
	 * Retrieves all the pay davices for a given employee
	 * 
	 * @return list of pay advices
	 * @param employeeId Employee Id
	 * @throws PayAdviceNotFoundException
	 *             pay advices not found for an employee
	 */
	List<PayAdvice> getEmployeePayAdvice(String employeeId) throws PayAdviceNotFoundException;
	
	/**
	 * Retrieves attachment for a given pay advice detail
	 * 
	 * @param externalCode Payslip External Code
	 * @param effectiveStartDate Effective Start Date
	 * @param userId Employee Number
	 * @return attachment details for a given pay advice
	 * @throws PayAdviceNotFoundException  pay advices not found for an employee
	 */
	Attachment getAttachment(String externalCode, Date effectiveStartDate, String userId) throws PayAdviceNotFoundException;
	
}
