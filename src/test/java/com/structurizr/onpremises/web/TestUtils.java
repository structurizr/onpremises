package com.structurizr.onpremises.web;

import com.structurizr.onpremises.domain.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;

public class TestUtils {

    public static void clearUser() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public static void setUser(String username, String... roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            roles.add(new Role(roleName));
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(username, "password", roles);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, roles);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
