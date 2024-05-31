package com.costco.au.payadvice.rests;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.costco.au.payadvice.constants.ApiValues;
import com.costco.au.payadvice.exceptions.PayAdviceNotFoundException;
import com.costco.au.payadvice.exceptions.UserNotFoundException;
import com.costco.au.payadvice.gson.GsonHelper;
import com.costco.au.payadvice.successfactors.EmployeeService;
import com.costco.au.payadvice.successfactors.PayAdviceService;
import com.costco.au.payadvice.successfactors.pojo.Attachment;
import com.costco.au.payadvice.successfactors.pojo.Employee;
import com.costco.au.payadvice.successfactors.pojo.PayAdvice;
import com.costco.au.payadvice.successfactors.pojo.PayAdviceOverview;
import com.google.gson.Gson;

/**
 * Provides the APIs for pay advices
 * 
 */
@Controller
public class PayAdviceListServlet {
	private static final Logger logger = LoggerFactory.getLogger(PayAdviceListServlet.class);
	@Autowired
	PayAdviceService payadviceService;

	@Autowired
	EmployeeService employeeService;

	/**
	 * 
	 * Returns list of payadvices for the current user;
	 * 
	 * @param request HTTP request
	 * @param response HTTP response
	 * @return json Pay Advice list
	 * @throws IOException Exception error
	 */
	@RequestMapping(value = ApiValues.PAYADVICE_LIST, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String getCurrentUserPayAdviceList(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		Gson gson = GsonHelper.adaptedGson;

		try {
			PayAdviceOverview overview = new PayAdviceOverview();
			Employee employeeInfo = employeeService.getEmployeeData(request.getRemoteUser());
			List<PayAdvice> payadviceList = payadviceService.getEmployeePayAdvice(request.getRemoteUser());

			overview = mapToOverview(employeeInfo,payadviceList);
			return gson.toJson(overview);

		} catch (PayAdviceNotFoundException | UserNotFoundException e) {
			response.sendError(404, "No payadvices found for user");
			return null;
		}
	}

	/**
	 * 
	 * Returns list of payadvices for a given user;
	 * 
	 * @param userId Employee Id
	 * @param request HTTP request
	 * @param response HTTP response
	 * @return json Pay Advice list
	 * @throws IOException Exception error
	 */
	@RequestMapping(value = ApiValues.ADMIN_PAYADVICE_LIST  + "/{userId}" , method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String getEmployeePayAdviceList(@PathVariable String userId, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		Gson gson = GsonHelper.adaptedGson;

		try {
			PayAdviceOverview overview = new PayAdviceOverview();
			Employee employeeInfo = employeeService.getEmployeeData(userId);
			List<PayAdvice> payadviceList = payadviceService.getEmployeePayAdvice(userId);

			overview = mapToOverview(employeeInfo,payadviceList);
			return gson.toJson(overview);

		} catch (PayAdviceNotFoundException | UserNotFoundException e) {
			response.sendError(404, "No payadvices found for user" + userId);
			return null;
		}
	}
	
	private PayAdviceOverview mapToOverview(Employee employeeInfo, List<PayAdvice> payadviceList) {
		
		PayAdviceOverview overview = new PayAdviceOverview();
		overview.setDefaultFullName(employeeInfo.getDefaultFullName());
		overview.setProfilePhotoURL(employeeInfo.getProfilePhotoURL());
		overview.setDepartment(employeeInfo.getDepartment());
		overview.setJobCode(employeeInfo.getJobCode());
		overview.setPositionTitle(employeeInfo.getPositionTitle());
		overview.setLocation(employeeInfo.getLocation());
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
	 */
	@RequestMapping(value = ApiValues.EMPLOYEE_DATA, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String getEmployeeData(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Gson gson = GsonHelper.adaptedGson;
		Employee employeeData = null;
		try {
			employeeData = employeeService.getEmployeeData(request.getRemoteUser());

		} catch (UserNotFoundException e) {
			response.sendError(404, "Employee data not found");
			return null;
		}
		return gson.toJson(employeeData);

	}

	/**
	 * 
	 * Returns a pay advice as pdf requested by admin
	 * 
	 * @param externalCode	Payslip External Code
	 * @param effectiveStartDate Effective Start Date
	 * @param userId Employee Id
	 * @param filename Attachment name
	 * @param request HTTP request
	 * @param response HTTP response
	 * @throws IOException Exception error
	 * @return content PDF
	 */
	@RequestMapping(value = ApiValues.ADMIN_PAYADVICE_PDF + "/{externalCode}" + "/{effectiveStartDate}" + "/{userId}" + "/{filename}", method = RequestMethod.GET, produces = "application/pdf")
	@ResponseBody
	public byte[] getPayAdvicePDFForUser(@PathVariable String externalCode, @PathVariable String effectiveStartDate, @PathVariable String userId, @PathVariable String filename, HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		long l = Long.parseLong(effectiveStartDate);
		Date startDate = new Date(l);

		try {
			Attachment attachment = payadviceService.getAttachment(externalCode, startDate, userId);
			byte[] document = attachment.getFileContent();
			response.setHeader("Content-Disposition", "filename=" + attachment.getFilename());
			return document;
			
		} catch (PayAdviceNotFoundException e) {
			logger.error("Payslip id: {} for emplpoyee {} not found", externalCode, userId);
			response.sendError(404, "PayAdvice not found");
			return new byte[] {};
		}
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
	 */
	@RequestMapping(value = ApiValues.PAYADVICE_PDF + "/{externalCode}" + "/{effectiveStartDate}"
			+ "/{filename}", method = RequestMethod.GET, produces = "application/pdf")
	@ResponseBody
	public byte[] getPayAdvicePDF(@PathVariable String externalCode, @PathVariable String effectiveStartDate,
			@PathVariable String filename, HttpServletRequest request, HttpServletResponse response)
			throws IOException {

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
}
