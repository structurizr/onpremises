package com.structurizr.onpremises.configuration;

import com.structurizr.util.StringUtils;

import java.util.Properties;

import static com.structurizr.onpremises.configuration.StructurizrProperties.*;

class ElasticsearchConfigurer extends Configurer {

    ElasticsearchConfigurer(Properties properties) {
        super(properties);
    }

    private static final String PROTOCOL_SEPARATOR = "://";
    private static final String PORT_SEPARATOR = ":";

    private static final String DEFAULT_PROTOCOL = "http";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "9200";

    void apply() {
        setDefault(ELASTICSEARCH_PROTOCOL, DEFAULT_PROTOCOL);
        setDefault(ELASTICSEARCH_HOST, DEFAULT_HOST);
        setDefault(ELASTICSEARCH_PORT, DEFAULT_PORT);
        setDefault(ELASTICSEARCH_USERNAME, "");
        setDefault(ELASTICSEARCH_PASSWORD, "");

        String endpoint = properties.getProperty(ELASTICSEARCH_ENDPOINT);
        if (!StringUtils.isNullOrEmpty(endpoint)) {
            // setting elasticsearch.endpoint will override:
            // - elasticsearch.protocol
            // - elasticsearch.host
            // - elasticsearch.port
            String protocol = endpoint.substring(0, endpoint.indexOf(PROTOCOL_SEPARATOR));
            String host = endpoint.substring(endpoint.indexOf(PROTOCOL_SEPARATOR) + PROTOCOL_SEPARATOR.length());
            host = host.substring(0, host.indexOf(PORT_SEPARATOR));
            String port = endpoint.substring(endpoint.lastIndexOf(PORT_SEPARATOR) + PORT_SEPARATOR.length());

            properties.setProperty(ELASTICSEARCH_PROTOCOL, protocol);
            properties.setProperty(ELASTICSEARCH_HOST, host);
            properties.setProperty(ELASTICSEARCH_PORT, port);
        } else {
            // set elasticsearch.endpoint from the separate protocol, host, and port properties
            String protocol = properties.getProperty(ELASTICSEARCH_PROTOCOL);
            String host = properties.getProperty(ELASTICSEARCH_HOST);
            String port = properties.getProperty(ELASTICSEARCH_PORT);
            endpoint = String.format("%s://%s:%s", protocol, host, port);
            properties.setProperty(ELASTICSEARCH_ENDPOINT, endpoint);
        }

    }


}