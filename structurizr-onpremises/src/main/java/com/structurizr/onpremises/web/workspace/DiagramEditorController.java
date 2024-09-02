package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.util.StringUtils;
import com.structurizr.view.PaperSize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DiagramEditorController extends AbstractWorkspaceEditorController {

    private static final String VIEW = "diagrams";

    @RequestMapping(value = "/workspace/{workspaceId}/diagram-editor", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedDiagramEditor(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        model.addAttribute("publishThumbnails", StringUtils.isNullOrEmpty(branch) && StringUtils.isNullOrEmpty(version));
        model.addAttribute("createReviews", true);
        model.addAttribute("quickNavigationPath", "diagram-editor");
        model.addAttribute("paperSizes", PaperSize.getOrderedPaperSizes());

        if (!workspaceMetaData.hasNoUsersConfigured() && !workspaceMetaData.isWriteUser(getUser())) {
            if (workspaceMetaData.isReadUser(getUser())) {
                return showError("workspace-is-readonly", model);
            } else {
                return show404Page(model);
            }
        }

        return lockWorkspaceAndShowAuthenticatedView(VIEW, workspaceMetaData, branch, version, model, false);
    }

}