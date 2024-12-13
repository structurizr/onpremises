package com.structurizr.onpremises.configuration;

import java.util.Properties;

abstract class Configurer {

    protected Properties properties;

    Configurer(Properties properties) {
        this.properties = properties;
    }

    void setDefault(String name, String defaultValue) {
        if (!properties.containsKey(name)) {
            properties.setProperty(name, defaultValue);
        }
    }

    abstract void apply();

}