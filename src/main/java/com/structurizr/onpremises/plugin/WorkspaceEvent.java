package com.structurizr.onpremises.plugin;

public final class WorkspaceEvent {

    private final long workspaceId;
    private String json;

    public WorkspaceEvent(long workspaceId, String json) {
        this.workspaceId = workspaceId;
        this.json = json;
    }

    public long getWorkspaceId() {
        return workspaceId;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

}