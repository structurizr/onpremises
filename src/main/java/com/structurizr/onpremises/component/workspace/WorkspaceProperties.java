package com.structurizr.onpremises.component.workspace;

import com.structurizr.configuration.Role;
import com.structurizr.configuration.User;
import com.structurizr.configuration.Visibility;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

public final class WorkspaceProperties {

    private final long id;
    private final String name;
    private final String description;
    private final Visibility visibility;

    private final Set<User> users;

    private final Date lastModifiedDate;

    public WorkspaceProperties(WorkspaceMetaData workspaceMetaData) {
        this.id = workspaceMetaData.getId();
        this.name = workspaceMetaData.getName();
        this.description = workspaceMetaData.getDescription();

        this.users = new LinkedHashSet<>();
        for (String user : workspaceMetaData.getReadUsers()) {
            users.add(new User(user, Role.ReadOnly));
        }
        for (String user : workspaceMetaData.getWriteUsers()) {
            users.add(new User(user, Role.ReadWrite));
        }

        if (workspaceMetaData.isPublicWorkspace()) {
            this.visibility = Visibility.Public;
        } else {
            this.visibility = Visibility.Private;
        }

        this.lastModifiedDate = workspaceMetaData.getLastModifiedDate();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public Set<User> getUsers() {
        return new LinkedHashSet<>(users);
    }

}