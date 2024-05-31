package com.costco.eeterm.rests;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.costco.eeterm.common.service.DestinationService;
import com.costco.eeterm.constants.ApiValues;
import com.costco.eeterm.exceptions.DestinationNotFoundException;
import com.costco.eeterm.exceptions.DestinationReadingException;
import com.costco.eeterm.exceptions.ErrorGettingEmployeeDataException;
import com.costco.eeterm.exceptions.ErrorGettingPayAdviceException;
import com.costco.eeterm.exceptions.ErrorReadingAttachmentException;
import com.costco.eeterm.exceptions.ExternalAppNotFoundException;
import com.costco.eeterm.exceptions.PayAdviceNotFoundException;
import com.costco.eeterm.exceptions.UserNotFoundException;
import com.costco.eeterm.gson.GsonHelper;
import com.costco.eeterm.successfactors.EmployeeService;
import com.costco.eeterm.successfactors.PayAdviceService;
import com.costco.eeterm.successfactors.pojo.Attachment;
import com.costco.eeterm.successfactors.pojo.Employee;
import com.costco.eeterm.successfactors.pojo.PayAdvice;
import com.costco.eeterm.successfactors.pojo.PayAdviceOverview;
import com.google.gson.Gson;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

@Controller
public class TermEmployeeAdminServlet {
    
    private final Logger logger = LoggerFactory.getLogger(TermEmployeeAdminServlet.class);
    
    @Autowired
    EmployeeService employeeService;
    
    @Autowired
    PayAdviceService payadviceService;
    
    @Autowired
    DestinationService destinationService;

    /**
     * Get employee data with given user id
     * @return employee data
     * @throws UserNotFoundException
     * @throws ErrorGettingEmployeeDataException 
     */
    @RequestMapping(
        value = ApiValues.ADMIN_EMPLOYEE_DATA + "/{userId}", 
        method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getEmployeeData(@PathVariable String userId) 
            throws UserNotFoundException, ErrorGettingEmployeeDataException {
        Employee employeeData = employeeService.getEmployeeData(userId);
        Gson gson = GsonHelper.adaptedGson;
        return ResponseEntity.ok(gson.toJson(employeeData));
    }
    
    /**
     * Search AU/CAN terminated employee by name
     * @return List of employee data containing the name
     * @throws ErrorGettingEmployeeDataException 
     */
    @RequestMapping(
        value = ApiValues.ADMIN_TERMINATED_EMPLOYEE_SEARCH,
        method=RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> searchTerminatedEmployeeByName(@RequestParam String name) 
            throws ErrorGettingEmployeeDataException {
        List<Employee> employees = employeeService.findTerminatedAusNCanEmployeesByName(name);
        Gson gson = GsonHelper.adaptedGson;
        return ResponseEntity.ok(gson.toJson(employees));
    }
    
    /**
     * Get pay advice for user
     * @param userId user id in SF
     * @return Pay advice list with employee data 
     * @throws UserNotFoundException
     * @throws PayAdviceNotFoundException
     * @throws ErrorGettingPayAdviceException 
     * @throws ErrorGettingEmployeeDataException 
     */
    @RequestMapping(
        value = ApiValues.ADMIN_PAYADVICE_LIST + "/{userId}", 
        method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getUserPayAdviceList(@PathVariable String userId) 
            throws UserNotFoundException, PayAdviceNotFoundException, 
                ErrorGettingPayAdviceException, ErrorGettingEmployeeDataException {
        PayAdviceOverview overview = new PayAdviceOverview();
        Employee employeeInfo = employeeService.getEmployeeData(userId);
        List<PayAdvice> payadviceList = payadviceService.getEmployeePayAdvice(userId);
        overview = mapToOverview(employeeInfo,payadviceList);
        
        Gson gson = GsonHelper.adaptedGson;
        return ResponseEntity.ok(gson.toJson(overview));
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
     * Get external app URL
     * @param appName external app name, same in destination settings
     * @param empId employee Id for the target user
     * @return external app URL
     * @throws ExternalAppNotFoundException
     */
    @RequestMapping(
        value = ApiValues.ADMIN_EXTERNAL_APP,
        method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getExternalAppURL(@RequestParam(required = true) String appName, 
            @RequestParam(required = true) String empId) throws ExternalAppNotFoundException {
        String externalAppURL = fetchExternalAppURLFromDest(appName, empId);
        
        Map<String, String> externalApp = new HashMap<>();
        externalApp.put("name", appName);
        externalApp.put("empId", empId);
        externalApp.put("url", externalAppURL);
        
        Gson gson = GsonHelper.adaptedGson;
        return ResponseEntity.ok(gson.toJson(externalApp));
    }
    
    private String fetchExternalAppURLFromDest(String appName, String empId) 
            throws ExternalAppNotFoundException {
        try {
            DestinationConfiguration dest = destinationService.getDestinationByName(appName);
            String url = dest.getProperty("URL");
            String empIdParameterName = dest.getProperty("empIdParameterName");
            if (url == null || empIdParameterName == null) {
                throw new ExternalAppNotFoundException(
                        "External app" + appName + " dest not configured properly.");
            }
            return url + '&' + empIdParameterName + '=' + empId;
        } catch (DestinationNotFoundException | DestinationReadingException e) {
            throw new ExternalAppNotFoundException(e.getMessage());
        }
    }
    
    /**
     * Get pay advice PDF 
     * @param externalCode Payslip External Code
     * @param effectiveStartDate Effective Start Date
     * @param userId Employee Id
     * @param filename Attachment name
     * @return requested pay advice PDF
     * @throws PayAdviceNotFoundException
     * @throws NumberFormatException
     * @throws ErrorReadingAttachmentException 
     */
    @RequestMapping(
        value = ApiValues.ADMIN_PAYADVICE_PDF 
            + "/{externalCode}" + "/{effectiveStartDate}" + "/{userId}" + "/{filename}", 
        method = RequestMethod.GET, produces = "application/pdf")
    public ResponseEntity<byte[]> getPayAdvicePDFForUser(
            @PathVariable String externalCode, @PathVariable String effectiveStartDate, 
            @PathVariable String userId, @PathVariable String filename) 
            throws PayAdviceNotFoundException, NumberFormatException, ErrorReadingAttachmentException {
        long l = Long.parseLong(effectiveStartDate);
        Date startDate = new Date(l);

        try {
            Attachment attachment = payadviceService.getAttachment(externalCode, startDate, userId);
            byte[] document = attachment.getFileContent();
            return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Disposition", "filename=" + attachment.getFilename())
                .body(document);
        } catch (PayAdviceNotFoundException e) {
            logger.error("Payslip id: {} for emplpoyee {} not found", externalCode, userId);
            throw new PayAdviceNotFoundException("PayAdvice " + externalCode +  " not found");
        }
    }
    
}
