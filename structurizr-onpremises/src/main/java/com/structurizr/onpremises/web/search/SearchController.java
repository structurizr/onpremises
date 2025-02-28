package com.structurizr.onpremises.web.search;

import com.structurizr.Workspace;
import com.structurizr.onpremises.component.search.SearchComponent;
import com.structurizr.onpremises.component.search.SearchComponentException;
import com.structurizr.onpremises.component.search.SearchResult;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.util.HtmlUtils;
import com.structurizr.onpremises.configuration.StructurizrProperties;
import com.structurizr.onpremises.web.workspace.AbstractWorkspaceController;
import com.structurizr.util.StringUtils;
import com.structurizr.util.WorkspaceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.structurizr.onpremises.configuration.StructurizrProperties.SEARCH_IMPLEMENTATION;

@Controller
public class SearchController extends AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(SearchController.class);

    private static final int NUMBER_OF_THREADS = 20;

    @Autowired
    private SearchComponent searchComponent;

    private final ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    @PostConstruct
    public void rebuildSearchIndex() {
        // rebuild local (Lucene) search indexes on startup
        if (StructurizrProperties.SEARCH_VARIANT_LUCENE.equals(Configuration.getInstance().getProperty(SEARCH_IMPLEMENTATION))) {
            log.debug("Rebuilding search index...");

            try {
                Collection<WorkspaceMetaData> workspaces = workspaceComponent.getWorkspaces();
                for (WorkspaceMetaData workspaceMetaData : workspaces) {
                    executorService.submit(() -> {
                        try {
                            if (!workspaceMetaData.isClientEncrypted()) {
                                log.info("Indexing workspace with ID " + workspaceMetaData.getId());
                                String json = workspaceComponent.getWorkspace(workspaceMetaData.getId(), null, null);
                                Workspace workspace = WorkspaceUtils.fromJson(json);
                                searchComponent.index(workspace);
                            } else {
                                log.debug("Skipping workspace with ID " + workspaceMetaData.getId() + " because it's client-side encrypted");
                            }
                        } catch (Exception e) {
                            log.warn("Error indexing workspace with ID " + workspaceMetaData.getId(), e);
                        }
                    });
                }
            } catch (Exception e) {
                log.error("Error rebuilding search index", e);
            }
        }
    }

    @PreDestroy
    public void stop() {
        executorService.shutdownNow();
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(ModelMap model,
                         @RequestParam(required = false) String query,
                         @RequestParam(required = false) Long workspaceId,
                         @RequestParam(required = false) String category) {
        model.addAttribute("searchBaseUrl", "/");

        List<SearchResult> filteredSearchResults = new ArrayList<>();

        if (query != null) {
            query = HtmlUtils.filterHtml(query);
            query = query.replaceAll("\"", "");
        }

        if (category != null) {
            category = HtmlUtils.filterHtml(category);
            category = category.replaceAll("\"", "");
            category = category.toLowerCase();
        }

        Collection<WorkspaceMetaData> workspaces = workspaceComponent.getWorkspaces(getUser());
        if (!workspaces.isEmpty()) {
            Map<Long, WorkspaceMetaData> workspacesById = new HashMap<>();

            if (workspaceId == null) {
                for (WorkspaceMetaData workspace : workspaces) {
                    workspacesById.put(workspace.getId(), workspace);
                }
            } else {
                workspaces.stream().filter(w -> w.getId() == workspaceId).findFirst().ifPresent(w -> workspacesById.put(w.getId(), w));
            }

            if (!StringUtils.isNullOrEmpty(query)) {
                List<SearchResult> searchResults = new ArrayList<>();

                try {
                    searchResults = searchComponent.search(query, category, workspacesById.keySet());
                } catch (SearchComponentException e) {
                    log.error(e);
                }

                for (SearchResult searchResult : searchResults) {
                    if (workspacesById.containsKey(searchResult.getWorkspaceId())) {
                        searchResult.
                                setWorkspace(workspacesById.get(searchResult.getWorkspaceId()));
                        filteredSearchResults.add(searchResult);
                    }
                }
            }
        }

        model.addAttribute("query", query);
        model.addAttribute("workspaceId", workspaceId);
        model.addAttribute("category", category);
        model.addAttribute("results", filteredSearchResults);
        addCommonAttributes(model, "Search", true);

        return "search-results";
    }

}