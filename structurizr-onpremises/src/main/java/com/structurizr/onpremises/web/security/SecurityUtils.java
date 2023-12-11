package com.structurizr.onpremises.web.security;

import com.structurizr.onpremises.domain.AuthenticationMethod;
import com.structurizr.onpremises.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility to extract user details (username/roles) from Spring Security.
 */
public final class SecurityUtils {

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
                // todo
//                if (authentication.getCredentials() instanceof SAMLCredential) {
//                    SAMLCredential samlCredential = (SAMLCredential) authentication.getCredentials();
//                    String emailAddress = samlCredential.getAttributeAsString(SAML_EMAIL_ADDRESS_ATTRIBUTE);
//
//                    String[] groups = samlCredential.getAttributeAsStringArray(SAML_GROUP_ATTRIBUTE);
//                    if (groups != null) {
//                        roles.addAll(Arrays.asList(groups));
//                    }
//
//                    String[] auth0Roles = samlCredential.getAttributeAsStringArray(SAML_AUTH0_ROLES_ATTRIBUTE);
//                    if (auth0Roles != null) {
//                        roles.addAll(Arrays.asList(auth0Roles));
//                    }
//
//                    user = new User(emailAddress, roles, AuthenticationMethod.SAML);
//                }
            }
        }

        return user;
    }

}