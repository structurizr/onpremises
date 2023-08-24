package com.structurizr.onpremises.web.dashboard;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.util.HtmlUtils;
import com.structurizr.onpremises.web.AbstractController;
import com.structurizr.util.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
public class DashboardController extends AbstractController {

    private static final String SORT_DATE = "date";
    private static final String SORT_NAME = "name";

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String show(@RequestParam(required = false) String sort, ModelMap model) {
        model.addAttribute("urlPrefix", "/workspace");
        model.addAttribute("showAdminFeatures", Configuration.getInstance().getAdminUsersAndRoles().isEmpty() || getUser().isAdmin());

        List<WorkspaceMetaData> workspaces = new ArrayList<>(workspaceComponent.getWorkspaces(getUser()));

        sort = HtmlUtils.filterHtml(sort);
        if (!StringUtils.isNullOrEmpty(sort) && sort.trim().equals(SORT_DATE)) {
            Collections.sort(workspaces, (wmd1, wmd2) -> wmd2.getLastModifiedDate().compareTo(wmd1.getLastModifiedDate()));

            sort = SORT_DATE;
        } else {
            Collections.sort(workspaces, Comparator.comparing(wmd -> wmd.getName().toLowerCase()));

            sort = SORT_NAME;
        }

        model.addAttribute("workspaces", workspaces);
        model.addAttribute("numberOfWorkspaces", workspaces.size());
        model.addAttribute("sort", sort);
        addCommonAttributes(model, "", true);

        return "dashboard";
    }

}