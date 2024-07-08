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
public class TreeController extends AbstractWorkspaceController {

    private static final String VIEW = "tree";

    @RequestMapping(value = "/share/{workspaceId}/explore/tree", method = RequestMethod.GET)
    public String showPublicTree(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String view,
            ModelMap model
    ) {
        model.addAttribute("view", view);

        return showPublicView(VIEW, workspaceId, model, true);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/explore/tree", method = RequestMethod.GET)
    public String showSharedTree(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String view,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        model.addAttribute("view", view);

        return showSharedView(VIEW, workspaceId, token, model, true);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/explore/tree", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedTree(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String view,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        model.addAttribute("view", view);

        return showAuthenticatedView(VIEW, workspaceMetaData, branch, version, model, true, false);
    }

}