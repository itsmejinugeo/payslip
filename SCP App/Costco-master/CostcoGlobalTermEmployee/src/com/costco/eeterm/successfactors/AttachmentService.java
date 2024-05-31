package com.costco.eeterm.successfactors;

import java.util.Date;

import com.costco.eeterm.exceptions.AttachmentNotFoundException;
import com.costco.eeterm.exceptions.ErrorReadingAttachmentException;
import com.costco.eeterm.exceptions.ErrorRetrievingAttachmentContentException;
import com.costco.eeterm.successfactors.pojo.Attachment;
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
     * @throws AttachmentNotFoundException
     * @throws ErrorReadingAttachmentException
     */	
	Attachment readAttachmentDetails(String externalCode, String employeeId, Date effectiveStartDate) 
	        throws AttachmentNotFoundException, ErrorReadingAttachmentException;
	
    /**
     * Retrieve attachment content
     *
     * @return Content 
     * @param attachmentId Attachment Id
     * @throws ErrorRetrievingAttachmentContentException
     */	
	byte[] retrieveAttachmentContent(String attachmentId) throws ErrorRetrievingAttachmentContentException;
}
