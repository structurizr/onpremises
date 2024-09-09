package com.structurizr.onpremises.web.home;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.util.Features;
import com.structurizr.onpremises.util.HtmlUtils;
import com.structurizr.onpremises.web.AbstractController;
import com.structurizr.util.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class HomePageController extends AbstractController {

    private static final String SORT_DATE = "date";
    private static final String SORT_NAME = "name";

    private static final String DEFAULT_PAGE_NUMBER = "1";
    private static final String DEFAULT_PAGE_SIZE = "" + PaginatedWorkspaceList.DEFAULT_PAGE_SIZE;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showUnauthenticatedHomePage(
            @RequestParam(required = false, defaultValue = SORT_NAME) String sort,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGE_NUMBER) int pageNumber,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            ModelMap model) {

        if (isAuthenticated()) {
            return "redirect:/dashboard";
        }

        sort = determineSort(sort);
        List<WorkspaceMetaData> workspaces = sortAndPaginate(new ArrayList<>(workspaceComponent.getWorkspaces(null)), sort, pageNumber, pageSize, model);

        model.addAttribute("workspaces", workspaces);
        model.addAttribute("numberOfWorkspaces", workspaces.size());
        model.addAttribute("sort", sort);

        model.addAttribute("reviewsEnabled", Configuration.getInstance().isFeatureEnabled(Features.DIAGRAM_REVIEWS));

        addCommonAttributes(model, "", true);

        return "home";
    }

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedDashboard(
            @RequestParam(required = false, defaultValue = SORT_NAME) String sort,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGE_NUMBER) int pageNumber,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            ModelMap model) {

        sort = determineSort(sort);
        List<WorkspaceMetaData> workspaces = sortAndPaginate(new ArrayList<>(workspaceComponent.getWorkspaces(getUser())), sort, pageNumber, pageSize, model);

        model.addAttribute("workspaces", workspaces);
        model.addAttribute("numberOfWorkspaces", workspaces.size());
        model.addAttribute("sort", sort);

        model.addAttribute("userCanCreateWorkspace", Configuration.getInstance().getAdminUsersAndRoles().isEmpty() || getUser().isAdmin());
        model.addAttribute("reviewsEnabled", Configuration.getInstance().isFeatureEnabled(Features.DIAGRAM_REVIEWS));

        addCommonAttributes(model, "", true);

        return "dashboard";
    }

    private List<WorkspaceMetaData> sortAndPaginate(List<WorkspaceMetaData> workspaces, String sort, int pageNumber, int pageSize, ModelMap model) {
        if (SORT_DATE.equals(sort)) {
            workspaces.sort((wmd1, wmd2) -> wmd2.getLastModifiedDate().compareTo(wmd1.getLastModifiedDate()));
        } else {
            workspaces.sort(Comparator.comparing(wmd -> wmd.getName().toLowerCase()));
        }

        if (workspaces.isEmpty() || pageSize >= workspaces.size()) {
            return workspaces;
        } else {
            PaginatedWorkspaceList paginatedWorkspaceList = new PaginatedWorkspaceList(workspaces, pageNumber, pageSize);

            model.addAttribute("pageNumber", paginatedWorkspaceList.getPageNumber());
            if (paginatedWorkspaceList.hasPreviousPage()) {
                model.addAttribute("previousPage", pageNumber - 1);
            }
            if (paginatedWorkspaceList.hasNextPage()) {
                model.addAttribute("nextPage", pageNumber + 1);
            }

            model.addAttribute("maxPage", paginatedWorkspaceList.getMaxPage());

            model.addAttribute("pageSize", paginatedWorkspaceList.getPageSize());

            return paginatedWorkspaceList.getWorkspaces();
        }
    }

    private String determineSort(String sort) {
        sort = HtmlUtils.filterHtml(sort);

        if (!StringUtils.isNullOrEmpty(sort) && sort.trim().equals(SORT_DATE)) {
            sort = SORT_DATE;
        } else {
            sort = SORT_NAME;
        }

        return sort;
    }

}