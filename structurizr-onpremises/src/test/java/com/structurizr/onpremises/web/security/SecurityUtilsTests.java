package com.structurizr.onpremises.web.security;

import com.structurizr.onpremises.domain.Role;
import com.structurizr.onpremises.domain.User;
import org.junit.jupiter.api.Test;
//import org.opensaml.saml2.core.impl.AssertionBuilder;
//import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.saml.SAMLCredential;

import java.util.HashSet;
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

//    @Test
//    public void getUser_ViaSAMLWithGroups() {
//        SAMLCredential credential = new SAMLCredential(
//                new NameIDBuilder().buildObject(), new AssertionBuilder().buildObject(), null, null) {
//            @Override
//            public String getAttributeAsString(String name) {
//                if (name.equals(SecurityUtils.SAML_EMAIL_ADDRESS_ATTRIBUTE)) {
//                    return "user";
//                } else {
//                    return "";
//                }
//            }
//
//            @Override
//            public String[] getAttributeAsStringArray(String name) {
//                if (name.equals(SecurityUtils.SAML_GROUP_ATTRIBUTE)) {
//                    return new String[] { "role1", "role2" };
//                } else {
//                    return new String[]{};
//                }
//            }
//        };
//        Authentication authentication = new UsernamePasswordAuthenticationToken("structurizr", credential, new HashSet<Role>());
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        User user = SecurityUtils.getUser();
//        assertEquals("user", user.getUsername());
//        assertEquals(2, user.getRoles().size());
//        assertTrue(user.getRoles().contains("role1"));
//        assertTrue(user.getRoles().contains("role2"));
//    }
//
//    @Test
//    public void getUser_ViaSAMLWithAuth0Roles() {
//        SAMLCredential credential = new SAMLCredential(
//                new NameIDBuilder().buildObject(), new AssertionBuilder().buildObject(), null, null) {
//            @Override
//            public String getAttributeAsString(String name) {
//                if (name.equals(SecurityUtils.SAML_EMAIL_ADDRESS_ATTRIBUTE)) {
//                    return "user";
//                } else {
//                    return "";
//                }
//            }
//
//            @Override
//            public String[] getAttributeAsStringArray(String name) {
//                if (name.equals(SecurityUtils.SAML_AUTH0_ROLES_ATTRIBUTE)) {
//                    return new String[] { "role1", "role2" };
//                } else {
//                    return new String[]{};
//                }
//            }
//        };
//        Authentication authentication = new UsernamePasswordAuthenticationToken("structurizr", credential, new HashSet<Role>());
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        User user = SecurityUtils.getUser();
//        assertEquals("user", user.getUsername());
//        assertEquals(2, user.getRoles().size());
//        assertTrue(user.getRoles().contains("role1"));
//        assertTrue(user.getRoles().contains("role2"));
//    }

}