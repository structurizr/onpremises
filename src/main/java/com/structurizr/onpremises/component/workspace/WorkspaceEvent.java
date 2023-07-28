package com.structurizr.onpremises.component.workspace;

public final class WorkspaceEvent {

    private final long workspaceId;
    private final WorkspaceProperties workspaceProperties;
    private String json;

    WorkspaceEvent(WorkspaceMetaData workspaceMetaData, String json) {
        this.workspaceId = workspaceMetaData.getId();
        this.workspaceProperties = new WorkspaceProperties(workspaceMetaData);
        this.json = json;
    }

    public long getWorkspaceId() {
        return workspaceId;
    }

    public WorkspaceProperties getWorkspaceProperties() {
        return workspaceProperties;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

}