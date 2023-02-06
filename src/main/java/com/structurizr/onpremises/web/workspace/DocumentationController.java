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
public class DocumentationController extends AbstractWorkspaceController {

    private static final String VIEW = "documentation";
    private static final String WORKSPACE_SCOPE = "*";

    @RequestMapping(value = "/share/{workspaceId}/documentation", method = RequestMethod.GET)
    public String showPublicDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        return showPublicDocumentation(workspaceId, version, null, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/documentation/{softwareSystem}", method = RequestMethod.GET)
    public String showPublicDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            ModelMap model
    ) {
        return showPublicDocumentation(workspaceId, version, softwareSystem, null, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/documentation/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showPublicDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            ModelMap model
    ) {
        model.addAttribute("scope", toScope(softwareSystem, container));
        model.addAttribute("showHeader", true);

        return showPublicView(VIEW, workspaceId, version, model, false);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/documentation", method = RequestMethod.GET)
    public String showSharedDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        return showSharedDocumentation(workspaceId, version, null, token, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/documentation/{softwareSystem}", method = RequestMethod.GET)
    public String showSharedDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        return showSharedDocumentation(workspaceId, version, softwareSystem, null, token, model);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/documentation/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showSharedDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        model.addAttribute("scope", toScope(softwareSystem, container));
        model.addAttribute("showHeader", true);

        return showSharedView(VIEW, workspaceId, token, version, model, false);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/documentation", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        return showAuthenticatedDocumentation(workspaceId, version, null, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/documentation/{softwareSystem}", method = RequestMethod.GET)
    public String showAuthenticatedDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            ModelMap model
    ) {
        return showAuthenticatedDocumentation(workspaceId, version, softwareSystem, null, model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/documentation/{softwareSystem}/{container}", method = RequestMethod.GET)
    public String showAuthenticatedDocumentation(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("softwareSystem") String softwareSystem,
            @PathVariable("container") String container,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        model.addAttribute("scope", toScope(softwareSystem, container));
        model.addAttribute("showHeader", true);

        return showAuthenticatedView(VIEW, workspaceMetaData, version, model, false, false);
    }

    String toScope(String softwareSystem, String container) {
        if (softwareSystem == null && container == null) {
            return WORKSPACE_SCOPE;
        } else if (container == null) {
            return HtmlUtils.filterHtml(softwareSystem);
        } else {
            return HtmlUtils.filterHtml(softwareSystem) + "/" + HtmlUtils.filterHtml(container);
        }
    }

}