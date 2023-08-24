package com.structurizr.onpremises.web.security;

import com.structurizr.onpremises.domain.AuthenticationMethod;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.web.AbstractController;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
public class SignoutController extends AbstractController {

    @RequestMapping(value = "/signout", method = RequestMethod.GET)
   	public String signout(HttpServletResponse response) {
        User user = getUser();

        if (user.getAuthenticationMethod() == AuthenticationMethod.SAML) {
            return "redirect:/saml/logout?local=true";
        } else {
            SecurityContextHolder.getContext().setAuthentication(null);

            Cookie cookie = new Cookie("remember-me", null);
            cookie.setMaxAge(0);

            response.addCookie(cookie);

            return "redirect:/";
        }
   	}

}