package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class JsonController extends AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(JsonController.class);
    private static final String VIEW = "json";

    @RequestMapping(value = "/share/{workspaceId}/json", method = RequestMethod.GET)
    public String showPublicJson(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        try {
            String workspaceAsJson = workspaceComponent.getWorkspace(workspaceId, version);
            model.addAttribute("json", workspaceAsJson);
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }

        return showPublicView(VIEW, workspaceId, version, model, false);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/json", method = RequestMethod.GET)
    public String showSharedJson(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false) String version,
            @PathVariable("token") String token,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        try {
            String workspaceAsJson = workspaceComponent.getWorkspace(workspaceId, version);
            model.addAttribute("json", workspaceAsJson);
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }

        return showSharedView(VIEW, workspaceId, token, version, model, false);
    }

}