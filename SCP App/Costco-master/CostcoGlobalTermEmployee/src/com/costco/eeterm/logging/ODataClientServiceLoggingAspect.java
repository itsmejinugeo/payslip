package com.costco.eeterm.logging;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ODataClientServiceLoggingAspect {
    
    private static Logger logger = LoggerFactory.getLogger(ODataClientServiceLoggingAspect.class);

    @Pointcut("within(com.costco.eeterm.odata.odatav2.ODataClientService+)")
    public void inODataClientService() {}
    
    @Pointcut("execution(public * *(..))")
    public void publicMethod() {}
    
    @Around("inODataClientService() && publicMethod()")
    public Object logResponseTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        
        Object returnVal = proceedingJoinPoint.proceed();
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        logger.info("OData request {}, takes time: {} ms", requestId, executionTime);
        
        logMethodCallDetails(requestId, proceedingJoinPoint);
        return returnVal;
    }
    
    private void logMethodCallDetails(String requestId, ProceedingJoinPoint proceedingJoinPoint) {
        if (logger.isDebugEnabled()) {
            logger.debug("OData request {}, method: {}, args: <{}>", 
                    requestId, proceedingJoinPoint.getSignature(), proceedingJoinPoint.getArgs());            
        }
    }

}
