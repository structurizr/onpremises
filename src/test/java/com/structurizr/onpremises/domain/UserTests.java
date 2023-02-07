package com.structurizr.onpremises.domain;

import com.structurizr.onpremises.util.Configuration;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserTests {

    private User user;

    @Test
    public void construction_WithoutRoles() {
        user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);

        assertEquals("user@example.com", user.getUsername());
        assertEquals(AuthenticationMethod.LOCAL, user.getAuthenticationMethod());

        assertEquals(0, user.getRoles().size());
    }

    @Test
    public void construction_WithRoles() {
        Set<String> roles = new HashSet<>();
        roles.add("role1");
        roles.add("role2");
        roles.add("role3");
        user = new User("user@example.com", roles, AuthenticationMethod.LOCAL);

        assertEquals("user@example.com", user.getUsername());
        assertEquals(AuthenticationMethod.LOCAL, user.getAuthenticationMethod());

        assertEquals(3, user.getRoles().size());
        assertTrue(user.getRoles().contains("role1"));
        assertTrue(user.getRoles().contains("role2"));
        assertTrue(user.getRoles().contains("role3"));
    }

    @Test
    public void isUserOrRole_ReturnsFalse_WhenTheListOfUsersAndRolesIsEmpty() {
        user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);

        assertFalse(user.isUserOrRole(Collections.emptySet()));
    }

    @Test
    public void isUserOrRole_ReturnsFalse_WhenTheListOfUsersAndRolesIsNull() {
        user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);

        assertFalse(user.isUserOrRole(null));
    }

    @Test
    public void isUserOrRole_ReturnsFalse_WhenUsernameIsNotInTheListOfUsersAndRoles() {
        user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);

        // 1. case-insensitive match on username - false
        assertFalse(user.isUserOrRole(Collections.singleton("user@google.com")));
    }

    @Test
    public void isUserOrRole_ReturnsTrue_WhenUsernameIsInTheListOfUsersAndRoles() {
        user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);

        // 1. case-insensitive match on username - true
        assertTrue(user.isUserOrRole(Collections.singleton("user@example.com")));
    }

    @Test
    public void isUserOrRole_ReturnsFalse_WhenUsernameDoesNotMatchARegex() {
        user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);

        // 2. regex match on username - false
        assertFalse(user.isUserOrRole(Collections.singleton("^.*@google.com$")));
    }

    @Test
    public void isUserOrRole_ReturnsTrue_WhenUsernameMatchesARegex() {
        user = new User("user@example.com", new HashSet<>(), AuthenticationMethod.LOCAL);

        // 2. regex match on username - true
        assertTrue(user.isUserOrRole(Collections.singleton("^.*@example.com$")));
    }

    @Test
    public void isUserOrRole_ReturnsFalse_WhenARoleIsNotInTheListOfUsersAndRoles() {
        Set<String> roles = new HashSet<>();
        roles.add("role1");
        roles.add("role2");
        roles.add("role3");
        user = new User("user@example.com", roles, AuthenticationMethod.LOCAL);

        // 3. case-insensitive match on role - false
        assertFalse(user.isUserOrRole(Collections.singleton("role4")));
    }

    @Test
    public void isUserOrRole_ReturnsTrue_WhenARoleIsInTheListOfUsersAndRoles() {
        Set<String> roles = new HashSet<>();
        roles.add("role1");
        roles.add("role2");
        roles.add("role3");
        user = new User("user@example.com", roles, AuthenticationMethod.LOCAL);

        // 3. case-insensitive match on role - true
        assertTrue(user.isUserOrRole(Collections.singleton("role2")));
    }

    @Test
    public void isUserOrRole_ReturnsFalse_WhenRoleDoesNotMatchARegex() {
        Set<String> roles = new HashSet<>();
        roles.add("group2-subgroup1");
        user = new User("user@example.com", roles, AuthenticationMethod.LOCAL);

        // 4. regex match on role - false
        assertFalse(user.isUserOrRole(Collections.singleton("^group1-.*$")));
    }

    @Test
    public void isUserOrRole_ReturnsTrue_WhenRoleMatchesARegex() {
        Set<String> roles = new HashSet<>();
        roles.add("group1-subgroup1");
        user = new User("user@example.com", roles, AuthenticationMethod.LOCAL);

        // 4. regex match on role - true
        assertTrue(user.isUserOrRole(Collections.singleton("^group1-.*$")));
    }

    @Test
    public void isAdmin() {
        Set<String> roles = new HashSet<>();
        roles.add("role1");
        user = new User("user@example.com", roles, AuthenticationMethod.LOCAL);

        Configuration.init();
        Configuration.getInstance().setAdminUsersAndRoles(new String[0]);
        assertFalse(user.isAdmin());

        Configuration.getInstance().setAdminUsersAndRoles(new String[] { "user@google.com" });
        assertFalse(user.isAdmin()); // not a named user

        Configuration.getInstance().setAdminUsersAndRoles(new String[] { "user@example.com" });
        assertTrue(user.isAdmin()); // a named user

        Configuration.getInstance().setAdminUsersAndRoles(new String[] { "role1" });
        assertTrue(user.isAdmin()); // a named role

        Configuration.getInstance().setAdminUsersAndRoles(new String[] { "role2" });
        assertFalse(user.isAdmin()); // not a named role

    }

}