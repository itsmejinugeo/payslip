package com.costco.eeterm.logging;

import javax.naming.NamingException;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ProfilePhotoServiceLoggingAspect {
    
    private final static Logger logger = LoggerFactory.getLogger(ProfilePhotoServiceLoggingAspect.class);
    
    @Pointcut("within(com.costco.eeterm.successfactors.ProfilePhotoService+)")
    public void inProfilePhotoService() {}
    
    @Pointcut("execution(* getProfilePhotoURL())")
    public void getProfilePhotoURL() {}
    
    @AfterThrowing(
        pointcut="inProfilePhotoService() && getProfilePhotoURL()",
        throwing="ex")
    public void logProfilePhotoURLNamingException(NamingException ex) {
        logger.error("Error getting SF profile photo URL", ex);
    }
    
}
