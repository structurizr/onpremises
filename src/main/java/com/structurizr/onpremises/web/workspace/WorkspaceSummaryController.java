package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.util.Configuration;
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
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        model.addAttribute("versions", workspaceComponent.getWorkspaceVersions(workspaceId, Configuration.getInstance().getMaxWorkspaceVersions()));

        return showPublicView(VIEW, workspaceId, version, model, true);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}", method = RequestMethod.GET)
    public String showSharedWorkspaceSummary(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        model.addAttribute("versions", workspaceComponent.getWorkspaceVersions(workspaceId, Configuration.getInstance().getMaxWorkspaceVersions()));

        return showSharedView(VIEW, workspaceId, token, version, model, true);
    }

    @RequestMapping(value = "/workspace/{workspaceId}", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedWorkspaceSummary(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        model.addAttribute("versions", workspaceComponent.getWorkspaceVersions(workspaceId, Configuration.getInstance().getMaxWorkspaceVersions()));

        boolean editable = workspaceMetaData.hasNoUsersConfigured() || workspaceMetaData.isWriteUser(getUser());

        return showAuthenticatedView(VIEW, workspaceMetaData, version, model, true, editable);
    }

}