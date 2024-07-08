package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ExploreController extends AbstractWorkspaceController {

    private static final String VIEW = "explore";

    @RequestMapping(value = "/share/{workspaceId}/explore", method = RequestMethod.GET)
    public String showPublicExplorePage(
            @PathVariable("workspaceId") long workspaceId,
            ModelMap model
    ) {
        return showPublicView(VIEW, workspaceId, model, true);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/explore", method = RequestMethod.GET)
    public String showSharedExplorePage(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        return showSharedView(VIEW, workspaceId, token, model, true);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/explore", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedExplorePage(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        return showAuthenticatedView(VIEW, workspaceMetaData, branch, version, model, true, false);
    }

}