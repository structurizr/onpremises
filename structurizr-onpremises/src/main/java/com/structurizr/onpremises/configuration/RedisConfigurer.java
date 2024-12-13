package com.structurizr.onpremises.configuration;

import com.structurizr.util.StringUtils;

import java.util.Properties;

import static com.structurizr.onpremises.configuration.StructurizrProperties.*;

class RedisConfigurer extends Configurer {

    private static final String PROTOCOL_SEPARATOR = "://";
    private static final String PORT_SEPARATOR = ":";

    private static final String DEFAULT_PROTOCOL = "redis";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "6379";
    private static final String DEFAULT_DATABASE = "0";
    private static final String DEFAULT_PASSWORD = "";

    private static final String SECURE_PROTOCOL = "rediss";

    RedisConfigurer(Properties properties) {
        super(properties);
    }

    void apply() {
        setDefault(REDIS_PROTOCOL, DEFAULT_PROTOCOL);
        setDefault(REDIS_HOST, DEFAULT_HOST);
        setDefault(REDIS_PORT, DEFAULT_PORT);
        setDefault(REDIS_PASSWORD, DEFAULT_PASSWORD);
        setDefault(REDIS_DATABASE, DEFAULT_DATABASE);

        String redisEndpoint = properties.getProperty(StructurizrProperties.REDIS_ENDPOINT);
        if (!StringUtils.isNullOrEmpty(redisEndpoint)) {
            // setting structurizr.redis.endpoint will override:
            // - structurizr.redis.protocol
            // - structurizr.redis.host
            // - structurizr.redis.port
            String protocol = redisEndpoint.substring(0, redisEndpoint.indexOf(PROTOCOL_SEPARATOR));
            String host = redisEndpoint.substring(redisEndpoint.indexOf(PROTOCOL_SEPARATOR) + PROTOCOL_SEPARATOR.length());
            host = host.substring(0, host.indexOf(PORT_SEPARATOR));
            String port = redisEndpoint.substring(redisEndpoint.lastIndexOf(PORT_SEPARATOR) + PORT_SEPARATOR.length());

            properties.setProperty(REDIS_PROTOCOL, protocol);
            properties.setProperty(REDIS_HOST, host);
            properties.setProperty(REDIS_PORT, port);
        } else {
            // set structurizr.redis.endpoint from the separate protocol, host, and port properties
            String protocol = properties.getProperty(REDIS_PROTOCOL);
            String host = properties.getProperty(REDIS_HOST);
            String port = properties.getProperty(REDIS_PORT);
            redisEndpoint = String.format("%s://%s:%s", protocol, host, port);
            properties.setProperty(REDIS_ENDPOINT, redisEndpoint);
        }

        // a protocol of rediss will set structurizr.redis.ssl to true
        // (this is used in applicationContext-session-redis.xml)
        properties.setProperty(REDIS_SSL, Boolean.toString(SECURE_PROTOCOL.equals(properties.getProperty(REDIS_PROTOCOL, DEFAULT_PROTOCOL))));
    }

}