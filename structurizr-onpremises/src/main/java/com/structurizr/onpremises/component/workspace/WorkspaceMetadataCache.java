package com.structurizr.onpremises.component.workspace;

interface WorkspaceMetadataCache {

    WorkspaceMetaData get(long workspaceId);

    void put(WorkspaceMetaData workspaceMetaData);

    void stop();

}