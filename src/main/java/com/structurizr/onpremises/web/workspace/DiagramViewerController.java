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
public class DiagramViewerController extends AbstractWorkspaceController {

    private static final String VIEW = "diagrams";

    @RequestMapping(value = "/share/{workspaceId}/diagrams", method = RequestMethod.GET)
    public String showPublicDiagramViewer(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String perspective,
            ModelMap model
    ) {
        model.addAttribute("publishThumbnails", false);
        model.addAttribute("quickNavigationPath", "diagrams");
        model.addAttribute("perspective", HtmlUtils.filterHtml(perspective));
        model.addAttribute("includeEditButton", false);

        return showPublicView(VIEW, workspaceId, version, model, false);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/diagrams", method = RequestMethod.GET)
    public String showSharedDiagramViewer(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String perspective,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        model.addAttribute("publishThumbnails", false);
        model.addAttribute("quickNavigationPath", "diagrams");
        model.addAttribute("perspective", HtmlUtils.filterHtml(perspective));
        model.addAttribute("includeEditButton", false);

        return showSharedView(VIEW, workspaceId, token, version, model, false);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/diagrams", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedDiagramViewer(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String perspective,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        model.addAttribute("publishThumbnails", true);
        model.addAttribute("createReviews", true);
        model.addAttribute("quickNavigationPath", "diagrams");
        model.addAttribute("perspective", HtmlUtils.filterHtml(perspective));

        boolean editable = workspaceMetaData.isOpen() || workspaceMetaData.isWriteUser(getUser());
        model.addAttribute("includeEditButton", editable);

        return showAuthenticatedView(VIEW, workspaceMetaData, version, model, false, false);
    }

}