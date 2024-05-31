package com.costco.au.payadvice.successfactors;

import java.util.Date;
import java.util.Map;

import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.costco.au.payadvice.exceptions.PayAdviceNotFoundException;
import com.costco.au.payadvice.odata.odatav2.ODataClientService;
import com.costco.au.payadvice.successfactors.pojo.Attachment;
import com.costco.au.payadvice.successfactors.pojo.EmployeeCentralConstant;

/**
 * {@inheritDoc}
 */
@Service
public class AttachmentServiceImpl implements AttachmentService {

	private static final Logger logger = LoggerFactory.getLogger(AttachmentServiceImpl.class);

	@Autowired
	ODataClientService oDataService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attachment readAttachmentDetails(String externalCode,String employeeId,
			Date effectiveStartDate) throws PayAdviceNotFoundException {

		try {
			ODataFeed attachmentFeed = oDataService.readFeed(EmployeeCentralConstant.PAYADVICE_TABLE,
					EmployeeCentralConstant.ATTACHMENT_NAV, setFilter(externalCode,employeeId), selectBasicFields(),
					effectiveStartDate);
			Attachment attachment = mapEntryToAttachment(attachmentFeed);

			return attachment;

		} catch (Exception e) {

			logger.error("something went wrong attempting to read attachment details from SuccessFactors.", e);
			String errorMessage = e.getMessage();
			if (errorMessage == null) {
				errorMessage = "";
			}
			throw new PayAdviceNotFoundException();
		}
	}

	private String setFilter(String externalCode, String employeeId) {
		return EmployeeCentralConstant.PAYADVICE_EMPLOYEEID + " eq '" + employeeId + "' and " + EmployeeCentralConstant.PAYADVICE_EXTERNALCODE + " eq '" + externalCode + "'";
	}


	private Attachment mapEntryToAttachment(ODataFeed feed) {
		Attachment attachment = new Attachment();

		if (feed != null) {
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
	public byte[] retrieveAttachmentContent(String attachmentId) {
		byte[] document = new byte[] {};

		try {
			ODataEntry attachmentEntry = oDataService.readEntry(EmployeeCentralConstant.ATTACHMENT_CONTENT,
					attachmentId, null, selectContent());
			document = (byte[]) mapEntryToAttachmentContent(attachmentEntry);

		} catch (Exception e) {

			logger.error("something went wrong attempting to read attachment details from SuccessFactors.", e);
			String errorMessage = e.getMessage();
			if (errorMessage == null) {
				errorMessage = "";
			}
		}

		return document;
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
