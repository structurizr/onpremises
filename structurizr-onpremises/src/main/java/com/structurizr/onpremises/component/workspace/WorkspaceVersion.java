package com.structurizr.onpremises.component.workspace;

import java.util.Date;

public final class WorkspaceVersion {

    private String versionId;
    private final Date lastModifiedDate;

    public WorkspaceVersion(String versionId, Date lastModifiedDate) {
        this.versionId = versionId;
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getVersionId() {
        return versionId;
    }

    public void clearVersionId() {
        this.versionId = null;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

}