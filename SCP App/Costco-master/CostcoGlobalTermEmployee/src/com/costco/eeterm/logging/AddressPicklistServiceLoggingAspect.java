package com.costco.eeterm.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.costco.eeterm.exceptions.ErrorGettingAddressPicklistException;

@Aspect
@Component
public class AddressPicklistServiceLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(AddressPicklistServiceLoggingAspect.class);
    
    @Pointcut("within(com.costco.eeterm.successfactors.AddressPicklistService+)")
    public void inAddressPicklistService() {}
    
    @Pointcut("execution(* getAddressPicklist())")
    public void getAddressPicklist() {}
    
    @Around("inAddressPicklistService() && getAddressPicklist()")
    public Object logGetAddressPicklist(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        logger.info("Starting retrieve of picklist info");
        Object retVal = proceedingJoinPoint.proceed();
        logger.info("Finished retrieve of picklist info");
        return retVal;
    }
    
    @AfterThrowing(
        pointcut="inAddressPicklistService() && getAddressPicklist()",
        throwing="ex")
    public void logGetProfilePhotoURLError(ErrorGettingAddressPicklistException ex) {
        logger.error("Error getting address picklist", ex);
    }
    
}
