package com.structurizr.onpremises.web.workspace.explore;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.web.workspace.AbstractWorkspaceController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GraphController extends AbstractWorkspaceController {

    private static final String VIEW = "graph";

    @RequestMapping(value = "/share/{workspaceId}/explore/graph", method = RequestMethod.GET)
    public String showPublicGraph(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String view,
            ModelMap model
    ) {
        model.addAttribute("view", view);

        return showPublicView(VIEW, workspaceId, model, true);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/explore/graph", method = RequestMethod.GET)
    public String showSharedGraph(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String view,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        model.addAttribute("view", view);

        return showSharedView(VIEW, workspaceId, token, model, true);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/explore/graph", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedGraph(
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