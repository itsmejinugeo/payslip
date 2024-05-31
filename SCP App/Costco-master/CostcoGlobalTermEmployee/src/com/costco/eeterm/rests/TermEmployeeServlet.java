package com.costco.eeterm.rests;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.costco.eeterm.constants.ApiValues;
import com.costco.eeterm.exceptions.ErrorGettingAddressPicklistException;
import com.costco.eeterm.exceptions.ErrorGettingEmployeeDataException;
import com.costco.eeterm.exceptions.ErrorGettingPayAdviceException;
import com.costco.eeterm.exceptions.ErrorReadingAttachmentException;
import com.costco.eeterm.exceptions.PayAdviceNotFoundException;
import com.costco.eeterm.exceptions.RequestHandlingException;
import com.costco.eeterm.exceptions.UserNotFoundException;
import com.costco.eeterm.gson.GsonHelper;
import com.costco.eeterm.successfactors.AddressPicklistService;
import com.costco.eeterm.successfactors.EmployeeService;
import com.costco.eeterm.successfactors.PayAdviceService;
import com.costco.eeterm.successfactors.pojo.Address;
import com.costco.eeterm.successfactors.pojo.AddressPicklist;
import com.costco.eeterm.successfactors.pojo.Attachment;
import com.costco.eeterm.successfactors.pojo.Employee;
import com.costco.eeterm.successfactors.pojo.PayAdvice;
import com.costco.eeterm.successfactors.pojo.PayAdviceOverview;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Provides the APIs for pay advices
 * 
 */
@Controller
public class TermEmployeeServlet {
	private static final Logger logger = LoggerFactory.getLogger(TermEmployeeServlet.class);
	
	@Autowired
	PayAdviceService payadviceService;

	@Autowired
	EmployeeService employeeService;
	
	@Autowired
	AddressPicklistService addressPicklistService;

