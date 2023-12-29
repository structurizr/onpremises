package com.structurizr.onpremises.web.security;

import com.structurizr.onpremises.domain.Role;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.util.StructurizrProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecurityUtilsTests {

    @Test
    public void getUser_ViaUsernameAndPassword() {
        Set<Role> roles = new HashSet<>();
        roles.add(new Role("role1"));
        roles.add(new Role("role2"));
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("user", "password", roles);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, roles);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = SecurityUtils.getUser();
        assertEquals("user", user.getUsername());
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains("role1"));
        assertTrue(user.getRoles().contains("role2"));
   }

    @Test
    public void getUser_ViaSAMLWithGroups() {
        Saml2AuthenticatedPrincipal principal = new Saml2AuthenticatedPrincipal() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getFirstAttribute(String name) {
                if (StructurizrProperties.DEFAULT_SAML_ATTRIBUTE_USERNAME.equals(name)) {
                    return "user";
                } else {
                    return null;
                }
            }

            @Override
            public List<Object> getAttribute(String name) {
                if (StructurizrProperties.DEFAULT_SAML_ATTRIBUTE_ROLE.equals(name)) {
                    List<Object> list = new ArrayList<>();
                    list.add("role1");
                    list.add("role2");
                    return list;
                } else {
                    return null;
                }
            }
        };

        Saml2Authentication credential = new Saml2Authentication(principal, "SAML response", null);
        //Authentication authentication = new UsernamePasswordAuthenticationToken("structurizr", credential, new HashSet<Role>());
        SecurityContextHolder.getContext().setAuthentication(credential);

        Configuration.init();
        User user = SecurityUtils.getUser();
        assertEquals("user", user.getUsername());
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains("role1"));
        assertTrue(user.getRoles().contains("role2"));
    }

}