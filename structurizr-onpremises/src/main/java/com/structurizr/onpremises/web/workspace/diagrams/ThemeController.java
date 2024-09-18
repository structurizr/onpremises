package com.structurizr.onpremises.web.workspace.diagrams;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.web.workspace.AbstractWorkspaceController;
import com.structurizr.util.WorkspaceUtils;
import com.structurizr.view.ThemeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ThemeController extends AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(ThemeController.class);
    private static final String VIEW = "json";

    @RequestMapping(value = "/share/{workspaceId}/theme", method = RequestMethod.GET)
    public String showPublicTheme(
            @PathVariable("workspaceId") long workspaceId,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        try {
            String workspaceAsJson = workspaceComponent.getWorkspace(workspaceId, null, null);
            model.addAttribute("json", ThemeUtils.toJson(WorkspaceUtils.fromJson(workspaceAsJson)));
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }

        return showPublicView(VIEW, workspaceId, model, false);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/theme", method = RequestMethod.GET)
    public String showSharedTheme(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        try {
            String workspaceAsJson = workspaceComponent.getWorkspace(workspaceId, null, null);
            model.addAttribute("json", ThemeUtils.toJson(WorkspaceUtils.fromJson(workspaceAsJson)));
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }

        return showSharedView(VIEW, workspaceId, token, model, false);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/theme", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedTheme(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        try {
            String workspaceAsJson = workspaceComponent.getWorkspace(workspaceId, branch, version);
            model.addAttribute("json", ThemeUtils.toJson(WorkspaceUtils.fromJson(workspaceAsJson)));
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }

        return showAuthenticatedView(VIEW, workspaceMetaData, branch, version, model, false, false);
    }

}