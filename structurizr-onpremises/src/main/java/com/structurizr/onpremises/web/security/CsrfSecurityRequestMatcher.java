package com.structurizr.onpremises.web.security;

import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;

public class CsrfSecurityRequestMatcher implements RequestMatcher {

    @Override
    public boolean matches(HttpServletRequest request) {
        String method = request.getMethod();

        if ("POST".equals(method)) {
            String uri = request.getRequestURI();

            if (
                    uri.startsWith("/login") || uri.startsWith("/workspace")
            ) {
                return true;
            }
        }

        return false;
    }

}