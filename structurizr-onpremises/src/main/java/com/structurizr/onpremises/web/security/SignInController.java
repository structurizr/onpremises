package com.structurizr.onpremises.web.security;

import com.structurizr.onpremises.web.AbstractController;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.Filter;

@Controller
public class SignInController extends AbstractController implements ApplicationContextAware {

    private static final String STRUCTURIZR_USERNAME_COOKIE_NAME = "structurizr.username";
    private static final String FILTER_CHAIN_PROXY_BEAN_NAME = "org.springframework.security.filterChainProxy";

    private ApplicationContext applicationContext;

    @RequestMapping(value = "/signin", method = RequestMethod.GET)
    public String getSignInPage(@CookieValue(value=STRUCTURIZR_USERNAME_COOKIE_NAME,defaultValue="") String username, ModelMap model) {
        if (username != null && username.length() > 0) {
            model.addAttribute("username", username);
        }

        // only show the "remember me" checkbox if the RememberMeAuthenticationFilter is configured
        boolean rememberMeEnabled = false;
        FilterChainProxy filterChainProxy = (FilterChainProxy)applicationContext.getBean(FILTER_CHAIN_PROXY_BEAN_NAME);
        if (filterChainProxy != null) {
            for (SecurityFilterChain filterChain : filterChainProxy.getFilterChains()) {
                for (Filter filter : filterChain.getFilters()) {
                    if (filter instanceof RememberMeAuthenticationFilter) {
                        rememberMeEnabled = true;
                    }
                }
            }
        }

        model.addAttribute("rememberMeEnabled", rememberMeEnabled);
        addCommonAttributes(model, "Sign in", true);

        return "signin";
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}