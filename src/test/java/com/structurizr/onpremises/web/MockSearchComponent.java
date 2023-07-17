package com.structurizr.onpremises.web;

import com.structurizr.Workspace;
import com.structurizr.onpremises.component.search.SearchComponent;
import com.structurizr.onpremises.component.search.SearchComponentException;
import com.structurizr.onpremises.component.search.SearchResult;

import java.util.List;
import java.util.Set;

public class MockSearchComponent implements SearchComponent {

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void index(Workspace workspace) {
    }

    @Override
    public List<SearchResult> search(String query, String type, Set<Long> workspaceIds) {
        return null;
    }

    @Override
    public void delete(long workspaceId) {
    }

    @Override
    public void clear() throws SearchComponentException {
    }

}