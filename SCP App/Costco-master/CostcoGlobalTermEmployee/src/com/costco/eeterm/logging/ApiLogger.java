package com.costco.eeterm.logging;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class ApiLogger extends HandlerInterceptorAdapter {
    
    private final static Logger logger = LoggerFactory.getLogger(ApiLogger.class);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        super.preHandle(request, response, handler);
        
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        String requestId = UUID.randomUUID().toString();
        request.setAttribute("requestId", requestId);
        
        logRequest(request);
        
        return true;
    }
    
    private void logRequest(HttpServletRequest request) {
        String requestId = (String) request.getAttribute("requestId");
        logger.info("requestId {}, host {}  HttpMethod: {}, URI: {}",
                requestId, request.getHeader("host"), request.getMethod(), request.getRequestURI());
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        super.afterCompletion(request, response, handler, ex);
        logExecutionTime(request, handler);
    }
    
    private void logExecutionTime(HttpServletRequest request, Object handler) {
        long startTime = (Long)request.getAttribute("startTime");    
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        logger.info("requestId {}, Handle :{} , request takes time: {} ms",
                request.getAttribute("requestId"), handler, executionTime);
    }

}
