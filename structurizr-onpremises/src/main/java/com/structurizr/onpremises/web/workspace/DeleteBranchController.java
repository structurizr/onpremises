package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.util.Features;
import com.structurizr.onpremises.web.AbstractController;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DeleteBranchController extends AbstractController {

    private static final Log log = LogFactory.getLog(DeleteBranchController.class);

    @RequestMapping(value="/workspace/{workspaceId}/branch/{branch}/delete", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated()")
    public String deleteWorkspace(@PathVariable("workspaceId")long workspaceId,
                                  @PathVariable("branch")String branch,
                                  ModelMap model) {
        if (!Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
            return show404Page(model);
        }

        if (StringUtils.isNullOrEmpty(branch)) {
            return show404Page(model);
        }

        try {
            WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
            if (workspaceMetaData != null) {
                if (workspaceMetaData.hasUsersConfigured() && !workspaceMetaData.isWriteUser(getUser())) {
                    return show404Page(model);
                }

                workspaceComponent.deleteBranch(workspaceId, branch);
            } else {
                return show404Page(model);
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
        }

        return "redirect:/workspace/" + workspaceId;
    }

}