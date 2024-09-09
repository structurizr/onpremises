package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceBranch;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.util.Features;
import com.structurizr.util.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WorkspaceSummaryController extends AbstractWorkspaceController {

    private static final String VIEW = "workspace-summary";

    @RequestMapping(value = "/share/{workspaceId}", method = RequestMethod.GET)
    public String showPublicWorkspaceSummary(
            @PathVariable("workspaceId") long workspaceId,
            ModelMap model
    ) {
        model.addAttribute("branch", "");
        model.addAttribute("versions", workspaceComponent.getWorkspaceVersions(workspaceId, null, Configuration.getInstance().getMaxWorkspaceVersions()));

        return showPublicView(VIEW, workspaceId, model, true);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}", method = RequestMethod.GET)
    public String showSharedWorkspaceSummary(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        model.addAttribute("branch", "");
        model.addAttribute("versions", workspaceComponent.getWorkspaceVersions(workspaceId, null, Configuration.getInstance().getMaxWorkspaceVersions()));

        return showSharedView(VIEW, workspaceId, token, model, true);
    }

    @RequestMapping(value = "/workspace/{workspaceId}", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedWorkspaceSummary(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false, defaultValue = "") String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        if (Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
            if (WorkspaceBranch.isMainBranch(branch)) {
                branch = "";
            }
            model.addAttribute("branchesEnabled", true);
            model.addAttribute("branch", branch);
            model.addAttribute("branches", workspaceComponent.getWorkspaceBranches(workspaceId));
        }

        if (!StringUtils.isNullOrEmpty(branch) && !Configuration.getInstance().isFeatureEnabled(Features.WORKSPACE_BRANCHES)) {
            return showError("workspace-branches-not-enabled", model);
        }

        model.addAttribute("versions", workspaceComponent.getWorkspaceVersions(workspaceId, branch, Configuration.getInstance().getMaxWorkspaceVersions()));

        model.addAttribute("reviewsEnabled", Configuration.getInstance().isFeatureEnabled(Features.DIAGRAM_REVIEWS));

        boolean editable = workspaceMetaData.hasNoUsersConfigured() || workspaceMetaData.isWriteUser(getUser());

        return showAuthenticatedView(VIEW, workspaceMetaData, branch, version, model, true, editable);
    }

}