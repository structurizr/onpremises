package com.structurizr.dsl;

import com.structurizr.Workspace;

public class DslBridge {

    // todo remove this after structurizr-dsl:1.26.0
    public static void setDsl(Workspace workspace, String dsl) {
        DslUtils.setDsl(workspace, dsl);
    }

}