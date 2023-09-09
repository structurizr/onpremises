package com.structurizr.onpremises.component.workspace;

/**
 * Workspace metadata cache implementation that does nothing.
 */
class NoOpWorkspaceMetadataCache implements WorkspaceMetadataCache {

    @Override
    public WorkspaceMetaData get(long workspaceId) {
        return null;
    }

    @Override
    public void put(WorkspaceMetaData workspaceMetaData) {
    }

    @Override
    public void stop() {
    }

}