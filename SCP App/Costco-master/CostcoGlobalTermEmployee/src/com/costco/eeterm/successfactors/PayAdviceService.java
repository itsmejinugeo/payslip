package com.costco.eeterm.successfactors;

import java.util.Date;
import java.util.List;

import com.costco.eeterm.exceptions.ErrorGettingPayAdviceException;
import com.costco.eeterm.exceptions.ErrorReadingAttachmentException;
import com.costco.eeterm.exceptions.PayAdviceNotFoundException;
import com.costco.eeterm.successfactors.pojo.Attachment;
import com.costco.eeterm.successfactors.pojo.PayAdvice;

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
	 * @throws ErrorGettingPayAdviceException
	 */
	List<PayAdvice> getEmployeePayAdvice(String employeeId) 
	        throws PayAdviceNotFoundException, ErrorGettingPayAdviceException;
	
	/**
	 * Retrieves attachment for a given pay advice detail
	 * 
	 * @param externalCode Payslip External Code
	 * @param effectiveStartDate Effective Start Date
	 * @param userId Employee Number
	 * @return attachment details for a given pay advice
	 * @throws PayAdviceNotFoundException  pay advices not found for an employee
	 * @throws ErrorReadingAttachmentException
	 */
	Attachment getAttachment(String externalCode, Date effectiveStartDate, String userId) 
	        throws PayAdviceNotFoundException, ErrorReadingAttachmentException;
	
}
