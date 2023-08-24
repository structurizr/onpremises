package com.structurizr.onpremises.web.security;

import com.structurizr.onpremises.domain.Role;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

public class FileBasedUserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private static Log log = LogFactory.getLog(FileBasedUserDetailsService.class);

    private static final String STRUCTURIZR_USERS_FILENAME = "structurizr.users";
    private static final String STRUCTURIZR_ROLES_FILENAME = "structurizr.roles";
    private static final String DEFAULT_USERNAME = "structurizr";
    private static final String DEFAULT_PASSWORD = "$2a$06$uM5wM.eJwrPq1RM/gBXRr.d0bfyu9ABxdE56qYbRLSCZzqfR7xHcC"; // password

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Properties properties = new Properties();

        try {
            File file = new File(Configuration.getInstance().getDataDirectory(), STRUCTURIZR_USERS_FILENAME);
            if (!file.exists()) {
                properties.setProperty(DEFAULT_USERNAME, DEFAULT_PASSWORD);
                FileWriter fileWriter = new FileWriter(file);
                properties.store(fileWriter, "");
                fileWriter.flush();
                fileWriter.close();
            }

            FileReader fileReader = new FileReader(file);
            properties.load(fileReader);
            fileReader.close();

            if (properties.containsKey(username)) {
                return new User(username, properties.getProperty(username), loadGroupsForUser(username));
            } else {
                throw new UsernameNotFoundException("Could not find user with username " + username);
            }
        } catch (IOException e) {
            log.error(e);

            throw new UsernameNotFoundException("Could not find user with username " + username, e);
        }
    }

    private Collection<? extends GrantedAuthority> loadGroupsForUser(String username) {
        Collection<Role> roles = new HashSet<>();

        Properties properties = new Properties();

        try {
            File file = new File(Configuration.getInstance().getDataDirectory(), STRUCTURIZR_ROLES_FILENAME);
            if (file.exists()) {
                FileReader fileReader = new FileReader(file);
                properties.load(fileReader);
                fileReader.close();
            }

            if (properties.containsKey(username)) {
                String allRoles = properties.getProperty(username);
                if (!StringUtils.isNullOrEmpty(allRoles)) {
                    for (String role : allRoles.split(",")) {
                        if (!StringUtils.isNullOrEmpty(role)) {
                            roles.add(new Role(role.trim()));
                        }
                    }
                }
            }

        } catch (IOException e) {
            log.warn(e);
        }

        return roles;
    }

}