package com.structurizr.onpremises.web.workspace.management;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.configuration.Features;
import com.structurizr.onpremises.web.workspace.AbstractWorkspaceController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WorkspaceSettingsController extends AbstractWorkspaceController {

    private static final String VIEW = "workspace-settings";

    @RequestMapping(value = "/workspace/{workspaceId}/settings", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedWorkspaceSettings(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        if (!Configuration.getInstance().isFeatureEnabled(Features.UI_WORKSPACE_SETTINGS)) {
            return showFeatureNotAvailablePage(model);
        }

        if (workspaceMetaData.hasNoUsersConfigured() || workspaceMetaData.isWriteUser(getUser())) {
            model.addAttribute("showAdminFeatures", Configuration.getInstance().getAdminUsersAndRoles().isEmpty() || getUser().isAdmin());
            return showAuthenticatedView(VIEW, workspaceMetaData, null, version, model, true, true);
        } else {
            return show404Page(model);
        }
    }

}