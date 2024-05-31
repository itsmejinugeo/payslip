package com.costco.eeterm.logging;

import java.util.Date;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.costco.eeterm.exceptions.PayAdviceNotFoundException;

@Aspect
@Component
public class PayAdviceServiceLoggingAspect {
    
    private static final Logger logger = 
            LoggerFactory.getLogger(PayAdviceServiceLoggingAspect.class);

    @Pointcut("within(com.costco.eeterm.successfactors.PayAdviceService+)")
    public void inPayAdviceService() {}
    
    @Pointcut("execution(public * *(..))")
    public void publicMethod() {}
    
    @Pointcut("execution(* getEmployeePayAdvice(..))")
    public void getEmployeePayAdvice() {}
    
    @Pointcut("execution(* getAttachment(..))")
    public void getAttachment() {}
    
    @Around("inPayAdviceService() && getEmployeePayAdvice()")
    public Object logGetEmployeePayAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        logger.info("Started retrieve of user's pay advice");
        Object retVal = proceedingJoinPoint.proceed();
        logger.info("Finished retrieve of user's pay advice data");
        return retVal;
    }
    
    @AfterThrowing(
        pointcut="inPayAdviceService() && publicMethod()",
        throwing="ex")
    public void logPublicMethodException(Exception ex) {
        if (isLoggableException(ex)) {
            logger.error(ex.getMessage(), ex);            
        }
    }
    
    private boolean isLoggableException(Exception ex) {
        boolean loggable = !(ex instanceof PayAdviceNotFoundException);
        return loggable;
    }
    
    /**
     * Logs PayAdviceNotFound exception.
     * As per business logic, front end call should not be intriguing this
     */
    @AfterThrowing(
        pointcut="inPayAdviceService() && getAttachment() "
                + "&& args(externalCode, effectiveStartDate, userId)",
        throwing="ex")
    public void logPayAdviceNotFoundException(String externalCode, Date effectiveStartDate, 
            String userId, Exception ex) {
        logger.warn("Pay advice not found: externalCode {}, effectiveStartDate {}, userId {}", 
                externalCode, effectiveStartDate, userId, ex);
    }
    
}
