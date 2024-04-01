package com.structurizr.onpremises.web.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class CsrfSecurityRequestMatcher implements RequestMatcher {

    @Override
    public boolean matches(HttpServletRequest request) {
        String method = request.getMethod();

        if ("POST".equals(method)) {
            String uri = request.getRequestURI();

            if (
                    uri.startsWith("/login")
            ) {
                return true;
            }
        }

        return false;
    }

}