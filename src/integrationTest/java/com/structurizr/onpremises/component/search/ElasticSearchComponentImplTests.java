package com.structurizr.onpremises.component.search;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class ElasticSearchComponentImplTests extends AbstractSearchComponentTests {

    private ElasticSearchComponentImpl searchComponent;

    @BeforeEach
    public void setUp() {
        searchComponent = new ElasticSearchComponentImpl("localhost", 9200, "http", null, null, "_doc");
        searchComponent.async = false; // disable async indexing for testing
        searchComponent.setIndexName("structurizr-test");
        searchComponent.start();
    }

    @AfterEach
    public void tearDown() {
        searchComponent.stop();
    }

    @Override
    protected SearchComponent getSearchComponent() {
        return searchComponent;
    }

}