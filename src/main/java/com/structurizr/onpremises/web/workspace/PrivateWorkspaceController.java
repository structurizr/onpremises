package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.web.AbstractController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class PrivateWorkspaceController extends AbstractController {

    private static final Log log = LogFactory.getLog(PrivateWorkspaceController.class);

    @RequestMapping(value="/workspace/{workspaceId}/private", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated()")
    public String makeWorkspacePrivate(@PathVariable("workspaceId")long workspaceId, ModelMap model) {
        try {
            WorkspaceMetaData workspace = workspaceComponent.getWorkspaceMetaData(workspaceId);
            if (workspace != null) {
                if (workspace.hasNoUsersConfigured() || workspace.isWriteUser(getUser())) {
                    workspaceComponent.makeWorkspacePrivate(workspaceId);
                }
            } else {
                return show404Page(model);
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
        }

        return "redirect:/workspace/" + workspaceId + "/settings";
    }

}