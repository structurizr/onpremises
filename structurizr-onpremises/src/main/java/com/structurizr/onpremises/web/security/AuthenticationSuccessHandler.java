package com.structurizr.onpremises.web.security;

import com.structurizr.onpremises.domain.AuthenticationMethod;
import com.structurizr.onpremises.domain.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Log log = LogFactory.getLog(AuthenticationSuccessHandler.class);

    private static final int USERNAME_COOKIE_EXPIRY_IN_SECONDS = 60 * 60 * 24 * 30; // 30 days

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        User user = SecurityUtils.getUser(authentication);
        log.info(user.getUsername() + " successfully authenticated");

        if (user.getAuthenticationMethod() == AuthenticationMethod.LOCAL) {
            Cookie cookie = new Cookie("structurizr.username", user.getUsername());
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            String rememberUsername = request.getParameter("rememberUsername");
            if (rememberUsername != null && "true".equals(rememberUsername)) {
                cookie.setMaxAge(USERNAME_COOKIE_EXPIRY_IN_SECONDS);
            } else {
                cookie.setMaxAge(0);
            }
            httpServletResponse.addCookie(cookie);
        }

        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, httpServletResponse);
        String hash = request.getParameter("hash");
        if (hash == null) {
            hash = "";
        }

        if (savedRequest != null) {
            httpServletResponse.sendRedirect(savedRequest.getRedirectUrl() + hash);
        } else {
            httpServletResponse.sendRedirect("/dashboard");
        }
    }

}