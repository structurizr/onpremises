package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import org.springframework.ui.ModelMap;

public abstract class AbstractWorkspaceEditorController extends AbstractWorkspaceController {

    protected final String lockWorkspaceAndShowAuthenticatedView(String view, WorkspaceMetaData workspaceMetaData, String version, ModelMap model, boolean showHeaderAndFooter) {
        boolean locked = lockWorkspace(workspaceMetaData);

        if (!locked) {
            if (workspaceMetaData.isLocked()) {
                model.addAttribute("showHeader", true);
                model.addAttribute("showFooter", true);
                addCommonAttributes(model, "Workspace locked", true);
                model.addAttribute("workspace", workspaceMetaData);

                return showError("workspace-locked", model);
            } else {
                workspaceMetaData.setEditable(false);
                model.addAttribute("showHeader", true);
                model.addAttribute("showFooter", true);
                addCommonAttributes(model, "Workspace could not be locked", true);
                model.addAttribute("workspace", workspaceMetaData);

                return showError("workspace-could-not-be-locked", model);
            }
        } else {
            return showAuthenticatedView(view, workspaceMetaData, version, model, false, true);
        }
    }

}