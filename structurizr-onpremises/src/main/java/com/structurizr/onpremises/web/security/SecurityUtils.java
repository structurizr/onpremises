package com.structurizr.onpremises.web.security;

import com.structurizr.onpremises.domain.AuthenticationMethod;
import com.structurizr.onpremises.domain.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility to extract user details (username/roles) from Spring Security.
 */
public final class SecurityUtils {

    private static final Log log = LogFactory.getLog(SecurityUtils.class);

    static final String SAML_EMAIL_ADDRESS_ATTRIBUTE = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";
    static final String SAML_GROUP_ATTRIBUTE = "http://schemas.xmlsoap.org/claims/Group";
    static final String SAML_AUTH0_ROLES_ATTRIBUTE = "http://schemas.auth0.com/roles";

    public static User getUser() {
        return getUser(SecurityContextHolder.getContext().getAuthentication());
    }

    public static User getUser(Authentication authentication) {
        User user = null;
        Set<String> roles = new HashSet<>();

        if (authentication != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                String username =  userDetails.getUsername();

                for (GrantedAuthority grantedAuthority : userDetails.getAuthorities()) {
                    roles.add(grantedAuthority.getAuthority());
                }

                return new User(username, roles, AuthenticationMethod.LOCAL);
            } else {
                if (authentication instanceof Saml2Authentication) {
                    Saml2Authentication saml2Authentication = (Saml2Authentication)authentication;
                    Saml2AuthenticatedPrincipal saml2AuthenticatedPrincipal = (Saml2AuthenticatedPrincipal)saml2Authentication.getPrincipal();

                    Map<String, List<Object>> attributes = saml2AuthenticatedPrincipal.getAttributes();
                    for (String name : attributes.keySet()) {
                        List<Object> values = attributes.get(name);
                        if (values != null) {
                            for (Object value : values) {
                                log.debug(name + " = " + value);
                            }
                        }
                    }

                    String emailAddress = saml2AuthenticatedPrincipal.getFirstAttribute(SAML_EMAIL_ADDRESS_ATTRIBUTE);

                    List<Object> groups = saml2AuthenticatedPrincipal.getAttribute(SAML_GROUP_ATTRIBUTE);
                    if (groups != null) {
                        for (Object g : groups) {
                            roles.add(g.toString());
                        }
                    }

                    List<Object> auth0Roles = saml2AuthenticatedPrincipal.getAttribute(SAML_AUTH0_ROLES_ATTRIBUTE);
                    if (auth0Roles != null) {
                        for (Object r : auth0Roles) {
                            roles.add(r.toString());
                        }
                    }

                    user = new User(emailAddress, roles, AuthenticationMethod.SAML);
                }
            }
        }

        return user;
    }

}