package com.structurizr.onpremises.domain;

public final class UserType {

    public UserType() {
    }

    public boolean isAllowedToShareWorkspacesWithLink() {
        return true;
    }

    public boolean isAllowedToUseImageEmbed() {
        return false;
    }

    public boolean isAllowedToLockWorkspaces() {
        return true;
    }

}