	/**
	 * 
	 * Returns list of payadvices for the current user;
	 * 
	 * @param request HTTP request
	 * @param response HTTP response
	 * @return json Pay Advice list
	 * @throws IOException Exception error
	 * @throws ErrorGettingPayAdviceException 
	 * @throws ErrorGettingEmployeeDataException 
	 */
	@RequestMapping(value = ApiValues.PAYADVICE_LIST, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String getCurrentUserPayAdviceList(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ErrorGettingPayAdviceException, ErrorGettingEmployeeDataException {
		Gson gson = GsonHelper.adaptedGson;

		try {
			PayAdviceOverview overview = new PayAdviceOverview();
			Employee employeeInfo = employeeService.getEmployeeData(request.getRemoteUser());
			List<PayAdvice> payadviceList = 
			        payadviceService.getEmployeePayAdvice(request.getRemoteUser());
			overview = mapToOverview(employeeInfo,payadviceList);
			return gson.toJson(overview);
		} catch (PayAdviceNotFoundException | UserNotFoundException e) {
			response.sendError(404, "No payadvices found for user");
			return null;
		}
	}
	
	private PayAdviceOverview mapToOverview(Employee employeeInfo, List<PayAdvice> payadviceList) {
		
		PayAdviceOverview overview = new PayAdviceOverview();
		overview.setDefaultFullName(employeeInfo.getDefaultFullName());
		overview.setProfilePhotoURL(employeeInfo.getProfilePhotoURL());
		overview.setUserId(employeeInfo.getUserId());
		overview.setPayadviceList(payadviceList);
		
		return overview;
	}

	/**
	 * 
	 * Returns employee data from SuccessFactors;
	 * 
	 * @param request HTTP request
	 * @param response HTTP response
	 * @return json Employee Data
	 * @throws IOException Exception error
	 * @throws ErrorGettingEmployeeDataException 
	 */
	@RequestMapping(value = ApiValues.EMPLOYEE_DATA, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String getEmployeeData(HttpServletRequest request, HttpServletResponse response) 
	        throws IOException, ErrorGettingEmployeeDataException {
		Gson gson = GsonHelper.adaptedGson;
		Employee employeeData = null;
		try {
			//employeeData = employeeService.getEmployeeData("4002570");
			employeeData = employeeService.getEmployeeData(request.getRemoteUser());

		} catch (UserNotFoundException e) {
			response.sendError(404, "Employee data not found");
			return null;
		}
		return gson.toJson(employeeData);
	}

	/**
	 * 
	 * Returns a picklist
	 * 
	 * @param request HTTP request
	 * @param response HTTP response
	 * @throws IOException Exception error
	 * @return content PDF
	 */
	@RequestMapping(value = ApiValues.LOAD_PICKLIST, method = RequestMethod.GET, produces = "application/pdf")
	@ResponseBody
	public String getPicklists(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		Gson gson = GsonHelper.adaptedGson;
		AddressPicklist picklist = null;
	
		try {
		    picklist = addressPicklistService.getAddressPicklist();
        } catch (ErrorGettingAddressPicklistException e) {
            throw new IOException(e.getMessage(), e);
        }

		return gson.toJson(picklist);
	}
	
	/**
	 * 
	 * Returns a pay advice as pdf
	 * 
	 * @param externalCode	Payslip External Code
	 * @param effectiveStartDate Effective Start Date
	 * @param filename Attachment name
	 * @param request HTTP request
	 * @param response HTTP response
	 * @throws IOException Exception error
	 * @return content PDF
	 * @throws ErrorReadingAttachmentException 
	 */
	@RequestMapping(value = ApiValues.PAYADVICE_PDF + "/{externalCode}" + "/{effectiveStartDate}"
			+ "/{filename}", method = RequestMethod.GET, produces = "application/pdf")
	@ResponseBody
	public byte[] getPayAdvicePDF(@PathVariable String externalCode, @PathVariable String effectiveStartDate,
			@PathVariable String filename, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ErrorReadingAttachmentException {

		long l = Long.parseLong(effectiveStartDate);
		Date startDate = new Date(l);

		try {
			Attachment attachment = payadviceService.getAttachment(externalCode, startDate, request.getRemoteUser());
			byte[] document = attachment.getFileContent();
			response.setHeader("Content-Disposition", "filename=" + attachment.getFilename());
			return document;
			
		} catch (PayAdviceNotFoundException e) {
			logger.error("Payslip id: {} not found", externalCode);
			response.sendError(404, "PayAdvice not found");
			return new byte[] {};
		}
	}	
	
    /**
     * Update address request
     *
     * @param requestBody JSON Request body
     * @param request HTTP request
     * @param response HTTP response   
     * @return JSON Result
     * @throws IOException IO error
     */
	@RequestMapping(value = ApiValues.UPDATE_ADDRESS, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String updateAddress(@RequestBody String requestBody, HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		Gson gson = GsonHelper.adaptedGson;

		Address addressDetails;

		logger.trace("Address update request trace: {}", requestBody);
		try {
			addressDetails = gson.fromJson(requestBody, Address.class);

			if (addressDetails == null ){
				// not able to read data
				logger.warn("Invalid JSON sent to API");
				response.setStatus(400);
				return "{\"errorMessage\": "
				        + "\"invalid JSON body, ensure fields are populated correctly\"}";
			}
			
			if (addressDetails.getPersonIdExternal() == null){
				addressDetails.setPersonIdExternal(request.getRemoteUser());
			}
			if (addressDetails.getAddressType() == null){
				addressDetails.setAddressType("home");
			}
		
			employeeService.updateEmloyeeAddress(addressDetails);
			
	        logger.info("Employee address updated");
	        response.setStatus(204);
	        return "{}";

		} catch (JsonSyntaxException e) {
			// not able to read data
			logger.warn("invalid JSON sent to Position Req API", e);
			logger.debug("JSON that was sent: {}", requestBody);
			response.setStatus(400);
			return "{\"errorMessage\": \"invalid JSON\"}";
		} catch (RequestHandlingException e) {
		    logger.info("update address failed:", e);
		    response.setStatus(500);
		    return "{\"errorMessage\": \"" + e.getMessage() + "\"}";
		}
		
	}	
}
