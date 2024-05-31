package com.costco.eeterm.logging;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.costco.eeterm.exceptions.AttachmentNotFoundException;

@Aspect
@Component
public class AttachmentServiceLoggingAspect {
    
    private static final Logger logger = 
            LoggerFactory.getLogger(AttachmentServiceLoggingAspect.class);

    @Pointcut("within(com.costco.eeterm.successfactors.AttachmentService+)")
    public void inAttachmentService() {}
    
    @Pointcut("execution(public * *(..))")
    public void publicMethod() {}
    
    @AfterThrowing(
        pointcut="inAttachmentService() && publicMethod()",
        throwing="ex")
    public void logPublicMethodException(Exception ex) {
        if (isLoggableException(ex)) {
            logger.error(ex.getMessage(), ex);            
        }
    }
    
    private boolean isLoggableException(Exception ex) {
        boolean loggable = !(ex instanceof AttachmentNotFoundException);
        return loggable;
    }
    
}
