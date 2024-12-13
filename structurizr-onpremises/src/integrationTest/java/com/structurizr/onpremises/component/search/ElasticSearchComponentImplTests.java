package com.structurizr.onpremises.component.search;


import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.configuration.StructurizrProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.util.Properties;

public class ElasticSearchComponentImplTests extends AbstractSearchComponentTests {

    private static ElasticsearchContainer elasticsearchContainer;
    private ElasticSearchComponentImpl searchComponent;

    @BeforeAll
    public static void startElasticsearchTestContainer() {
        elasticsearchContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.17.5")
                .withExposedPorts(9200);
        elasticsearchContainer.start();
    }

    @AfterAll
    public static void stopElasticsearchTestContainer() {
        elasticsearchContainer.stop();
    }

    @BeforeEach
    public void setUp() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ELASTICSEARCH_HOST, "localhost");
        properties.setProperty(StructurizrProperties.ELASTICSEARCH_PORT, "" + elasticsearchContainer.getFirstMappedPort());
        properties.setProperty(StructurizrProperties.ELASTICSEARCH_PROTOCOL, "http");
        properties.setProperty(StructurizrProperties.ELASTICSEARCH_USERNAME, "");
        properties.setProperty(StructurizrProperties.ELASTICSEARCH_PASSWORD, "");
        Configuration.init(properties);

        searchComponent = new ElasticSearchComponentImpl();
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