package com.structurizr.onpremises.component.search;

import com.structurizr.Workspace;
import com.structurizr.onpremises.util.Configuration;

import java.util.List;
import java.util.Set;

class SearchComponentImpl implements SearchComponent {

    private static final String ELASTICSEARCH_PROTOCOL_PROPERTY = "elasticsearch.protocol";
    private static final String ELASTICSEARCH_HOST_PROPERTY = "elasticsearch.host";
    private static final String ELASTICSEARCH_PORT_PROPERTY = "elasticsearch.port";
    private static final String ELASTICSEARCH_USERNAME_PROPERTY = "elasticsearch.username";
    private static final String ELASTICSEARCH_PASSWORD_PROPERTY = "elasticsearch.password";

    private final SearchComponent searchComponent;

    SearchComponentImpl() {
        String searchImplementation = Configuration.getInstance().getSearchImplementationName();
        if (ELASTICSEARCH.equals(searchImplementation)) {
            String protocol = Configuration.getConfigurationParameterFromStructurizrPropertiesFile(ELASTICSEARCH_PROTOCOL_PROPERTY, "http");
            String host = Configuration.getConfigurationParameterFromStructurizrPropertiesFile(ELASTICSEARCH_HOST_PROPERTY, "localhost");
            String port = Configuration.getConfigurationParameterFromStructurizrPropertiesFile(ELASTICSEARCH_PORT_PROPERTY, "9200");
            String username = Configuration.getConfigurationParameterFromStructurizrPropertiesFile(ELASTICSEARCH_USERNAME_PROPERTY, "");
            String password = Configuration.getConfigurationParameterFromStructurizrPropertiesFile(ELASTICSEARCH_PASSWORD_PROPERTY, "");

            searchComponent = new ElasticSearchComponentImpl(host, Integer.parseInt(port), protocol, username, password);
        } else if (NONE.equalsIgnoreCase(searchImplementation)) {
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