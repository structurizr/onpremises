package com.structurizr.onpremises.component.search;

import com.structurizr.Workspace;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ElasticSearchComponentImplTests {

    private ElasticSearchComponentImpl searchComponent;

    @Before
    public void setUp() {
        searchComponent = new ElasticSearchComponentImpl("localhost", 9200, "http", null, null);
        searchComponent.async = false; // disable async indexing for testing
        searchComponent.setIndexName("structurizr-test");
        searchComponent.start();
    }

    @After
    public void tearDown() {
        searchComponent.stop();
    }

    @Test
    public void test_index_AddsTheWorkspaceToTheSearchIndex() {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.setId(12345);
        searchComponent.index(workspace);

        List<SearchResult> results = searchComponent.search("name", null, Collections.singleton(12345L));
        assertEquals(1, results.size());

        SearchResult result = results.get(0);
        assertEquals(12345, result.getWorkspaceId());
        assertEquals("workspace", result.getType());
        assertEquals("", result.getUrl());
        assertEquals("Name", result.getName());
        assertEquals("Description", result.getDescription());
    }

    @Test
    public void test_index_ReplacesTheWorkspaceInTheSearchIndexWhenItAlreadyExists() {
        Workspace workspace = new Workspace("Name", "Old");
        workspace.setId(12345);
        searchComponent.index(workspace);

        workspace.setDescription("New");
        searchComponent.index(workspace);

        List<SearchResult> results = searchComponent.search("new", null, Collections.singleton(12345L));
        assertEquals(1, results.size());

        SearchResult result = results.get(0);
        assertEquals(12345, result.getWorkspaceId());
        assertEquals("workspace", result.getType());
        assertEquals("", result.getUrl());
        assertEquals("Name", result.getName());
        assertEquals("New", result.getDescription());
    }

    @Test
    public void test_search_ThrowsAnException_WhenNoWorkspaceIdsAreProvided() {
        try {
            searchComponent.search("query", "type", Collections.emptySet());
            fail();
        } catch (IllegalArgumentException iae) {
            assertEquals("One or more workspace IDs must be provided.", iae.getMessage());
        }
    }

    @Test
    public void test_search_FiltersResultsByWorkspaceId() {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.setId(1);
        searchComponent.index(workspace);

        workspace = new Workspace("Name", "Description");
        workspace.setId(11);
        searchComponent.index(workspace);

        List<SearchResult> results = searchComponent.search("name", null, Collections.singleton(1L));
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getWorkspaceId());

        results = searchComponent.search("name", null, Collections.singleton(11L));
        assertEquals(1, results.size());
        assertEquals(11, results.get(0).getWorkspaceId());
    }

    @Test
    public void test_search_FiltersResultsByType() {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.setId(1);
        searchComponent.index(workspace);

        List<SearchResult> results = searchComponent.search("name", null, Collections.singleton(1L));
        assertEquals(1, results.size());

        results = searchComponent.search("name", DocumentType.WORKSPACE, Collections.singleton(1L));
        assertEquals(1, results.size());

        results = searchComponent.search("name", DocumentType.DOCUMENTATION, Collections.singleton(1L));
        assertEquals(0, results.size());
    }

}