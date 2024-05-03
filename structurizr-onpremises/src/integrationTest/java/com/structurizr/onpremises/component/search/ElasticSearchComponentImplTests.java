package com.structurizr.onpremises.component.search;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

public class ElasticSearchComponentImplTests extends AbstractSearchComponentTests {

    private static ElasticsearchContainer elasticsearchContainer;
    private ElasticSearchComponentImpl searchComponent;

    @BeforeAll
    public static void startElasticsearchTestContainer() {
        elasticsearchContainer = new ElasticsearchContainer("elasticsearch:7.17.5")
                .withExposedPorts(9200);
        elasticsearchContainer.start();
    }

    @AfterAll
    public static void stopElasticsearchTestContainer() {
        elasticsearchContainer.stop();
    }

    @BeforeEach
    public void setUp() {
        searchComponent = new ElasticSearchComponentImpl("localhost", elasticsearchContainer.getFirstMappedPort(), "http", null, null);
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