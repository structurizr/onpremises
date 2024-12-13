package com.structurizr.onpremises.component.search;

import com.structurizr.Workspace;
import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.configuration.StructurizrProperties;

import java.util.List;
import java.util.Set;

import static com.structurizr.onpremises.configuration.StructurizrProperties.SEARCH_IMPLEMENTATION;

class SearchComponentImpl implements SearchComponent {

    private final SearchComponent searchComponent;

    SearchComponentImpl() {
        String searchImplementation = Configuration.getInstance().getProperty(SEARCH_IMPLEMENTATION);
        if (StructurizrProperties.SEARCH_VARIANT_ELASTICSEARCH.equals(searchImplementation)) {
            searchComponent = new ElasticSearchComponentImpl();
        } else if (StructurizrProperties.SEARCH_VARIANT_NONE.equalsIgnoreCase(searchImplementation)) {
            searchComponent = new NoOpSearchComponentImpl();
        } else {
            searchComponent = new ApacheLuceneSearchComponentImpl(Configuration.getInstance().getDataDirectory());
        }
    }

    SearchComponentImpl(SearchComponent searchComponent) {
        this.searchComponent = searchComponent;
    }

    @Override
    public void start() {
        searchComponent.start();
    }

    @Override
    public void stop() {
        searchComponent.stop();
    }

    @Override
    public boolean isEnabled() {
        return searchComponent.isEnabled();
    }

    @Override
    public void index(Workspace workspace) throws SearchComponentException {
        searchComponent.index(workspace);
    }

    @Override
    public List<SearchResult> search(String query, String type, Set<Long> workspaceIds) throws SearchComponentException {
        return searchComponent.search(query, type, workspaceIds);
    }

    @Override
    public void delete(long workspaceId) throws SearchComponentException {
        searchComponent.delete(workspaceId);
    }

}