package com.structurizr.onpremises.component.search;

import com.structurizr.Workspace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ApacheLuceneSearchComponentTests {

    private static final File DATA_DIRECTORY = new File("./build/ApacheLuceneSearchComponentTests");

    private ApacheLuceneSearchComponentImpl searchComponent;

    @Before
    public void setUp() {
        searchComponent = new ApacheLuceneSearchComponentImpl(DATA_DIRECTORY);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void index_AddsTheWorkspaceToTheSearchIndex() {
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
    public void index_ReplacesTheWorkspaceInTheSearchIndexWhenItAlreadyExists() {
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
    public void search_ReturnsAnEmptyList_WhenNoWorkspaceIdsAreProvided() {
        Workspace workspace = new Workspace("Name", "Description");
        workspace.setId(1);
        searchComponent.index(workspace);

        List<SearchResult> results = searchComponent.search("query", "type", Collections.emptySet());
        assertEquals(0, results.size());
    }

    @Test
    public void search_FiltersResultsByWorkspaceId() {
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
    public void search_FiltersResultsByType() {
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