package com.costco.eeterm.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.costco.eeterm.constants.Roles;
import com.costco.eeterm.exceptions.UserNotAuthorisedException;

public class AuthorizationHandler extends HandlerInterceptorAdapter {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
            Object handler) throws UserNotAuthorisedException {
        checkAdminPermission(request);
        return true;
    }
    
    private void checkAdminPermission(HttpServletRequest request) 
            throws UserNotAuthorisedException {
        if (!isUserInAdminRole(request)) {
            throw new UserNotAuthorisedException("Unauthorised access", request.getRequestURI());
        }
    }
    
    private boolean isUserInAdminRole(HttpServletRequest request) {
        return request.isUserInRole(Roles.TERM_EE_ADMIN_ROLE);
    }

}
