package com.structurizr.onpremises.configuration;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RedisConfigurerTests {

    @Test
    void apply_WithDefaults() {
        Properties properties = new Properties();
        new RedisConfigurer(properties).apply();

        assertEquals("redis", properties.getProperty(StructurizrProperties.REDIS_PROTOCOL));
        assertEquals("localhost", properties.getProperty(StructurizrProperties.REDIS_HOST));
        assertEquals("6379", properties.getProperty(StructurizrProperties.REDIS_PORT));
        assertEquals("0", properties.getProperty(StructurizrProperties.REDIS_DATABASE));
        assertEquals("", properties.getProperty(StructurizrProperties.REDIS_PASSWORD));
        assertEquals("redis://localhost:6379", properties.getProperty(StructurizrProperties.REDIS_ENDPOINT));
        assertEquals("false", properties.getProperty(StructurizrProperties.REDIS_SSL));
    }

    @Test
    void apply_WithoutEndpoint() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.REDIS_HOST, "example.com");
        properties.setProperty(StructurizrProperties.REDIS_PORT, "1234");
        properties.setProperty(StructurizrProperties.REDIS_DATABASE, "1");
        properties.setProperty(StructurizrProperties.REDIS_PASSWORD, "password");
        new RedisConfigurer(properties).apply();

        assertEquals("redis", properties.getProperty(StructurizrProperties.REDIS_PROTOCOL));
        assertEquals("example.com", properties.getProperty(StructurizrProperties.REDIS_HOST));
        assertEquals("1234", properties.getProperty(StructurizrProperties.REDIS_PORT));
        assertEquals("1", properties.getProperty(StructurizrProperties.REDIS_DATABASE));
        assertEquals("password", properties.getProperty(StructurizrProperties.REDIS_PASSWORD));
        assertEquals("redis://example.com:1234", properties.getProperty(StructurizrProperties.REDIS_ENDPOINT));
        assertEquals("false", properties.getProperty(StructurizrProperties.REDIS_SSL));
    }

    @Test
    void apply_WithEndpoint() {
        Properties properties = new Properties();
        properties.setProperty(StructurizrProperties.REDIS_ENDPOINT, "rediss://example.com:1234");
        new RedisConfigurer(properties).apply();

        assertEquals("rediss", properties.getProperty(StructurizrProperties.REDIS_PROTOCOL));
        assertEquals("example.com", properties.getProperty(StructurizrProperties.REDIS_HOST));
        assertEquals("1234", properties.getProperty(StructurizrProperties.REDIS_PORT));
        assertEquals("0", properties.getProperty(StructurizrProperties.REDIS_DATABASE));
        assertEquals("", properties.getProperty(StructurizrProperties.REDIS_PASSWORD));
        assertEquals("rediss://example.com:1234", properties.getProperty(StructurizrProperties.REDIS_ENDPOINT));
        assertEquals("true", properties.getProperty(StructurizrProperties.REDIS_SSL));
    }

}