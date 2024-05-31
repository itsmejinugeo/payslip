package com.costco.au.payadvice.rests;

import java.io.IOException;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.costco.au.payadvice.constants.ApiValues;
import com.sap.security.auth.login.LoginContextFactory;
/**
 * This servlet provides service requests to web application performing the API calls and processing
 */
@Controller
public class LogoutServlet {
	private static final Logger logger = LoggerFactory.getLogger(LogoutServlet.class);
    /**
     * Logout
     *
     * @return Result
     * @param request HTTP request
     * @param response HTTP response        
     * @throws ServletException Servlet error	
     * @throws IOException I/O operations failed 
     */
	@RequestMapping(value = ApiValues.LOGOUT, method = RequestMethod.GET)
	@ResponseBody
	public String doLogout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		logger.info("Logging out user");
		LoginContext loginContext = null;
		if (request.getRemoteUser() != null) {
			try {
				loginContext = LoginContextFactory.createLoginContext();
				loginContext.logout();

				response.setHeader("location", "/auspayadvice");
				response.setStatus(302);
				return "Logged out";

			} catch (LoginException e) {
				// Servlet container handles the login exception
				// It throws it to the application for its information
				return "Logout failed. Reason: " + e.getMessage();
			}
		} else {
			response.setHeader("location", "/auspayadvice");
			response.setStatus(302);
			return "You are logged out.";
		}
	}
}
