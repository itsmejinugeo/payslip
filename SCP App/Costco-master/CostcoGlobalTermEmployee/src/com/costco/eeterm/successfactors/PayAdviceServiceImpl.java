package com.costco.eeterm.successfactors;

import java.io.IOException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.costco.eeterm.exceptions.AttachmentNotFoundException;
import com.costco.eeterm.exceptions.EmptyODataFeedException;
import com.costco.eeterm.exceptions.ErrorGettingPayAdviceException;
import com.costco.eeterm.exceptions.ErrorReadingAttachmentException;
import com.costco.eeterm.exceptions.NullODataFeedException;
import com.costco.eeterm.exceptions.PayAdviceNotFoundException;
import com.costco.eeterm.odata.odatav2.ODataClientService;
import com.costco.eeterm.successfactors.pojo.Attachment;
import com.costco.eeterm.successfactors.pojo.EmployeeCentralConstant;
import com.costco.eeterm.successfactors.pojo.PayAdvice;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * {@inheritDoc}
 */
@Service
public class PayAdviceServiceImpl implements PayAdviceService {

	@Autowired
	ODataClientService oDataService;

	@Autowired
	AttachmentService attachmentService;

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
    @Override
	public List<PayAdvice> getEmployeePayAdvice(String employeeId) 
	        throws PayAdviceNotFoundException, ErrorGettingPayAdviceException {
		try {
		    Date fromDate = getDate(1900, Calendar.JANUARY, 1, 0, 0, 0);
		    Date toDate = getDate(9999, Calendar.JANUARY, 1, 0, 0, 0);
			ODataFeed oDataFeed = oDataService.readFeed(EmployeeCentralConstant.PAYADVICE_TABLE,
					EmployeeCentralConstant.ATTACHMENT_NAV, filterEmployeeIdField(employeeId), selectPayAdviceFields(),
					fromDate, toDate);

			List<PayAdvice> payAdviceList = oDataFeedtoEmployeePayAdviceData(oDataFeed);
			return payAdviceList;
		} catch (NullODataFeedException | EmptyODataFeedException e) {
		    return Collections.emptyList();
        }  catch (NamingException | IOException | ODataException e) {
		    throw new ErrorGettingPayAdviceException("Error reading SF.", e);
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

	private List<PayAdvice> oDataFeedtoEmployeePayAdviceData(ODataFeed feed) 
	        throws NullODataFeedException, EmptyODataFeedException {
	    if (feed == null) {
	        throw new NullODataFeedException();
	    }
	    if (feed.getEntries().size() == 0) {
	        throw new EmptyODataFeedException();
	    }
		List<PayAdvice> employeePayAdviceList = new ArrayList<PayAdvice>();
		for (ODataEntry entry : feed.getEntries()) {
		    PayAdvice employeePayAdviceData = oDataEntrytoEmployeePayAdviceData(entry);
		    employeePayAdviceList.add(employeePayAdviceData);
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

	private String convertDateToPayslipDate(Date date) {
		SimpleDateFormat localformatter = new SimpleDateFormat("dd-MM-YYYY");
		// sydney = TimeZone.getTimeZone("Australia/Sydney");
		// localformatter.setTimeZone(sydney);
		String localDateTimeString = localformatter.format(date);
		return localDateTimeString;
	}
	
	private Date getDate(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
	    return new Calendar.Builder()
	            .setDate(year, month, dayOfMonth)
	            .setTimeOfDay(hourOfDay, minute, second)
	            .build()
	            .getTime();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attachment getAttachment(String externalCode, Date effectiveStartDate, String userId)
			throws PayAdviceNotFoundException, ErrorReadingAttachmentException {
        try {
            Attachment attachment = 
                    attachmentService.readAttachmentDetails(externalCode, userId, effectiveStartDate);
            return attachment;
        } catch (AttachmentNotFoundException e) {
            throw new PayAdviceNotFoundException();
        }
	}

}
