package com.costco.eeterm.logging;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.costco.eeterm.exceptions.DestinationReadingException;

@Aspect
@Component
public class DestinationServiceLoggingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(DestinationServiceLoggingAspect.class);
    
    @Pointcut("within(com.costco.eeterm.common.service.DestinationService+)")
    public void inDestinationService() {}
    
    @Pointcut("execution(* getDestinationByName(..))")
    public void getDestinationByName() {}
   
    @AfterThrowing(
        pointcut="inDestinationService() && getDestinationByName() && args(destName)",
        throwing="ex")
    public void logDestinationReadingException(String destName, DestinationReadingException ex) {
        logger.error("Error reading destination {}", destName, ex);              
    }
    
}
