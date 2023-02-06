package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.util.HtmlUtils;
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
            @RequestParam(required = false) String version,
            @RequestParam(required = true) String view,
            ModelMap model
    ) {
        model.addAttribute("view", view);

        return showPublicView(VIEW, workspaceId, version, model, true);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/explore/graph", method = RequestMethod.GET)
    public String showSharedGraph(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @RequestParam(required = true) String view,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        model.addAttribute("view", view);

        return showSharedView(VIEW, workspaceId, token, version, model, true);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/explore/graph", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedGraph(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @RequestParam(required = true) String view,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        model.addAttribute("view", view);

        return showAuthenticatedView(VIEW, workspaceMetaData, version, model, true, false);
    }

}