package com.structurizr.onpremises.web.security;

import com.structurizr.onpremises.domain.AuthenticationMethod;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.web.AbstractController;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SignoutController extends AbstractController {

    @RequestMapping(value = "/signout", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
   	public String signout(HttpServletRequest request, HttpServletResponse response) {
        //SecurityContextHolder.getContext().setAuthentication(null);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        User user = getUser();
        if (user.getAuthenticationMethod() == AuthenticationMethod.LOCAL) {
            Cookie cookie = new Cookie("remember-me", null);
            cookie.setMaxAge(0);

            response.addCookie(cookie);

        }

        return "redirect:/";
   	}

}