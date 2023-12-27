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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
public class DecisionsController extends AbstractWorkspaceController {

    private static final String VIEW = "decisions";
    private static final String WORKSPACE_SCOPE = "*";

    @RequestMapping(value = "/share/{workspaceId}/decisions", method = RequestMethod.GET)
    public String showPublicDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        return showPublicDecisions(workspaceId, version, null, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/decisions/{softwareSystem}", method = RequestMethod.GET)
    public String showPublicDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            ModelMap model
    ) {
        return showPublicDecisions(workspaceId, version, softwareSystem, null, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/decisions/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showPublicDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            ModelMap model
    ) {
        return showPublicDecisions(workspaceId, version, softwareSystem, container, null, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/decisions/{softwareSystem}/{container}/{component}", method = RequestMethod.GET)
    public String showPublicDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            @PathVariable("component") String component,
            ModelMap model
    ) {
        model.addAttribute("scope", Base64.getEncoder().encodeToString(toScope(softwareSystem, container, component).getBytes(StandardCharsets.UTF_8)));
        model.addAttribute("showHeader", true);

        return showPublicView(VIEW, workspaceId, version, model, false);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/decisions", method = RequestMethod.GET)
    public String showSharedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        return showSharedDecisions(workspaceId, version, null, token, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/decisions/{softwareSystem}", method = RequestMethod.GET)
    public String showSharedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        return showSharedDecisions(workspaceId, version, softwareSystem, null, token, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/decisions/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showSharedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        return showSharedDecisions(workspaceId, version, softwareSystem, container, null, token, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/decisions/{softwareSystem}/{container}/{component}", method = RequestMethod.GET)
    public String showSharedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            @PathVariable("component") String component,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        model.addAttribute("scope", Base64.getEncoder().encodeToString(toScope(softwareSystem, container, component).getBytes(StandardCharsets.UTF_8)));
        model.addAttribute("showHeader", true);

        return showSharedView(VIEW, workspaceId, token, version, model, false);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/decisions", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        return showAuthenticatedDecisions(workspaceId, version, null, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/decisions/{softwareSystem}", method = RequestMethod.GET)
    public String showAuthenticatedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            ModelMap model
    ) {
        return showAuthenticatedDecisions(workspaceId, version, softwareSystem, null, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/decisions/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showAuthenticatedDecisions(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            ModelMap model
    ) {
        return showAuthenticatedDecisions(workspaceId, version, softwareSystem, container, null, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/decisions/{softwareSystem}/{container}/{component}", method = RequestMethod.GET)
    public String showAuthenticatedDecisions(
            @PathVariable("workspaceId") long workspaceId,
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

        return showAuthenticatedView(VIEW, workspaceMetaData, version, model, false, false);
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