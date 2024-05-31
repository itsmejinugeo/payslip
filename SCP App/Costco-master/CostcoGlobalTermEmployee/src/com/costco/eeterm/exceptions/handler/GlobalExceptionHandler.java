package com.costco.eeterm.exceptions.handler;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.WebUtils;

import com.costco.eeterm.exceptions.ErrorGettingEmployeeDataException;
import com.costco.eeterm.exceptions.ErrorGettingPayAdviceException;
import com.costco.eeterm.exceptions.ErrorReadingAttachmentException;
import com.costco.eeterm.exceptions.ExternalAppNotFoundException;
import com.costco.eeterm.exceptions.PayAdviceNotFoundException;
import com.costco.eeterm.exceptions.UserNotAuthorisedException;
import com.costco.eeterm.exceptions.UserNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler({UserNotAuthorisedException.class})
    public final ResponseEntity<Object> handleNotAuthorisedException(
            UserNotAuthorisedException e, WebRequest request) {
        logger.info("Unauthorised access to {} by {}", e.getApiAccessed(), request.getRemoteUser());
        return handleExceptionInternal(e, HttpStatus.UNAUTHORIZED, request);
    }
    
    @ExceptionHandler({
        UserNotFoundException.class, 
        ExternalAppNotFoundException.class, 
        PayAdviceNotFoundException.class})
    public final ResponseEntity<Object> handleGeneralNotFoundException(
            Exception e, WebRequest request) {
        return handleExceptionInternal(e, HttpStatus.NOT_FOUND, request);
    }
    
    @ExceptionHandler({NumberFormatException.class})
    public final ResponseEntity<Object> handleGeneralBadRequestException(Exception e, 
            WebRequest request) {
        return handleExceptionInternal(e, HttpStatus.BAD_REQUEST, request);
    }
    
    @ExceptionHandler({
        ErrorReadingAttachmentException.class, 
        ErrorGettingPayAdviceException.class, 
        ErrorGettingEmployeeDataException.class,
        IOException.class})
    public final ResponseEntity<Object> handleGeneralInternalErrorException(Exception e, 
            WebRequest request) {
        return handleExceptionInternal(e, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
    
    /** Helper to reduce number of parameters. */
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, HttpStatus status, 
            WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), status, request);
    }
    
    /** A single place to customize the response body of all Exception types. */
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, 
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }
        return new ResponseEntity<>(body, headers, status);
    }
    
}
