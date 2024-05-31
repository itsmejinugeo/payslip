package com.costco.eeterm.successfactors;

import java.util.Date;
import java.util.Map;

import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.costco.eeterm.exceptions.AttachmentNotFoundException;
import com.costco.eeterm.exceptions.EmptyODataFeedException;
import com.costco.eeterm.exceptions.ErrorReadingAttachmentException;
import com.costco.eeterm.exceptions.ErrorRetrievingAttachmentContentException;
import com.costco.eeterm.exceptions.NullODataFeedException;
import com.costco.eeterm.odata.odatav2.ODataClientService;
import com.costco.eeterm.successfactors.pojo.Attachment;
import com.costco.eeterm.successfactors.pojo.EmployeeCentralConstant;

/**
 * {@inheritDoc}
 */
@Service
public class AttachmentServiceImpl implements AttachmentService {

	@Autowired
	ODataClientService oDataService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attachment readAttachmentDetails(String externalCode,String employeeId,
			Date effectiveStartDate) throws AttachmentNotFoundException, ErrorReadingAttachmentException {
		try {
			ODataFeed attachmentFeed = oDataService.readFeed(EmployeeCentralConstant.PAYADVICE_TABLE,
					EmployeeCentralConstant.ATTACHMENT_NAV, setFilter(externalCode,employeeId), selectBasicFields(),
					effectiveStartDate);
			Attachment attachment = mapEntryToAttachment(attachmentFeed);
			return attachment;
		} catch(EmptyODataFeedException | NullODataFeedException e) {
		    throw new AttachmentNotFoundException("Attachment couldn't be found from SF.", e);
		} catch (Exception e) {
		    throw new ErrorReadingAttachmentException(
		            "Error reading attachment details from SuccessFactors.", e);
		}
	}

	private String setFilter(String externalCode, String employeeId) {
		return EmployeeCentralConstant.PAYADVICE_EMPLOYEEID + " eq '" + employeeId + "' and " + EmployeeCentralConstant.PAYADVICE_EXTERNALCODE + " eq '" + externalCode + "'";
	}


	private Attachment mapEntryToAttachment(ODataFeed feed) 
	        throws NullODataFeedException, EmptyODataFeedException {
	    if (feed == null) {
	        throw new NullODataFeedException();
	    }
	    if (feed.getEntries().size() == 0) {
	        throw new EmptyODataFeedException();
	    }
		Attachment attachment = new Attachment();
		for (ODataEntry entry : feed.getEntries()) {
		    Map<String, Object> entryDetails = entry.getProperties();
		    ODataEntry entryNav;
		    entryNav = (ODataEntry) entryDetails.get(EmployeeCentralConstant.ATTACHMENT_NAV);
		    if (entryNav != null) {
		        // entryMetadata = entryNav.getMetadata();
		        // position.setCustResourceTypeUri((String)
		        // entryMetadata.getUri());
		        Map<String, Object> entryNavDetails = entryNav.getProperties();
		        
		        Long id = (Long) entryNavDetails.get(EmployeeCentralConstant.ATTACHMENT_ID);
		        attachment.setAttachmentId(String.valueOf(id));
		        attachment.setFilename((String) entryNavDetails.get(EmployeeCentralConstant.ATTACHMENT_FILENAME));
		        // attachment.setMimeType((String)
		        // entryDetails.get(EmployeeCentralConstant.ATTACHMENT_MIMETYPE));
		        // attachment.setFileExtension((String)
		        // entryDetails.get(EmployeeCentralConstant.ATTACHMENT_EXTENSION));
		        // attachment.setFileSize((String)
		        // entryDetails.get(EmployeeCentralConstant.ATTACHMENT_FILESIZE));
		        byte[] document = (byte[]) entryNavDetails.get(EmployeeCentralConstant.ATTACHMENT_CONTENT);
		        // attachment.setFileContent(Base64.encodeBase64String(document));
		        attachment.setFileContent(document);
		    }
		}
		return attachment;
	}

	private String selectBasicFields() {
		return EmployeeCentralConstant.ATTACHMENT_NAV + "/" + EmployeeCentralConstant.ATTACHMENT_ID + ","
				+ EmployeeCentralConstant.ATTACHMENT_NAV + "/" + EmployeeCentralConstant.ATTACHMENT_CONTENT + ","
				+ EmployeeCentralConstant.ATTACHMENT_NAV + "/" + EmployeeCentralConstant.ATTACHMENT_FILENAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] retrieveAttachmentContent(String attachmentId) throws ErrorRetrievingAttachmentContentException {
		try {
			ODataEntry attachmentEntry = oDataService.readEntry(EmployeeCentralConstant.ATTACHMENT_CONTENT,
					attachmentId, null, selectContent());
			byte[] document = (byte[]) mapEntryToAttachmentContent(attachmentEntry);
			return document;
		} catch (Exception e) {
		    throw new ErrorRetrievingAttachmentContentException(
		            "Error reading attachment details from SuccessFactors.", e);
		}
	}

	private Object mapEntryToAttachmentContent(ODataEntry entry) {

		Map<String, Object> entryDetails = entry.getProperties();

		byte[] document = (byte[]) entryDetails.get(EmployeeCentralConstant.ATTACHMENT_CONTENT);

		return document;
	}

	private String selectContent() {
		return EmployeeCentralConstant.ATTACHMENT_CONTENT + "," + EmployeeCentralConstant.ATTACHMENT_ID;
	}
}
