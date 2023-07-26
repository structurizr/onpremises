package com.structurizr.onpremises.web.api;

import java.util.ArrayList;
import java.util.List;

public class WorkspacesApiResponse {

    private final List<WorkspaceApiResponse> workspaces = new ArrayList<>();

    WorkspacesApiResponse() {
    }

    void add(WorkspaceApiResponse war) {
        this.workspaces.add(war);
    }

    public List<WorkspaceApiResponse> getWorkspaces() {
        return new ArrayList<>(workspaces);
    }

}
