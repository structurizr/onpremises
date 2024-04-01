package com.structurizr.onpremises.web.security;

import com.structurizr.util.StringUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

public class AuthenticationFailureHandler implements org.springframework.security.web.authentication.AuthenticationFailureHandler {

    private static Log log = LogFactory.getLog(AuthenticationFailureHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        String username = httpServletRequest.getParameter("username");

        if (!StringUtils.isNullOrEmpty(username)) {
            log.warn(username + " failed authentication: " + e);
        } else {
            log.warn(e);
        }

        httpServletResponse.sendRedirect("/signin?error=true");
    }

}
