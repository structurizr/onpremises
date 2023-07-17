package com.structurizr.onpremises.web.admin;

import com.structurizr.Workspace;
import com.structurizr.onpremises.component.search.SearchComponent;
import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.web.AbstractController;
import com.structurizr.util.WorkspaceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collection;

@Controller
public class RebuildSearchIndexController extends AbstractController {

    private static final Log log = LogFactory.getLog(RebuildSearchIndexController.class);

    private SearchComponent searchComponent;

    @Autowired
    public void setSearchComponent(SearchComponent searchComponent) {
        this.searchComponent = searchComponent;
    }

    @RequestMapping(value="/admin/rebuild-search-index", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String deleteWorkspace(ModelMap model) {
        Configuration configuration = Configuration.getInstance();
        User user = getUser();

        if (configuration.getAdminUsersAndRoles().isEmpty() || user.isAdmin()) {
            log.info("Rebuilding search index...");
            try {
                Collection<WorkspaceMetaData> workspaces = workspaceComponent.getWorkspaces();
                for (WorkspaceMetaData workspaceMetaData : workspaces) {
                    try {
                        if (!workspaceMetaData.isClientEncrypted()) {
                            log.info("Indexing workspace with ID " + workspaceMetaData.getId());
                            String json = workspaceComponent.getWorkspace(workspaceMetaData.getId(), null);
                            Workspace workspace = WorkspaceUtils.fromJson(json);
                            searchComponent.index(workspace);
                        } else {
                            log.info("Skipping workspace with ID " + workspaceMetaData.getId() + " because it's client-side encrypted");
                        }
                    } catch (Exception e) {
                        log.error(e);
                    }
                }
            } catch (WorkspaceComponentException e) {
                log.error(e);
            }
        } else {
            return show404Page(model);
        }

        return "redirect:/dashboard";
    }

}