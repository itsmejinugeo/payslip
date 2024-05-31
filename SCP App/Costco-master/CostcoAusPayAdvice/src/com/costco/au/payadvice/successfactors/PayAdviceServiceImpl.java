package com.costco.au.payadvice.successfactors;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

import com.costco.au.payadvice.exceptions.PayAdviceNotFoundException;
import com.costco.au.payadvice.odata.odatav2.ODataClientService;
import com.costco.au.payadvice.successfactors.pojo.Attachment;
import com.costco.au.payadvice.successfactors.pojo.EmployeeCentralConstant;
import com.costco.au.payadvice.successfactors.pojo.PayAdvice;

/**
 * {@inheritDoc}
 */
@Service
public class PayAdviceServiceImpl implements PayAdviceService {

	@Autowired
	ODataClientService oDataService;

	@Autowired
	AttachmentService attachmentService;

	private static final Logger logger = LoggerFactory.getLogger(PayAdviceServiceImpl.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PayAdvice> getEmployeePayAdvice(String employeeId) throws PayAdviceNotFoundException {
		logger.info("Started retrieve of user's pay advice");

		try {
			ODataFeed oDataFeed = oDataService.readFeed(EmployeeCentralConstant.PAYADVICE_TABLE,
					EmployeeCentralConstant.ATTACHMENT_NAV, filterEmployeeIdField(employeeId), selectPayAdviceFields(),
					parseStringToDate("01-01-1900"), parseStringToDate("01-01-9999"));

			List<PayAdvice> payAdviceList = oDataFeedtoEmployeePayAdviceData(oDataFeed);

			logger.info("Finished retrieve of user's pay advice data");
			return payAdviceList;

		} catch (NamingException | IOException | ODataException e) {
			logger.error("Something went wrong attempting read SuccessFactors.", e);
			// String errorMessage = e.getMessage();
			throw new PayAdviceNotFoundException();
		}
	}

	private String selectPayAdviceFields() {
		return EmployeeCentralConstant.PAYADVICE_EMPLOYEEID + ',' + EmployeeCentralConstant.PAYADVICE_EXTERNALCODE + ','
				+ EmployeeCentralConstant.PAYADVICE_ENDPERIOD + ',' + EmployeeCentralConstant.PAYADVICE_EFFECTIVEDATE
				+ ',' + EmployeeCentralConstant.PAYADVICE_EXTERNALNAME + ',' + EmployeeCentralConstant.ATTACHMENT_NAV
				+ "/" + EmployeeCentralConstant.ATTACHMENT_ID + ',' + EmployeeCentralConstant.ATTACHMENT_NAV + "/"
				+ EmployeeCentralConstant.ATTACHMENT_FILENAME;
	}

	private String filterEmployeeIdField(String employeeId) {
		String field = EmployeeCentralConstant.PAYADVICE_EMPLOYEEID + " eq " + "'" + employeeId + "'";
		return field;
	}

	private List<PayAdvice> oDataFeedtoEmployeePayAdviceData(ODataFeed feed) {

		List<PayAdvice> employeePayAdviceList = new ArrayList<PayAdvice>();

		if (feed != null) {
			for (ODataEntry entry : feed.getEntries()) {

				PayAdvice employeePayAdviceData = oDataEntrytoEmployeePayAdviceData(entry);
				employeePayAdviceList.add(employeePayAdviceData);
			}
		}

		return employeePayAdviceList;
	}

	private PayAdvice oDataEntrytoEmployeePayAdviceData(ODataEntry entry) {
		PayAdvice employeePayAdviceData = new PayAdvice();

		Map<String, Object> entryDetails = entry.getProperties();

		// PAY ADVICE
		Calendar effectiveDate = (Calendar) entryDetails.get(EmployeeCentralConstant.PAYADVICE_EFFECTIVEDATE);
		employeePayAdviceData.setEffectiveStartDate((Date) effectiveDate.getTime());
		employeePayAdviceData.setEmployeeId((String) entryDetails.get(EmployeeCentralConstant.PAYADVICE_EMPLOYEEID));
		employeePayAdviceData
				.setExternalCode((String) entryDetails.get(EmployeeCentralConstant.PAYADVICE_EXTERNALCODE));
		employeePayAdviceData
				.setExternalName((String) entryDetails.get(EmployeeCentralConstant.PAYADVICE_EXTERNALNAME));
		Calendar periodEnd = (Calendar) entryDetails.get(EmployeeCentralConstant.PAYADVICE_ENDPERIOD);
		employeePayAdviceData.setCustPeriodEnd(convertDateToPayslipDate((Date) periodEnd.getTime()));

		ODataEntry entryNav;
		entryNav = (ODataEntry) entryDetails.get(EmployeeCentralConstant.ATTACHMENT_NAV);
		if (entryNav != null) {
			// entryMetadata = entryNav.getMetadata();
			// position.setCustResourceTypeUri((String) entryMetadata.getUri());
			Map<String, Object> entryNavDetails = entryNav.getProperties();
			Long attachmentId = (Long) entryNavDetails.get(EmployeeCentralConstant.ATTACHMENT_ID);
			employeePayAdviceData.setAttachmentId(String.valueOf(attachmentId));
			employeePayAdviceData
					.setFilename((String) entryNavDetails.get(EmployeeCentralConstant.ATTACHMENT_FILENAME));
		}
		return employeePayAdviceData;
	}

	// private String convertDateToOData(Date paymentDate) {
	// SimpleDateFormat formatter = new
	// SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	// return formatter.format(paymentDate);
	// }

	private String convertDateToPayslipDate(Date date) {

		SimpleDateFormat localformatter = new SimpleDateFormat("dd-MM-yyyy");
		// sydney = TimeZone.getTimeZone("Australia/Sydney");
		// localformatter.setTimeZone(sydney);
		String localDateTimeString = localformatter.format(date);
		return localDateTimeString;
	}

	private Date parseStringToDate(String formattedDate) {

		Date date;
		try {
			// TimeZone utc = TimeZone.getTimeZone("UTC");
			SimpleDateFormat dateformatter = new SimpleDateFormat("dd-MM-YYYY");
			// dateformatter.setTimeZone(utc);
			date = dateformatter.parse(formattedDate);
		} catch (ParseException e) {
			logger.error("Error converting date", e);
			date = new Date();
		}
		return date;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attachment getAttachment(String externalCode, Date effectiveStartDate, String userId)
			throws PayAdviceNotFoundException {

		Attachment attachment = attachmentService.readAttachmentDetails(externalCode, userId, effectiveStartDate);
		// attachment.setFileContent(attachmentService.retrieveAttachmentContent(attachmentId));

		return attachment;
	}

}
