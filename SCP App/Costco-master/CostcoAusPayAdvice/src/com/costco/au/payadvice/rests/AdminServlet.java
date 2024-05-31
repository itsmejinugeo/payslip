package com.costco.au.payadvice.rests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.costco.au.payadvice.constants.ApiValues;
import com.costco.au.payadvice.gson.UTCDateTypeAdapter;
import com.costco.au.payadvice.successfactors.EmployeeService;
import com.costco.au.payadvice.successfactors.pojo.Employee;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This servlet provides the APIs for administrator/payroll clerk
 * 
 */
@Controller
public class AdminServlet {

	@Autowired
	EmployeeService employeeService;

	/**
	 * 
	 * Return result of employee name from SuccessFactors;
	 * 
	 * @param request
	 *            HTTP request
	 * @param response
	 *            HTTP response
	 * @return json List of employees
	 * @throws IOException
	 *             Exception error
	 */
	@RequestMapping(value = ApiValues.ADMIN_EMPLOYEE_SEARCH, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String searchEmployeeRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.registerTypeAdapter(Date.class, new UTCDateTypeAdapter()).create();
		
		String filter = request.getParameter("filter");
		
		//check if only numbers then filter by employee userId
		boolean userId = false;
		
		if (filter.matches("[0-9]+")) {
			userId = true;
		}
		
		List<Employee> searchResult = new ArrayList<Employee>();
		if (userId) {
			searchResult = employeeService.findEmployeeByUserId(filter);
			return gson.toJson(searchResult);
		} else {
			searchResult = employeeService.findEmployeeByName(filter);
			return gson.toJson(searchResult);
		}
	}

}