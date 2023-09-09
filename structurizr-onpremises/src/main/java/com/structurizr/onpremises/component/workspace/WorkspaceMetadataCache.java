package com.structurizr.onpremises.component.workspace;

interface WorkspaceMetadataCache {

    int DEFAULT_CACHE_EXPIRY_IN_MINUTES = 5;

    WorkspaceMetaData get(long workspaceId);

    void put(WorkspaceMetaData workspaceMetaData);

    void stop();

}