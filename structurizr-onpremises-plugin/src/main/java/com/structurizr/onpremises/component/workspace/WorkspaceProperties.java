package com.structurizr.onpremises.component.workspace;

import com.structurizr.configuration.User;
import com.structurizr.configuration.Visibility;

import java.util.Date;
import java.util.Set;

public interface WorkspaceProperties {

    long getId();

    String getName();

    String getDescription();

    Date getLastModifiedDate();

    Visibility getVisibility();

    Set<User> getUsers();

}