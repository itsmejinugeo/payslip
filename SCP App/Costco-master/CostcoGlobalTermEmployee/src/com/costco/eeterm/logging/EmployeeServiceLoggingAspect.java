package com.costco.eeterm.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.costco.eeterm.exceptions.ErrorGettingEmployeeDataException;
import com.costco.eeterm.exceptions.UnexpectedCompanyCodeException;
import com.costco.eeterm.exceptions.RequestHandlingException;

@Aspect
@Component
public class EmployeeServiceLoggingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceLoggingAspect.class);

    @Pointcut("within(com.costco.eeterm.successfactors.EmployeeService+)")
    public void inEmployeeService() {}
    
    @Pointcut("execution(public * *(..))")
    public void publicMethod() {}
    
    @Pointcut("execution(* getEmployeeData(..))")
    public void getEmployeeData() {}
    
    @Pointcut("execution(* getCountryCodeFromCompanyCode(..))")
    public void getCountryCodeFromCompanyCode() {}
    
    @Around("inEmployeeService() && getEmployeeData() && args(userId)")
    public Object logGetEmployeeData(ProceedingJoinPoint proceedingJoinPoint ,String userId) 
            throws Throwable {
        logger.info("Started retrieve of details of user " + userId);
        Object retVal = proceedingJoinPoint.proceed();
        logger.info("Finished retrieve of user details {}", userId);
        return retVal;
    }
    
    @AfterThrowing(
        pointcut = "inEmployeeService() && publicMethod()",
        throwing = "ex")
    public void logErrorGettingEmployeeDataException(ErrorGettingEmployeeDataException ex) {
        logger.error(ex.getMessage(), ex);
    }
    
    @AfterThrowing(
        pointcut = "inEmployeeService() && publicMethod()",
        throwing = "ex")
    public void logRequestHandlingException(RequestHandlingException ex) {
        logger.error(ex.getMessage(), ex);
    }
    
    @AfterThrowing(
        pointcut = "inEmployeeService() && getCountryCodeFromCompanyCode() && args(companyCode)",
        throwing = "ex")
    public void logUnexpectedCompanyCode(String companyCode, UnexpectedCompanyCodeException ex) {
        logger.error("Expected company code {} in employee data", companyCode, ex);
    }
    
    
}
