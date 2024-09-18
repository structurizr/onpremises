package com.structurizr.onpremises.web.workspace.documentation;

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
public class DocumentationController extends AbstractWorkspaceController {

    private static final String VIEW = "documentation";
    private static final String WORKSPACE_SCOPE = "*";

    @RequestMapping(value = "/share/{workspaceId}/documentation", method = RequestMethod.GET)
    public String showPublicDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            ModelMap model
    ) {
        return showPublicDocumentation(workspaceId, null, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/documentation/{softwareSystem}", method = RequestMethod.GET)
    public String showPublicDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable("softwareSystem") String softwareSystem,
            ModelMap model
    ) {
        return showPublicDocumentation(workspaceId, softwareSystem, null, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/documentation/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showPublicDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            ModelMap model
    ) {
        return showPublicDocumentation(workspaceId, softwareSystem, container, null, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/documentation/{softwareSystem}/{container}/{component}", method = RequestMethod.GET)
    public String showPublicDocumentation(
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

    @RequestMapping(value = "/share/{workspaceId}/{token}/documentation", method = RequestMethod.GET)
    public String showSharedDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        return showSharedDocumentation(workspaceId, null, token, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/documentation/{softwareSystem}", method = RequestMethod.GET)
    public String showSharedDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        return showSharedDocumentation(workspaceId, softwareSystem, null, token, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/documentation/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showSharedDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        return showSharedDocumentation(workspaceId, softwareSystem, container, null, token, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/documentation/{softwareSystem}/{container}/{component}", method = RequestMethod.GET)
    public String showSharedDocumentation(
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

    @RequestMapping(value = "/workspace/{workspaceId}/documentation", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        return showAuthenticatedDocumentation(workspaceId, branch, version, null, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/documentation/{softwareSystem}", method = RequestMethod.GET)
    public String showAuthenticatedDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            ModelMap model
    ) {
        return showAuthenticatedDocumentation(workspaceId, branch, version, softwareSystem, null, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/documentation/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showAuthenticatedDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            ModelMap model
    ) {
        return showAuthenticatedDocumentation(workspaceId, branch, version, softwareSystem, container, null, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/documentation/{softwareSystem}/{container}/{component}", method = RequestMethod.GET)
    public String showAuthenticatedDocumentation(
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