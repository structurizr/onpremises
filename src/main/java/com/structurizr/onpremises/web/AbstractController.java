package com.structurizr.onpremises.web;

import com.structurizr.onpremises.component.search.SearchComponent;
import com.structurizr.onpremises.component.workspace.WorkspaceComponent;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.util.Version;
import com.structurizr.onpremises.web.security.SecurityUtils;
import com.structurizr.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;

public abstract class AbstractController {

    protected WorkspaceComponent workspaceComponent;
    protected SearchComponent searchComponent;

    @Autowired
    public void setWorkspaceComponent(WorkspaceComponent workspaceComponent) {
        this.workspaceComponent = workspaceComponent;
    }

    @Autowired
    public void setSearchComponent(SearchComponent searchComponent) {
        this.searchComponent = searchComponent;
    }

    @ModelAttribute("structurizrConfiguration")
    public Configuration getConfiguration() {
        return Configuration.getInstance();
    }

    @ModelAttribute
    protected void addSecurityHeaders(HttpServletResponse response) {
        response.addHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    @ModelAttribute
    protected void addXFrameOptionsHeader(HttpServletRequest request, HttpServletResponse response) {
        response.addHeader("X-Frame-Options", "sameorigin");
    }

    protected void addCommonAttributes(ModelMap model, String pageTitle, boolean showHeaderAndFooter) {
        model.addAttribute("timeZone", TimeZone.getDefault().getID());
        model.addAttribute("showHeader", showHeaderAndFooter);
        model.addAttribute("showFooter", showHeaderAndFooter);
        model.addAttribute("version", new Version());
        model.addAttribute("authenticated", isAuthenticated());
        model.addAttribute("user", getUser());
        model.addAttribute("searchEnabled", searchComponent != null && searchComponent.isEnabled());

        if (StringUtils.isNullOrEmpty(pageTitle)) {
            model.addAttribute("pageTitle", "Structurizr");
        } else {
            model.addAttribute("pageTitle", "Structurizr - " + pageTitle);
        }
    }

    protected String show404Page(ModelMap model) {
        addCommonAttributes(model, "Not found", true);

        return "404";
    }

    protected String show500Page(ModelMap model) {
        addCommonAttributes(model, "Error", true);

        return "500";
    }

    protected final User getUser() {
        return SecurityUtils.getUser();
    }

    protected final boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        if (authentication instanceof AnonymousAuthenticationToken) {
            return false;
        } else {
            return authentication.isAuthenticated();
        }
    }

    protected final Collection<WorkspaceMetaData> getWorkspaces() {
        Collection<WorkspaceMetaData> workspaces = workspaceComponent.getWorkspaces();
        List<WorkspaceMetaData> filteredWorkspaces = new ArrayList<>();
        User user = getUser();

        if (!isAuthenticated()) {
            for (WorkspaceMetaData workspace : workspaces) {
                if (workspace.isOpen()) {
                    // the workspace is public, so anybody can see it
                    workspace.setEditable(false);
                    filteredWorkspaces.add(workspace);
                }
            }
        } else {
            for (WorkspaceMetaData workspace : workspaces) {
                if (workspace.isOpen()) {
                    // the workspace is public, so anybody can see it
                    workspace.setEditable(isAuthenticated());
                    filteredWorkspaces.add(workspace);
                } else if (workspace.isWriteUser(user)) {
                    // the user has read-write access to the workspace
                    workspace.setEditable(true);
                    filteredWorkspaces.add(workspace);
                } else if (workspace.isReadUser(user)) {
                    // the user has read-only access to the workspace
                    workspace.setEditable(false);
                    filteredWorkspaces.add(workspace);
                }
            }
        }

        return filteredWorkspaces;
    }

    protected final WorkspaceMetaData getWorkspace(long workspaceId) {
        Collection<WorkspaceMetaData> workspaces = getWorkspaces();

        for (WorkspaceMetaData workspace : workspaces) {
            if (workspace.getId() == workspaceId) {
                return workspace;
            }
        }

        return null;
    }

}