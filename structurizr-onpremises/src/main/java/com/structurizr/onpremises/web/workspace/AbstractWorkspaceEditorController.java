package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.util.RandomGuidGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ui.ModelMap;

public abstract class AbstractWorkspaceEditorController extends AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(AbstractWorkspaceEditorController.class);

    private static final String AGENT = "structurizr-onpremises";

    protected final String lockWorkspaceAndShowAuthenticatedView(String view, WorkspaceMetaData workspaceMetaData, String branch, String version, ModelMap model, boolean showHeaderAndFooter) {
        boolean success = false;
        String agent = AGENT + "/" + view + "/" + new RandomGuidGenerator().generate();

        try {
            success = workspaceComponent.lockWorkspace(workspaceMetaData.getId(), getUser().getUsername(), agent);
        } catch (WorkspaceComponentException e) {
            log.error(e);
        }

        if (!success) {
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
            workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceMetaData.getId()); // refresh metadata
            model.addAttribute("userAgent", agent);
            return showAuthenticatedView(view, workspaceMetaData, branch, version, model, showHeaderAndFooter, true);
        }
    }

}