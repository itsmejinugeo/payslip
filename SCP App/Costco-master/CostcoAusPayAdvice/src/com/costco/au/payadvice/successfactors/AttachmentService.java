package com.costco.au.payadvice.successfactors;

import java.util.Date;

import com.costco.au.payadvice.exceptions.PayAdviceNotFoundException;
import com.costco.au.payadvice.successfactors.pojo.Attachment;
/**
 * This interface provides method to efficiently retrieve an attachment from SuccessFactors EC
 */
public interface AttachmentService {
    /**
     * Read attachment
     *
     * @return Attachment 
     * @param externalCode Payslip External Code
     * @param employeeId Employee Number
     * @param effectiveStartDate Effective Start Date
     * @throws PayAdviceNotFoundException No pay advice
     */	
	Attachment readAttachmentDetails(String externalCode, String employeeId, Date effectiveStartDate) throws PayAdviceNotFoundException;
	
    /**
     * Retrieve attachment content
     *
     * @return Content 
     * @param attachmentId Attachment Id
     */	
	byte[] retrieveAttachmentContent(String attachmentId);
}
