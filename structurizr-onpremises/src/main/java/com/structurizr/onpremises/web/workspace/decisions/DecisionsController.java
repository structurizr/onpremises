package com.structurizr.onpremises.web.workspace.decisions;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.web.workspace.AbstractWorkspaceController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
public class DecisionsController extends AbstractWorkspaceController {

    private static final String VIEW = "decisions";
    private static final String WORKSPACE_SCOPE = "*";

    @RequestMapping(value = "/share/{workspaceId}/decisions", method = RequestMethod.GET)
    public String showPublicDecisions(
            @PathVariable("workspaceId") long workspaceId,
            ModelMap model
    ) {
        return showPublicDecisions(workspaceId, null, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/decisions/{softwareSystem}", method = RequestMethod.GET)
    public String showPublicDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable("softwareSystem") String softwareSystem,
            ModelMap model
    ) {
        return showPublicDecisions(workspaceId, softwareSystem, null, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/decisions/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showPublicDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            ModelMap model
    ) {
        return showPublicDecisions(workspaceId, softwareSystem, container, null, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/decisions/{softwareSystem}/{container}/{component}", method = RequestMethod.GET)
    public String showPublicDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            @PathVariable("component") String component,
            ModelMap model
    ) {
        model.addAttribute("scope", Base64.getEncoder().encodeToString(toScope(softwareSystem, container, component).getBytes(StandardCharsets.UTF_8)));
        model.addAttribute("showHeader", true);

        return showPublicView(VIEW, workspaceId, model, false);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/decisions", method = RequestMethod.GET)
    public String showSharedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        return showSharedDecisions(workspaceId, null, token, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/decisions/{softwareSystem}", method = RequestMethod.GET)
    public String showSharedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        return showSharedDecisions(workspaceId, softwareSystem, null, token, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/decisions/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showSharedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        return showSharedDecisions(workspaceId, softwareSystem, container, null, token, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/decisions/{softwareSystem}/{container}/{component}", method = RequestMethod.GET)
    public String showSharedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            @PathVariable("component") String component,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        model.addAttribute("scope", Base64.getEncoder().encodeToString(toScope(softwareSystem, container, component).getBytes(StandardCharsets.UTF_8)));
        model.addAttribute("showHeader", true);

        return showSharedView(VIEW, workspaceId, token, model, false);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/decisions", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        return showAuthenticatedDecisions(workspaceId, branch, version, null, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/decisions/{softwareSystem}", method = RequestMethod.GET)
    public String showAuthenticatedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            ModelMap model
    ) {
        return showAuthenticatedDecisions(workspaceId, branch, version, softwareSystem, null, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/decisions/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showAuthenticatedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            ModelMap model
    ) {
        return showAuthenticatedDecisions(workspaceId, branch, version, softwareSystem, container, null, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/decisions/{softwareSystem}/{container}/{component}", method = RequestMethod.GET)
    public String showAuthenticatedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            @PathVariable("component") String component,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        model.addAttribute("scope", Base64.getEncoder().encodeToString(toScope(softwareSystem, container, component).getBytes(StandardCharsets.UTF_8)));
        model.addAttribute("showHeader", true);

        return showAuthenticatedView(VIEW, workspaceMetaData, branch, version, model, false, false);
    }

    String toScope(String softwareSystem, String container, String component) {
        if (softwareSystem != null && container != null && component != null) {
            return softwareSystem + "/" + container + "/" + component;
        } else if (softwareSystem != null && container != null) {
            return softwareSystem + "/" + container;
        } else if (softwareSystem != null) {
            return softwareSystem;
        } else {
            return WORKSPACE_SCOPE;
        }
    }
    
}