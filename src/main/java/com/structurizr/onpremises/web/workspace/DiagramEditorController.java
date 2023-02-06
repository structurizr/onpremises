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
public class DiagramEditorController extends AbstractWorkspaceController {

    private static final String VIEW = "diagrams";

    @RequestMapping(value = "/workspace/{workspaceId}/diagram-editor", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedDiagramEditor(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        model.addAttribute("publishThumbnails", true);
        model.addAttribute("quickNavigationPath", "diagram-editor");

        if (!workspaceMetaData.isOpen() && !workspaceMetaData.isWriteUser(getUser())) {
            if (workspaceMetaData.isReadUser(getUser())) {
                return "workspace-is-readonly";
            } else {
                return show404Page(model);
            }
        }

        return showAuthenticatedView(VIEW, workspaceMetaData, version, model, false, true);
    }

}