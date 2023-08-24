package com.structurizr.onpremises.component.workspace;

public interface WorkspaceEvent {

    WorkspaceProperties getWorkspaceProperties();

    String getJson();

    void setJson(String json);

}