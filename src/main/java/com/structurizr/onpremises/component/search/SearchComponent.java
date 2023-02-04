package com.structurizr.onpremises.component.search;

import com.structurizr.Workspace;

import java.util.List;
import java.util.Set;

/**
 * Provides search facilities for workspaces.
 */
public interface SearchComponent {

    String NONE = "none";
    String LUCENE = "lucene";
    String ELASTICSEARCH = "elasticsearch";

    void start();

    void stop();

    boolean isEnabled();

    void index(Workspace workspace);

    List<SearchResult> search(String query, String type, Set<Long> workspaceIds);

    void delete(long workspaceId);

}