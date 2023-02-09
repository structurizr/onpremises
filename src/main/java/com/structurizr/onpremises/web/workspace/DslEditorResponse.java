package com.structurizr.onpremises.web.workspace;

public final class DslEditorResponse {

    private final boolean success;
    private final String message;
    private final String workspace;

    public DslEditorResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.workspace = null;
    }

    public DslEditorResponse(String workspace) {
        this.success = true;
        this.workspace = workspace;
        this.message = null;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getWorkspace() {
        return workspace;
    }

}