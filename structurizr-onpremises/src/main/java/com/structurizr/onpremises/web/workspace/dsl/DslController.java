package com.structurizr.onpremises.web.workspace.dsl;

import com.structurizr.Workspace;
import com.structurizr.dsl.DslUtils;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.web.workspace.AbstractWorkspaceController;
import com.structurizr.util.WorkspaceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DslController extends AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(DslController.class);
    private static final String VIEW = "plaintext";

    @RequestMapping(value = "/share/{workspaceId}/dsl", method = RequestMethod.GET)
    public String showPublicDsl(
            @PathVariable("workspaceId") long workspaceId,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        try {
            String workspaceAsJson = workspaceComponent.getWorkspace(workspaceId, null, null);
            Workspace workspace = WorkspaceUtils.fromJson(workspaceAsJson);
            String dsl = DslUtils.getDsl(workspace);

            model.addAttribute("text", dsl);
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }

        return showPublicView(VIEW, workspaceId, model, false);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/dsl", method = RequestMethod.GET)
    public String showSharedDsl(
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
            Workspace workspace = WorkspaceUtils.fromJson(workspaceAsJson);
            String dsl = DslUtils.getDsl(workspace);

            model.addAttribute("text", dsl);
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }

        return showSharedView(VIEW, workspaceId, token, model, false);
    }

}