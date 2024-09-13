package com.structurizr.onpremises.web.workspace;

import com.structurizr.Workspace;
import com.structurizr.dsl.DslUtils;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.dsl.StructurizrDslParserException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.util.HtmlUtils;
import com.structurizr.onpremises.util.WorkspaceValidationUtils;
import com.structurizr.util.DslTemplate;
import com.structurizr.util.StringUtils;
import com.structurizr.util.WorkspaceUtils;
import com.structurizr.validation.WorkspaceScopeValidationException;
import com.structurizr.validation.WorkspaceScopeValidatorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@PreAuthorize("isAuthenticated()")
public class DslEditorController extends AbstractWorkspaceEditorController {

    private static final Log log = LogFactory.getLog(DslEditorController.class);

    protected static final String VIEW = "dsl-editor";

    @RequestMapping(value = "/workspace/{workspaceId}/dsl", method = RequestMethod.GET)
    public String showAuthenticatedDslEditor(
            @PathVariable("workspaceId") long workspaceId,
            @RequestParam(required = false, defaultValue = "") String branch,
            @RequestParam(required = false) String version,
            ModelMap model
    ) {
        if (!Configuration.getInstance().isDslEditorEnabled()) {
            return showError("dsl-editor-disabled", model);
        }

        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        if (workspaceMetaData.isClientEncrypted()) {
            return showError("workspace-is-client-side-encrypted", model);
        }

        model.addAttribute("publishThumbnails", StringUtils.isNullOrEmpty(branch) && StringUtils.isNullOrEmpty(version));
        model.addAttribute("quickNavigationPath", "diagram-editor");
        try {
            model.addAttribute("dslVersion", Class.forName(StructurizrDslParser.class.getCanonicalName()).getPackage().getImplementationVersion());
        } catch (ClassNotFoundException e) {
            // ignore
        }

        if (!workspaceMetaData.hasNoUsersConfigured() && !workspaceMetaData.isWriteUser(getUser())) {
            if (workspaceMetaData.isReadUser(getUser())) {
                return showError("workspace-is-readonly", model);
            } else {
                return show404Page(model);
            }
        }

        return lockWorkspaceAndShowAuthenticatedView(VIEW, workspaceMetaData, branch, version, model, false);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/dsl", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public DslEditorResponse postToDslEditor(
            @PathVariable("workspaceId") long workspaceId,
            @RequestBody String json
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return new DslEditorResponse(false, "404");
        }

        User user = getUser();
        if (!workspaceMetaData.hasNoUsersConfigured() && !workspaceMetaData.isWriteUser(getUser())) {
            return new DslEditorResponse(false, "404");
        }

        try {
            Workspace oldWorkspace = WorkspaceUtils.fromJson(json);

            String dsl = DslUtils.getDsl(oldWorkspace);
            if (StringUtils.isNullOrEmpty(dsl)) {
                dsl = DslTemplate.generate(workspaceMetaData.getName(), workspaceMetaData.getDescription());
            }

            try {
                Workspace newWorkspace = fromDsl(dsl);
                newWorkspace.setId(workspaceId);
                newWorkspace.setLastModifiedDate(new Date());
                newWorkspace.getViews().copyLayoutInformationFrom(oldWorkspace.getViews());

                newWorkspace.getViews().getConfiguration().copyConfigurationFrom(oldWorkspace.getViews().getConfiguration());

                return new DslEditorResponse(WorkspaceUtils.toJson(newWorkspace, false));
            } catch (StructurizrDslParserException e) {
                return new DslEditorResponse(false, e.getMessage(), e.getLineNumber());
            } catch (WorkspaceScopeValidationException e) {
                return new DslEditorResponse(false, e.getMessage());
            }
        } catch (Exception e) {
            if (!(e instanceof StructurizrDslParserException)) {
                log.error("Error converting DSL to JSON", e);
            }

            return new DslEditorResponse(false, e.getMessage());
        }
    }

    private Workspace fromDsl(String dsl) throws StructurizrDslParserException, WorkspaceScopeValidationException {
        StructurizrDslParser parser = new StructurizrDslParser();
        parser.setRestricted(true);
        parser.parse(dsl);

        Workspace workspace = parser.getWorkspace();
        DslUtils.setDsl(workspace, dsl);

        // add default views if no views are explicitly defined
        if (!workspace.getModel().isEmpty() && workspace.getViews().isEmpty()) {
            workspace.getViews().createDefaultViews();
        }

        WorkspaceValidationUtils.validateWorkspaceScope(workspace);

        return workspace;
    }

}