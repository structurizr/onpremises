package com.structurizr.onpremises.web.security;

import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletRequest;

public class CsrfSecurityRequestMatcher implements RequestMatcher {

    @Override
    public boolean matches(HttpServletRequest request) {
        String method = request.getMethod();

        if ("POST".equals(method)) {
            String uri = request.getRequestURI();

            /*
             * Matches URIs like:
             * /login*
             * /workspace/123/images/delete
             * /workspace/123/private
             * /workspace/123/public
             * /workspace/123/unshare
             * /workspace/123/share
             * /workspace/123/delete
             */
            if (uri.startsWith("/login")
                    || uri.matches("/workspace/\\d+/(images/delete|private|public|unshare|share|delete)")) {
                return true;
            }
        }

        return false;
    }

}