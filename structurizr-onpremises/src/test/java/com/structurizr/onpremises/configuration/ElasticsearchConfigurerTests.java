package com.structurizr.onpremises.configuration;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ElasticsearchConfigurerTests {

    @Test
    void apply_WithDefaults() {
        Properties properties = new Properties();
        new ElasticsearchConfigurer(properties).apply();

        assertEquals("http", properties.getProperty(StructurizrProperties.ELASTICSEARCH_PROTOCOL));
        assertEquals("localhost", properties.getProperty(StructurizrProperties.ELASTICSEARCH_HOST));
        assertEquals("9200", properties.getProperty(StructurizrProperties.ELASTICSEARCH_PORT));
        assertEquals("", properties.getProperty(StructurizrProperties.ELASTICSEARCH_USERNAME));
        assertEquals("", properties.getProperty(StructurizrProperties.ELASTICSEARCH_PASSWORD));
        assertEquals("http://localhost:9200", properties.getProperty(StructurizrProperties.ELASTICSEARCH_ENDPOINT));
    }

    @Test
    void apply_WithoutEndpoint() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ELASTICSEARCH_HOST, "example.com");
        properties.setProperty(StructurizrProperties.ELASTICSEARCH_PORT, "1234");
        properties.setProperty(StructurizrProperties.ELASTICSEARCH_USERNAME, "username");
        properties.setProperty(StructurizrProperties.ELASTICSEARCH_PASSWORD, "password");
        new ElasticsearchConfigurer(properties).apply();

        assertEquals("http", properties.getProperty(StructurizrProperties.ELASTICSEARCH_PROTOCOL));
        assertEquals("example.com", properties.getProperty(StructurizrProperties.ELASTICSEARCH_HOST));
        assertEquals("1234", properties.getProperty(StructurizrProperties.ELASTICSEARCH_PORT));
        assertEquals("username", properties.getProperty(StructurizrProperties.ELASTICSEARCH_USERNAME));
        assertEquals("password", properties.getProperty(StructurizrProperties.ELASTICSEARCH_PASSWORD));
        assertEquals("http://example.com:1234", properties.getProperty(StructurizrProperties.ELASTICSEARCH_ENDPOINT));
    }

    @Test
    void apply_WithEndpoint() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.ELASTICSEARCH_ENDPOINT, "https://example.com:1234");
        new ElasticsearchConfigurer(properties).apply();

        assertEquals("https", properties.getProperty(StructurizrProperties.ELASTICSEARCH_PROTOCOL));
        assertEquals("example.com", properties.getProperty(StructurizrProperties.ELASTICSEARCH_HOST));
        assertEquals("1234", properties.getProperty(StructurizrProperties.ELASTICSEARCH_PORT));
        assertEquals("", properties.getProperty(StructurizrProperties.ELASTICSEARCH_USERNAME));
        assertEquals("", properties.getProperty(StructurizrProperties.ELASTICSEARCH_PASSWORD));
        assertEquals("https://example.com:1234", properties.getProperty(StructurizrProperties.ELASTICSEARCH_ENDPOINT));
    }

}